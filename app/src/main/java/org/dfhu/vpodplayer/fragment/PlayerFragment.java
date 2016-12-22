package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.dfhu.vpodplayer.PlayerControlsView;
import org.dfhu.vpodplayer.PodPlayer;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.VPodPlayerApplication;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;
import org.dfhu.vpodplayer.util.LoggingSubscriber;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class PlayerFragment extends VicFragment<VPodPlayerApplication> {

    @Inject
    PodPlayer podPlayer;

    boolean isPlaying = false;
    private Subscription updatePositionSubscription;
    private CompositeSubscription subscriptions;
    private Context applicationContext;
    private PlayerControlsView controls;
    private AlertDialog annotateDialog;
    private MenuItem annotate;

    private static class UpdatePositionBus {

        private UpdatePositionBus() {}
        private static PublishSubject<Long> subject = PublishSubject.create();

        static void publish(Long v) { subject.onNext(v); }
        static Observable<Long> getEvents() { return subject; }
    }

    @Override
    void inject() {
        getRealApplication().getPodPlayerComponent().inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        applicationContext = getContext().getApplicationContext();

    }

    @Override
    public void onPause() {
        if (annotateDialog != null) {
            annotateDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        podPlayer.end();
        getRealApplication().releasePodPlayerComponent();
        subscriptions.unsubscribe();
        if (annotate != null) annotate.setVisible(false);
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        annotate = menu.findItem(R.id.annotate);
        annotate.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId != R.id.annotate) {
            return false;
        }

        final int episodeId = getArguments().getInt("episodeId");
        final View layout =
                getActivity().getLayoutInflater().inflate(R.layout.annotate_episode, null);
        layout.setTag(episodeId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        annotateDialog = builder.setTitle("Annotate")
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        TextView comment =
                                (TextView) layout.findViewById(R.id.episodeCommentAdder);
                        ToggleButton noDelete =
                                (ToggleButton) layout.findViewById(R.id.episodeNoDeleteChooser);
                        RatingBar rate =
                                (RatingBar) layout.findViewById(R.id.episodeRater);
                        Integer id = (Integer) layout.getTag();

                        Episodes episodes = new Episodes(layout.getContext().getApplicationContext());
                        Episode episode = episodes.getById(id);

                        episode.rating = (int) rate.getRating();
                        episode.notes = comment.getText().toString();
                        episode.deletionState = noDelete.isChecked() ? Episode.DS_DO_NOT_DELETE : 0;
                        episodes.addOrUpdate(episode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setView(layout)
                .create();

        Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {

                Episodes episodes = new Episodes(layout.getContext().getApplicationContext());
                Episode episode = episodes.getById(episodeId);
                TextView comment =
                        (TextView) layout.findViewById(R.id.episodeCommentAdder);
                ToggleButton noDelete =
                        (ToggleButton) layout.findViewById(R.id.episodeNoDeleteChooser);
                RatingBar rate =
                        (RatingBar) layout.findViewById(R.id.episodeRater);

                comment.setText(episode.notes);
                noDelete.setChecked(episode.deletionState == Episode.DS_DO_NOT_DELETE);
                rate.setRating(episode.rating);
                return Observable.just(true);
            }
        }).subscribe(new LoggingSubscriber<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                annotateDialog.show();
            }
        });

        return true;
    }

    /**
     * Set the listeners for controls view
     */
    private void setControlsListeners(PlayerControlsView controls, final Episode episode) {
        final Context fragmentContext = getContext().getApplicationContext();
        controls.setOnCenterClickListener(new PlayerControlsView.OnCenterClickListener() {
            @Override
            public void click(PlayerControlsView playerControlsView) {
                Toast.makeText(fragmentContext, episode.title, Toast.LENGTH_SHORT).show();

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
                        .onBackpressureDrop()
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
