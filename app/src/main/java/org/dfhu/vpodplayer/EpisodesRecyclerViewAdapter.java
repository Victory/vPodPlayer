package org.dfhu.vpodplayer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dfhu.vpodplayer.model.Episode;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public class EpisodesRecyclerViewAdapter extends RecyclerView.Adapter<EpisodesRecyclerViewAdapter.ViewHolder> {

    private final int highlightedEpisode;
    private final List<Episode> episodes;
    private Context context;

    public static class EpisodeClickBus {
        private EpisodeClickBus() {}
        private static PublishSubject<Episode> subject = PublishSubject.create();

        public static void publish(Episode v) { subject.onNext(v); }
        static Observable<Episode> getEvents() { return subject; }
    }



    public EpisodesRecyclerViewAdapter(List<Episode> episodes, int highlightedEpisode) {
        this.episodes = episodes;
        this.highlightedEpisode = highlightedEpisode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.episode_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.setEpisode(episode);
        holder.itemEpisodeTitle.setText(episode.title);

        setColorTitleColor(holder.itemEpisodeTitle, episode);
        setDuration(holder.itemEpisodeDuration, episode);
        setPercentListened(holder.itemEpisodePercentListened, episode);

        if (highlightedEpisode > 0 && position == highlightedEpisode) {
            ObjectAnimator opacity = ObjectAnimator.ofFloat(holder.itemEpisodeTitle, "alpha", 0.0f, 1.0f);
            opacity.setDuration(800);
            opacity.setRepeatCount(3);
            opacity.start();
        }
    }

    private void setPercentListened(TextView itemEpisodePercentListened, Episode episode) {
        if (!episode.isDownloaded()) {
            itemEpisodePercentListened.setVisibility(View.GONE);
            return;
        }
        String prettyPercent = episode.percentListened + "%";
        itemEpisodePercentListened.setText("Listened: " + prettyPercent);
    }

    private void setDuration(TextView itemEpisodeDuration, Episode episode) {
        if (!episode.isDownloaded()) {
            itemEpisodeDuration.setVisibility(View.GONE);
            return;
        }
        String prettyTime = DateUtils.formatElapsedTime(episode.duration / 1000);
        itemEpisodeDuration.setText("Duration: " + prettyTime);
    }

    private void setColorTitleColor(TextView itemEpisodeTitle, Episode episode) {
        if (!episode.isDownloaded()) {
            itemEpisodeTitle.setTextColor(ContextCompat.getColor(context, R.color.colorNotDownloaded));
        } else if (episode.percentListened < 5) {
            itemEpisodeTitle.setTextColor(ContextCompat.getColor(context, R.color.colorDownloaded));
        } else if (episode.percentListened >= 5 && episode.percentListened < 95) {
            itemEpisodeTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPartiallyListened));
        } else {
            itemEpisodeTitle.setTextColor(ContextCompat.getColor(context, R.color.colorListened));
        }
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
     implements View.OnClickListener {

        private Episode episode;
        final TextView itemEpisodeTitle;
        final TextView itemEpisodeDuration;
        final TextView itemEpisodePercentListened;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemEpisodeTitle = (TextView) itemView.findViewById(R.id.itemEpisodeTitle);
            itemEpisodeDuration = (TextView) itemView.findViewById(R.id.itemEpisodeDuration);
            itemEpisodePercentListened = (TextView) itemView.findViewById(R.id.itemEpisodePercentListened);
        }

        public void setEpisode(Episode episode) {
            this.episode = episode;
        }

        @Override
        public void onClick(View v) {
            if (episode == null) {
                throw new NullPointerException("no episode set in ViewHolder");
            }
            EpisodeClickBus.publish(episode);
        }
    }
}
