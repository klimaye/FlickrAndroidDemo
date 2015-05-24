package com.limayeapps.flikrdemo.flikrapi;

/**
 * Created by kshitijlimaye on 5/23/15.
 */
public class PhotoInfo {
    public String id;
    public String isPublic;
    public String owner;
    public String title;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoInfo photoInfo = (PhotoInfo) o;

        if (!id.equals(photoInfo.id)) return false;
        return owner.equals(photoInfo.owner);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PhotoInfo{" +
                "id='" + id + '\'' +
                ", isPublic='" + isPublic + '\'' +
                ", owner='" + owner + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
