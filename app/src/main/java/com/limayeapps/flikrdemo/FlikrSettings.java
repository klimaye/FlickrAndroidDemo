package com.limayeapps.flikrdemo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kshitijlimaye on 5/23/15.
 */
public class FlikrSettings {
    public static final String API_KEY = "";
    public static final String BASE_URL = "https://api.flickr.com";
    public static final String REST_ENDPOINT = "/services/rest/";

    public static Map<String, String> getInterestingPhotosQueryMap() {
        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("method","flickr.interestingness.getList");
        queryMap.put("api_key",FlikrSettings.API_KEY);
        queryMap.put("per_page","500");
        queryMap.put("page",String.valueOf(1));
        queryMap.put("format","json");
        queryMap.put("nojsoncallback","1");
        return queryMap;
    }

    public static Map<String, String> getPhotoWithId(String photoId) {
        Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("method","flickr.photos.getInfo");
        queryMap.put("api_key",FlikrSettings.API_KEY);
        queryMap.put("photo_id",photoId);
        queryMap.put("format","json");
        queryMap.put("nojsoncallback","1");
        return queryMap;
    }

}
