package org.dfhu.vpodplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.dfhu.vpodplayer.model.Show;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;


public class ShowsRecyclerViewAdapter extends RecyclerView.Adapter<ShowsRecyclerViewAdapter.ViewHolder> {

    public ShowsRecyclerViewAdapter(List<Show> shows) {
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
        holder.itemShowTitle.setText("" + position);
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener {

        final TextView itemShowTitle;
        final Button buttonWorthClicking;

        ViewHolder(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemShowTitle = (TextView) itemView.findViewById(R.id.itemShowTitle);
            buttonWorthClicking = (Button) itemView.findViewById(R.id.buttonWorthClicking);

            setViewFacade(itemView);

            itemView.setOnCreateContextMenuListener(this);
        }

        /**
         * Because the layout is nested to for the ripple we build a facade to pass along clicks
         */
        private void setViewFacade(final View itemView) {
            View facedForClicks = itemView.findViewById(R.id.showClickable);
            facedForClicks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // pass along the short click
                    ViewHolder.this.onClick(itemView);
                }
            });
            facedForClicks.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                    // no-op but passes up to the ViewHolder
                }
            });

            buttonWorthClicking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("one-off", "the button was clicked");
                }
            });
        }

        @Override
        public void onClick(View v) {
            Log.d("one-off", "short click on item");
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            Log.d("one-off", "long click on item");
        }
    }
}
