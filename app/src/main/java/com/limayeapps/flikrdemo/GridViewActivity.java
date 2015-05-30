package com.limayeapps.flikrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.limayeapps.flikrdemo.flikrapi.PhotoInfo;
import com.limayeapps.flikrdemo.flikrapi.PhotoInfoResponse;
import com.limayeapps.flikrdemo.flikrapi.PhotoResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RestAdapter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;


public class GridViewActivity extends ActionBarActivity {

    private static final String PHOTOS_KEY = "photos";
    @InjectView(R.id.gridView) GridView gridView;
    @InjectView(R.id.toolbar_actionbar) Toolbar toolbar;

    private FlikrService flikrService;
    private Subscription metaInfoSubscription;
    private ArrayList<PhotoWithUrl> photosWithUrls = new ArrayList<>();

    private void setGridViewColumns() {
        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            gridView.setNumColumns(4);
        }
        else {
            gridView.setNumColumns(3);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        ButterKnife.inject(this);
        setGridViewColumns();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Show new activity
                PhotoWithUrl photoWithUrl = photosWithUrls.get(i);
                Intent intent = PhotoActivity.createIntent(GridViewActivity.this, photoWithUrl);
                startActivity(intent);
            }
        });
        if (savedInstanceState != null) {
            photosWithUrls = (ArrayList<PhotoWithUrl>)savedInstanceState.getSerializable(PHOTOS_KEY);
        }
        setupFlickrService();
        initializePhotoStream();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photosWithUrls);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (metaInfoSubscription != null && !metaInfoSubscription.isUnsubscribed()) {
            metaInfoSubscription.unsubscribe();
        }
    }

    private void initializePhotoStream() {
        if (photosWithUrls.size() > 0) {
            this.gridView.setAdapter(new ImageAdapter(this, flikrService));
            return;
        }

        Map<String, String> queryMap = FlikrSettings.getInterestingPhotosQueryMap();
        metaInfoSubscription = flikrService.getInterestingPhotos(queryMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PhotoInfoResponse>() {
                    @Override
                    public void call(PhotoInfoResponse response) {
                        createAndSetAdapter(response.photos.photo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(GridViewActivity.this, R.string.could_not_fetch, Toast.LENGTH_SHORT).show();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });
    }

    private void setupFlickrService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlikrSettings.BASE_URL)
                .build();

        flikrService = restAdapter.create(FlikrService.class);
    }

    private void createAndSetAdapter(List<PhotoInfo> photo) {
        for (PhotoInfo photoInfo : photo) {
            photosWithUrls.add(PhotoWithUrl.from(photoInfo));
        }
        this.gridView.setAdapter(new ImageAdapter(this, flikrService));
    }

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private FlikrService service;

        public ImageAdapter(Context context, FlikrService service) {
            this.context = context;
            this.service = service;
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
}
