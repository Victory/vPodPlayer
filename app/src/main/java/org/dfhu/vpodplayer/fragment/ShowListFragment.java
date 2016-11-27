package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.ShowsRecyclerViewAdapter;
import org.dfhu.vpodplayer.VPodPlayer;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.service.DeleteEpisodeFilesService;
import org.dfhu.vpodplayer.service.RefreshAllShowsService;
import org.dfhu.vpodplayer.service.UnsubscribeService;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ShowListFragment extends Fragment {

    public static final String TAG = ShowListFragment.class.getName();

    AlertDialog unsubscribeConfirmationAlertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (unsubscribeConfirmationAlertDialog != null) {
            unsubscribeConfirmationAlertDialog.dismiss();
            unsubscribeConfirmationAlertDialog = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(this.getClass().getName(), "onCreateView");

        View view = inflater.inflate(R.layout.fragment_shows, container, false);

        Shows db = new Shows(getActivity().getApplicationContext());
        List<Show> shows = db.all();
        ShowsRecyclerViewAdapter adapter = new ShowsRecyclerViewAdapter(shows);

        RecyclerView showsRecyclerView = (RecyclerView) view.findViewById(R.id.showsRecyclerView);
        showsRecyclerView.setAdapter(adapter);
        showsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() != R.id.ShowListFragmentContextMenuId) {
            return false;
        }
        String clickedTitle = (String) item.getTitle();
        Context context = getContext().getApplicationContext();
        if (context.getString(R.string.contextMenuDeleteListened).equals(clickedTitle)) {
            startDeleteListenedService(item.getItemId());
        } else if (context.getString(R.string.contextMenuUnsubscribe).equals(clickedTitle)) {
                confirmAndConditionallyUnsubscribe(item.getItemId());
        } else {
            VPodPlayer.safeToast("Not implemented");
        }

        return true;
    }


    /**
     * Start the service that deletes listened episodes for this showId
     * @param showId - target show id
     */
    private void startDeleteListenedService(int showId) {
        Intent intent = new Intent(getActivity(), DeleteEpisodeFilesService.class);
        intent.setData(DeleteEpisodeFilesService.URI_DELETE_LISTENED);
        intent.putExtra("showId", showId);
        getActivity().startService(intent);
    }

    /**
     * Confirm that the user wants to unsubscribe from this podcast and if
     * so start unsubscribe service
     */
    private void confirmAndConditionallyUnsubscribe(final int itemId) {
        Observable.just(itemId)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Integer, Observable<Show>>() {
                    @Override
                    public Observable<Show> call(Integer integer) {
                        Shows db = new Shows(getContext().getApplicationContext());
                        return Observable.just(db.getById(itemId));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoggingSubscriber<Show>() {
                    @Override
                    public void onNext(Show show) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        unsubscribeConfirmationAlertDialog = builder
                                .setTitle("Unsubscribe from Podcast")
                                .setMessage("Are you sure you wish to unsubscribe from " + show.title)
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // no-op just hide dialog
                                    }
                                })
                                .setPositiveButton("Unsubscribe", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startUnsubscribeService(itemId);
                                    }
                                }).show();
                    }
                });
    }

    /**
     * Start a service that unsubscribe from a show
     * @param showId - target show id
     */
     void startUnsubscribeService(int showId) {
        Intent intent = new Intent(getActivity(), UnsubscribeService.class);
        intent.setData(UnsubscribeService.URI_UNSUBSCRIBE);
        intent.putExtra("showId", showId);
        getActivity().startService(intent);
    }

    public void refreshAllEpisodes() {
        Intent intent = new Intent(getActivity(), RefreshAllShowsService.class);
        intent.setData(RefreshAllShowsService.URI_REFRESH_ALL);
        getActivity().startService(intent);
    }
}
