package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.dfhu.vpodplayer.PlayerControlsView;
import org.dfhu.vpodplayer.PodPlayer;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class PlayerFragment extends Fragment {

    @Inject
    PodPlayer podPlayer;

    boolean isPlaying = false;
    private Subscription updatePositionSubscription;
    private CompositeSubscription subscriptions;
    private Context applicationContext;
    private PlayerControlsView controls;

    private static class UpdatePositionBus {

        private UpdatePositionBus() {}
        private static PublishSubject<Long> subject = PublishSubject.create();

        static void publish(Long v) { subject.onNext(v); }
        static Observable<Long> getEvents() { return subject; }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((VPodPlayerApplication) getActivity().getApplication()).component().inject(this);

        applicationContext = getContext().getApplicationContext();

    }

    @Override
    public void onDestroy() {
        podPlayer.end();
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (subscriptions != null) {
            subscriptions.unsubscribe();
        }
        subscriptions = new CompositeSubscription();

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        controls = (PlayerControlsView) view.findViewById(R.id.playerControls);

        int episodeId = getArguments().getInt("episodeId");
        Episodes db = new Episodes(getActivity().getApplicationContext());
        final Episode episode = db.getById(episodeId);

        bindToUpdatePositionBus(applicationContext, episodeId);

        Uri uri = Uri.parse(episode.localUri);

        boolean started = podPlayer.startPlayingUri(uri);
        long seekTo = episode.getPlayPosition();
        if (started && seekTo > 0) {
            podPlayer.seekTo(seekTo);
        }
        podPlayer.setMetaDataTitle(episode.title);
        isPlaying = true;
        subscribeUpdatePosition();

        setControlsListeners(controls, episode);

        Toast.makeText(getContext(), "Playing: " + episode.title, Toast.LENGTH_LONG).show();
        return view;
    }

    /**
     * Set the listeners for controls view
     */
    private void setControlsListeners(PlayerControlsView controls, final Episode episode) {
        controls.setOnCenterClickListener(new PlayerControlsView.OnCenterClickListener() {
            @Override
            public void click(PlayerControlsView playerControlsView) {
                Toast.makeText(getContext(), episode.title, Toast.LENGTH_SHORT).show();

                if (isPlaying) {
                    podPlayer.setPlayWhenReady(false);
                    playerControlsView.setCenterColor(R.color.colorFarBack);
                    unsubscribeUpdatePosition();
                } else {
                    podPlayer.setPlayWhenReady(true);
                    playerControlsView.setCenterColor(R.color.colorListened);
                    subscribeUpdatePosition();
                }

                isPlaying = !isPlaying;
            }
        });

        controls.setOnPositionDoneListener(new PlayerControlsView.OnPositionDoneListener() {
            @Override
            public void positionChange(double positionPercent) {
                //Log.d("PlayerFragment", "positionPercent: " + positionPercent);
                long duration = podPlayer.getDuration();
                long seek = (long) (duration * positionPercent);
                podPlayer.seekTo(seek);

                UpdatePositionBus.publish(-1L);
            }
        });
    }

    /**
     * Set the play position to 0
     */
    public void restartEpisode() {
        if (controls == null) {
            return;
        }
        PlayerControlsView.OnPositionDoneListener onPositionDoneListener = controls.getOnPositionDoneListener();
        if (onPositionDoneListener == null) {
            return;
        }
        onPositionDoneListener.positionChange(0);
    }

    /**
     * Bind to the "ticks" of the player controller
     */
    public void bindToUpdatePositionBus(final Context context, final int episodeId) {
        Subscription sub = UpdatePositionBus.getEvents()
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    int count = 0;
                    final int numEventsPerSave = 15;
                    double lastPosition = 0;

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    /**
                     * Update position in view and save periodically
                     *
                     * @param saveIndicator - if < 0 save immediately else ignore
                     */
                    @Override
                    public void onNext(Long saveIndicator) {
                        if (getView() == null) {
                            return;
                        }

                        PlayerControlsView view = (PlayerControlsView) getView().findViewById(R.id.playerControls);
                        if (view.getIsMoving()) {
                            return;
                        }
                        double duration = podPlayer.getDuration();
                        double position = podPlayer.getCurrentPosition();
                        double positionPercent = position / duration;

                        // don't update if paused except for first run
                        if (count > numEventsPerSave && lastPosition == position && saveIndicator > 0) {
                            return;
                        }
                        lastPosition = position;

                        PlayerControlsView.PlayerInfo playerInfo = new PlayerControlsView.PlayerInfo();
                        playerInfo.positionPercent = positionPercent;
                        playerInfo.currentPosition = (long) position;
                        playerInfo.duration = duration;
                        view.updatePlayer(playerInfo);

                        count += 1;
                        if (saveIndicator < 0 || count % numEventsPerSave == 0) {
                            Episodes db = new Episodes(context);
                            Episode episode = db.getById(episodeId);
                            episode.percentListened = (int) Math.floor(100 * playerInfo.positionPercent);

                            // corner cases of start and end of episode
                            if (episode.percentListened >= 98) {
                                episode.percentListened = 100;
                            } else if (episode.percentListened <= 2) {
                                episode.percentListened = 0;
                            }
                            db.updatePercentListened(episode);
                        }
                    }
                });

        subscriptions.add(sub);
    }

    public void unsubscribeUpdatePosition() {
        if (updatePositionSubscription != null) {
            updatePositionSubscription.unsubscribe();
        }
        updatePositionSubscription = null;
    }

    public void subscribeUpdatePosition() {
        updatePositionSubscription =
                Observable.interval(0, 1000, TimeUnit.MILLISECONDS, Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {

                                Log.d("PlayerFragement", "complete");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("PlayerFragement", "error", e);
                            }

                            @Override
                            public void onNext(Long aLong) {
                               UpdatePositionBus.publish(aLong);
                            }
                        });

        subscriptions.add(updatePositionSubscription);
    }
}
