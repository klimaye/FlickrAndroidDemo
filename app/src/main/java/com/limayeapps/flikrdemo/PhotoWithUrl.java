package com.limayeapps.flikrdemo;

import com.limayeapps.flikrdemo.flikrapi.PhotoInfo;

import java.io.Serializable;

/**
 * Created by kshitijlimaye on 5/25/15.
 */
public class PhotoWithUrl implements Serializable {
    public String id;
    public String isPublic;
    public String owner;
    public String title;
    public String url;

    public static PhotoWithUrl from(PhotoInfo photoInfo) {
        PhotoWithUrl photoWithUrl = new PhotoWithUrl();
        photoWithUrl.id = photoInfo.id;
        photoWithUrl.owner = photoInfo.owner;
        photoWithUrl.title = photoInfo.title;
        photoWithUrl.isPublic = photoInfo.isPublic;
        return photoWithUrl;
    }
}
