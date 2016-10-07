package org.dfhu.vpodplayer.model;


public class Episode {
    public int id;
    public int showId = -1;
    public String title = "unkown title";
    public String description;
    public String url;
    public String pathOnDisk;
    public boolean isDownloaded;
    public int percentListened;
    public int sizeInBytes;

    @Override
    public String toString() {
        String tmpUrl =  url;
        if (url == null) {
           tmpUrl = "NULL";
        }

        return "[id:" + id + " showId:" + showId + " -- " + title + " " + tmpUrl + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Episode)) {
            return false;
        }
        return ((Episode) o).url.equals(this.url);
    }
}