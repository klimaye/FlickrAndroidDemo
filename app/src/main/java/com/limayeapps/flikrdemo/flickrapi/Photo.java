package com.limayeapps.flikrdemo.flickrapi;

/**
 * Created by kshitijlimaye on 5/23/15.
 */
public class Photo {
    public String id;
    public String secret;
    public String server;
    public int farm;

    public String getUrl() {
        String url = String.format("https://farm%d.staticflickr.com/%s/%s_%s_%s.jpg",
                farm,server,id,secret,"z");
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;

        return id.equals(photo.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", server='" + server + '\'' +
                ", farm=" + farm +
                '}';
    }
}
