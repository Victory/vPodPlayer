package org.dfhu.vpodplayer.model;

public class Show {
    public int id;
    public String title;
    public String description;
    public String url;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Show)) {
            return false;
        }
        return ((Show) o).url.equals(this.url);
    }
}
