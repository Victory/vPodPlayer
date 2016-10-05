package org.dfhu.vpodplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class EpisodeListFragment extends Fragment {

    private int showId;

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

        showId = getArguments().getInt("showId");

        Episodes db = new Episodes(getActivity().getApplicationContext());
        List<Episode> episodes = db.allForShow(showId);
        EpisodesRecyclerViewAdapter adapter = new EpisodesRecyclerViewAdapter(episodes);

        RecyclerView showsRecyclerView = (RecyclerView) view.findViewById(R.id.episodesRecyclerView);
        showsRecyclerView.setAdapter(adapter);
        showsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }
}
