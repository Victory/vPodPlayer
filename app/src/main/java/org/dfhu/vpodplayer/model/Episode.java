package org.dfhu.vpodplayer.model;


public class Episode {
    public int id;
    public int showId = -1;
    public String title = "unkown title";
    public String description;
    public String url;
    public String localUri;
    public boolean isDownloaded = false;
    public int percentListened = 0;
    public int sizeInBytes = -1;
    public long downloadId = -1;

    @Override
    public String toString() {
        String tmpUrl = (url != null) ? url : "NULL";
        String tmpLocalUri = (localUri != null) ? localUri : "NULL";

        return "[id:" + id + " showId:" + showId + " -- " + title + " downloadId: " + downloadId + " " + tmpLocalUri + " " + tmpUrl + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Episode)) {
            return false;
        }
        return ((Episode) o).url.equals(this.url);
    }
}
