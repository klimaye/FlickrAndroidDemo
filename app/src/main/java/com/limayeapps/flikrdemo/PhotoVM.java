package com.limayeapps.flikrdemo;

/**
 * Created by kshitijlimaye on 5/23/15.
 */
public class PhotoVM {
    private final String id;
    private final String url;
    private final String title;

    public PhotoVM(String id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}
