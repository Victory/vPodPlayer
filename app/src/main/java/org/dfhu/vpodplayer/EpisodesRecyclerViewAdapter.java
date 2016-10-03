package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dfhu.vpodplayer.model.Episode;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class EpisodesRecyclerViewAdapter extends RecyclerView.Adapter<EpisodesRecyclerViewAdapter.ViewHolder> {

    static class EpisodeClickBus {
        private EpisodeClickBus() {}
        private static PublishSubject<Episode> subject = PublishSubject.create();

        static void publish(Episode v) { subject.onNext(v); }
        static Observable<Episode> getEvents() { return subject; }
    }


    private final List<Episode> episodes;

    public EpisodesRecyclerViewAdapter(List<Episode> episodes) {
        this.episodes = episodes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.episode_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.setEpisode(episode);
        holder.itemEpisodeTitle.setText(episode.title);
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
     implements View.OnClickListener {

        private Episode episode;
        final TextView itemEpisodeTitle;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemEpisodeTitle = (TextView) itemView.findViewById(R.id.itemEpisodeTitle);
        }

        public void setEpisode(Episode episode) {
            this.episode = episode;
        }

        @Override
        public void onClick(View v) {
            EpisodeClickBus.publish(episode);
        }
    }
}
