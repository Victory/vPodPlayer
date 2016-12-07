package org.dfhu.vpodplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dfhu.vpodplayer.EpisodesRecyclerViewAdapter;
import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Episode;
import org.dfhu.vpodplayer.sqlite.Episodes;

import java.util.List;

public class EpisodeListFragment extends VicFragment {

    public static final String TAG = EpisodeListFragment.class.getName();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(this.getClass().getName(), "onCreateView");

        View view = inflater.inflate(R.layout.fragment_episodes, container, false);

        int showId = getArguments().getInt("showId");
        int episodeId = getArguments().getInt("episodeId");

        Episodes db = new Episodes(getActivity().getApplicationContext());
        List<Episode> episodes = db.allForShow(showId);
        int episodeScrollPosition = findEpisodeScrollPosition(episodes, episodeId);
        EpisodesRecyclerViewAdapter adapter = new EpisodesRecyclerViewAdapter(episodes, episodeScrollPosition);

        RecyclerView showsRecyclerView = (RecyclerView) view.findViewById(R.id.episodesRecyclerView);
        showsRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.scrollToPosition(episodeScrollPosition - 1);
        showsRecyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    /**
     * Get position to scroll to for the given episode
     *
     * @param episodes - all episodes in order
     * @param episodeId - target episode id
     * @return
     */
    private int findEpisodeScrollPosition(List<Episode> episodes, int episodeId) {
        if (episodeId == 0) {
            return 0;
        }

        for (int ii = 0; ii < episodes.size(); ii++) {
            if (episodes.get(ii).id == episodeId) {
                return ii;
            }
        }

        return 0;
    }
}
