package org.dfhu.vpodplayer.model;


public class Episode {
    public int id;
    public int showId = -1;
    public String title = "unkown title";
    public String description;
    public String url;
    public String localUri;
    public int isDownloaded = 0;
    public int percentListened = 0;
    public int sizeInBytes = -1;
    public long downloadId = -1;
    public long pubDate;
    public long lastListened;
    public int deletionState;
    public int rating;
    public String notes;
    public String uniqueId;

    public static final int DS_NORMAL = 0;
    public static final int DS_DO_NOT_DELETE = 1;

    /** null if not set so can be populated on first pass */
    public Integer duration = null;

    @Override
    public String toString() {
        String tmpUrl = (url != null) ? url : "NULL";
        String tmpLocalUri = (localUri != null) ? localUri : "NULL";

        return "[id:" + id + " showId:" + showId + " -- " + title + " pubDate:" + pubDate + " downloadId: " + downloadId + " " + tmpLocalUri + " " + tmpUrl + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Episode)) {
            return false;
        }
        return ((Episode) o).uniqueId.equals(this.uniqueId);
    }

    public boolean isDownloaded() {
        return isDownloaded == 1;
    }

    /**
     * The last recorded play position in Ms
     */
    public long getPlayPosition() {
        return (int) Math.floor((duration) * (percentListened / 100.0));
    }

    /**
     * @return - true if is downloaded and the localUri is not null, else false
     */
    public boolean isReadyToPlay() {
        return isDownloaded() && localUri != null && !localUri.isEmpty();
    }

    /**
     * @return - true we could play this but more than 95% listened, else false
     */
    public boolean isReadyToDelete() {
       return isReadyToPlay() && percentListened > 95;
    }
}
