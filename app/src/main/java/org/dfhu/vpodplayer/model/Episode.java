package org.dfhu.vpodplayer.model;


public class Episode {
    int id;
    int showId;
    String title;
    String description;
    String url;
    String pathOnDisk;
    boolean isDownloaded;
    int percentListened;
    int sizeInBytes;

    @Override
    public String toString() {
        return "[id:" + id + " -- " + title + " " + url + "]";
    }
}
