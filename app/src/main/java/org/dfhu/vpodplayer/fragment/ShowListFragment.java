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

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.ShowsRecyclerViewAdapter;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;

import java.util.List;

public class ShowListFragment extends Fragment {

    public static final String TAG = ShowListFragment.class.getName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
}
