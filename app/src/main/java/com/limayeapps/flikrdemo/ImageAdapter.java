package com.limayeapps.flikrdemo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.limayeapps.flikrdemo.flickrapi.PhotoResponse;
import com.squareup.picasso.Picasso;

import org.askerov.dynamicgrid.BaseDynamicGridAdapter;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by kshitijlimaye on 5/30/15.
 */
public class ImageAdapter extends BaseDynamicGridAdapter {
    private Context context;
    private FlickrService service;
    private ArrayList<PhotoWithUrl> photosWithUrls;

    public ImageAdapter(Context context, FlickrService service, ArrayList<PhotoWithUrl> photosWithUrls, int columnCount) {
        super(context, photosWithUrls, columnCount);
        this.context = context;
        this.service = service;
        this.photosWithUrls = photosWithUrls;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final PhotoViewHolder photoViewHolder;
        if (convertView == null) {
            SquaredImageView imageView = new SquaredImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoViewHolder = new PhotoViewHolder(imageView);
            convertView = imageView;
            convertView.setTag(photoViewHolder);
        }
        else {
            photoViewHolder = (PhotoViewHolder)convertView.getTag();
        }
        final PhotoWithUrl info = photosWithUrls.get(i);
        photoViewHolder.build(info, service);
        return convertView;
    }

    private class PhotoViewHolder {
        ImageView imageView;
        private PhotoViewHolder(ImageView view) {
            imageView = view;
        }
        void build(final PhotoWithUrl info, FlickrService service) {
            if (info.url == null) {
                service.getPhoto(FlickrSettings.getPhotoWithId(info.id))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<PhotoResponse>() {
                            @Override
                            public void call(PhotoResponse photoResponse) {
                                info.url = photoResponse.getUrl();
                                Picasso
                                        .with(context)
                                        .load(info.url)
                                        .placeholder(R.drawable.placeholder)
                                        .fit()
                                        .into(imageView);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                Log.i("rxjava", "photoResponse onComplete");
                            }
                        });
            }
            else {
                Log.i("photoViewholder",info.url);
                Picasso.with(context).load(info.url).into(imageView);
            }
        }
    }
}