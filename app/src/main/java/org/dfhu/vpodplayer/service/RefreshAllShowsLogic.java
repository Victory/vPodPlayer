package org.dfhu.vpodplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;

import org.dfhu.vpodplayer.R;
import org.dfhu.vpodplayer.model.Show;
import org.dfhu.vpodplayer.sqlite.Shows;
import org.dfhu.vpodplayer.util.LoggingSubscriber;
import org.dfhu.vpodplayer.util.StringsProvider;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.schedulers.Schedulers;

class RefreshAllShowsLogic {
    private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
    private final Shows showsDb;
    private final StringsProvider stringsProvider;

    private RefreshAllShowsLogic(
            @NonNull RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
            @NonNull Shows showsDb,
            @NonNull StringsProvider stringsProvider) {
        this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
        this.showsDb = showsDb;
        this.stringsProvider = stringsProvider;
    }

    static class Builder {
        private RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private Shows showsDb;
        private StringsProvider stringsProvider;

        public Builder refreshAllShowsServiceNotification(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            return this;
        }

        public Builder showsDb(@NonNull Shows showsDb) {
            this.showsDb = showsDb;
            return this;
        }

        public Builder stringsProvider(StringsProvider stringsProvider) {
            this.stringsProvider = stringsProvider;
            return this;
        }

        public RefreshAllShowsLogic build() {
            //noinspection PrivateMemberAccessBetweenOuterAndInnerClass
            return new RefreshAllShowsLogic(
                    this.refreshAllShowsServiceNotification,
                    this.showsDb,
                    this.stringsProvider);
        }

    }

    void handleIntent() {
        Observable.fromCallable(new RefreshAllShows(refreshAllShowsServiceNotification, showsDb, stringsProvider))
                .subscribeOn(Schedulers.io())
                .toBlocking()
                .subscribe(new RefreshAllShowsSubscriber(refreshAllShowsServiceNotification, stringsProvider));
    }

    static class RefreshResults {
        int numShowsUpdated;
    }

    private static class RefreshAllShows implements Callable<RefreshResults> {

        private final Shows showsDb;
        private final StringsProvider stringsProvider;
        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;

        RefreshAllShows(RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                        Shows showsDb,
                        StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.showsDb = showsDb;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public RefreshResults call() throws Exception {
            RefreshResults refreshResults = new RefreshResults();
            showNotification(stringsProvider.getString(R.string.ellipsis));
            List<Show> showList = showsDb.all();
            updateEachShow(showList, refreshResults);
            return refreshResults;
        }

        private void updateEachShow(List<Show> showList, RefreshResults refreshResults) {
            for (Show show: showList) {
                showNotification(show.title);
                refreshResults.numShowsUpdated += 1;
            }
        }

        private void showNotification(String showTitle) {
            String appName = stringsProvider.getString(R.string.app_name);
            String info = stringsProvider.getString(R.string.updatingFeed, showTitle);
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }

    private static class RefreshAllShowsSubscriber extends LoggingSubscriber<RefreshResults> {

        private final RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification;
        private final StringsProvider stringsProvider;

        RefreshAllShowsSubscriber(
                RefreshAllShowsService.RefreshAllShowsServiceNotification refreshAllShowsServiceNotification,
                StringsProvider stringsProvider) {
            this.refreshAllShowsServiceNotification = refreshAllShowsServiceNotification;
            this.stringsProvider = stringsProvider;
        }

        @Override
        public void onNext(RefreshResults refreshResults) {
            showNotification(refreshResults);
        }

        private void showNotification(RefreshResults refreshResults) {
            String appName = stringsProvider.getString(R.string.app_name);
            String info = stringsProvider.getString(R.string.numShowsUpdated, refreshResults.numShowsUpdated);
            refreshAllShowsServiceNotification.show(appName, info);
        }
    }
}
