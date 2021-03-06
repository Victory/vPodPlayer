package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dfhu.vpodplayer.model.Show;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;


public class ShowsRecyclerViewAdapter extends RecyclerView.Adapter<ShowsRecyclerViewAdapter.ViewHolder> {

    static class ShowClickBus {
        private ShowClickBus() {}
        private static PublishSubject<Show> subject = PublishSubject.create();

        static void publish(Show v) { subject.onNext(v); }
        static Observable<Show> getEvents() { return subject; }
    }

    private final List<Show> shows;

    public ShowsRecyclerViewAdapter(List<Show> shows) {
        this.shows = shows;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.show_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Show show = shows.get(position);
        holder.setShow(show);
        holder.itemShowTitle.setText(show.title);
    }

    @Override
    public int getItemCount() {
        return shows.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener {

        final TextView itemShowTitle;

        private Show show;

        public void setShow(Show show) {
            this.show = show;
        }

        ViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemShowTitle = (TextView) itemView.findViewById(R.id.itemShowTitle);

            setViewFacade(itemView);

            itemView.setOnCreateContextMenuListener(this);
        }

        /**
         * Because the layout is nested to for the ripple we build a facade to pass along clicks
         */
        private void setViewFacade(final View itemView) {
            View facadeForClicks = itemView.findViewById(R.id.showClickable);
            facadeForClicks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewHolder.this.onClick(itemView);
                }
            });
            facadeForClicks.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    // no-op but passes up to the ViewHolder
                }
            });
        }

        @Override
        public void onClick(View v) {
            ShowClickBus.publish(show);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            Context context = view.getContext().getApplicationContext();
            menu.setHeaderTitle(show.title);

            menu.add(R.id.ShowListFragmentContextMenuId, show.id, 1,
                    context.getString(R.string.contextMenuDeleteListened));
            menu.add(R.id.ShowListFragmentContextMenuId, show.id, 2,
                    context.getString(R.string.contextMenuUnsubscribe));
        }
    }
}
