package com.limayeapps.flikrdemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.limayeapps.flikrdemo.flikrapi.PhotoResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by kshitijlimaye on 5/30/15.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private FlikrService service;
    private List<PhotoWithUrl> photosWithUrls;

    public ImageAdapter(Context context, FlikrService service, List<PhotoWithUrl> photosWithUrls) {
        this.context = context;
        this.service = service;
        this.photosWithUrls = photosWithUrls;
    }

    @Override
    public int getCount() {
        return photosWithUrls.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final SquaredImageView imageView;
        if (view == null) {
            imageView = new SquaredImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            imageView = (SquaredImageView)view;
        }
        final PhotoWithUrl info = photosWithUrls.get(i);
        if (info.url == null) {
            service.getPhoto(FlikrSettings.getPhotoWithId(info.id))
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
                    });
        }
        else {
            Picasso.with(context).load(info.url).into(imageView);
        }
        return imageView;
    }
}