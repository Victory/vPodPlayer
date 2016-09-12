package org.dfhu.vpodplayer.feed;

public class EpisodeInfo {
    private String title;
    private String description;
    private float percentListened;
    // size of episode in bytes
    private int size;
    // length of episode in seconds
    private int length;
    private String link;

    private String id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPercentListened() {
        return percentListened;
    }

    public void setPercentListened(float percentListened) {
        this.percentListened = percentListened;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
