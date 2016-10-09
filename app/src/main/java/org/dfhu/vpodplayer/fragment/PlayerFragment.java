package org.dfhu.vpodplayer.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dfhu.vpodplayer.PlayerControlsView;
import org.dfhu.vpodplayer.PodPlayer;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PlayerFragment extends Fragment {

    @Inject
    PodPlayer podPlayer;

    private boolean isPlaying = false;
    private Subscription updatePositionSubscription;
    private List<Subscription> subscriptions = new LinkedList<>();

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

        bindToUpdatePositionBus();
    }

    @Override
    public void onDestroy() {
        podPlayer.end();

        for (Subscription sub: subscriptions) {
            sub.unsubscribe();
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        PlayerControlsView controls = (PlayerControlsView) view.findViewById(R.id.playerControls);

        int episodeId = getArguments().getInt("episodeId");
        Episodes db = new Episodes(getActivity().getApplicationContext());
        final Episode episode = db.getById(episodeId);

        //Uri uri = Uri.parse(episode.url);
        Uri uri = Uri.parse("http://192.168.1.6:3000/pm.mp3");

        podPlayer.startPlayingUri(uri);
        isPlaying = true;
        subscribeUpdatePositionSubscription();

        controls.setOnCenterClickListener(new PlayerControlsView.OnCenterClickListener() {
            @Override
            public void click(PlayerControlsView playerControlsView) {

                Log.d("PlayerFragment", "clicked: " + episode.title);
                if (isPlaying) {
                    podPlayer.setPlayWhenReady(false);
                    playerControlsView.setCenterColor(PlayerControlsView.INNER_COLOR_PAUSE);
                    unsubscribeUpdatePositionSubscription();
                } else {
                    podPlayer.setPlayWhenReady(true);
                    playerControlsView.setCenterColor(PlayerControlsView.INNER_COLOR_PLAY);
                    subscribeUpdatePositionSubscription();
                }

                isPlaying = !isPlaying;
            }
        });

        controls.setOnPositionListener(new PlayerControlsView.OnPositionListener() {
            @Override
            public void positionChange(double positionPercent) {
                Log.d("PlayerFragment", "positionPercent: " + positionPercent);
                long duration = podPlayer.getDuration();
                double seek = duration * positionPercent;
                podPlayer.seekTo((long) seek);
            }
        });

        return view;
    }

    public void bindToUpdatePositionBus() {

        UpdatePositionBus.getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        PlayerControlsView view = (PlayerControlsView) getView().findViewById(R.id.playerControls);
                        if (view.getIsMoving()) {
                            return;
                        }
                        double duration = podPlayer.getDuration();
                        double position = podPlayer.getCurrentPosition();
                        double positionPercent = position / duration;

                        Log.d("PlayerFragement", "" + positionPercent + " - " + position);

                        PlayerControlsView.PlayerInfo playerInfo = new PlayerControlsView.PlayerInfo();
                        playerInfo.positionPercent = positionPercent;
                        playerInfo.currentPosition = (long) position;
                        view.updatePlayer(playerInfo);
                    }
                });
    }

    public void unsubscribeUpdatePositionSubscription() {
        if (updatePositionSubscription != null) {
            updatePositionSubscription.unsubscribe();
        }
        updatePositionSubscription = null;
    }

    public void subscribeUpdatePositionSubscription () {
        updatePositionSubscription =
                Observable.interval(0, 5000, TimeUnit.MILLISECONDS, Schedulers.newThread())
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
