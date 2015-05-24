package com.limayeapps.flikrdemo;

import com.limayeapps.flikrdemo.flikrapi.PhotoInfoResponse;
import com.limayeapps.flikrdemo.flikrapi.PhotoResponse;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.QueryMap;
import rx.Observable;

/**
 * Created by kshitijlimaye on 5/23/15.
 */
public interface FlikrService {
    //https://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=96d5dd087a8932f44a37a98a62afc727&per_page=10&page=1&format=json&api_sig=eae35603a9d97a4e4d00bd60b816b6fa
    @GET("/services/rest/")
    public Observable<PhotoInfoResponse> getInterestingPhotos(@QueryMap Map<String, String> options);

    //ttps://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=1eed361f5df5f48188a9379fbb6a6f2a&photo_id=17776584830&format=json&nojsoncallback=1&api_sig=92b16153acdde6e5f85cb6800c1cc2ba
    @GET("/services/rest/")
    public Observable<PhotoResponse> getPhoto(@QueryMap Map<String, String> options);
}
