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
    public String pubDate;
    public long lastListened;

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
        return ((Episode) o).url.equals(this.url);
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

}
