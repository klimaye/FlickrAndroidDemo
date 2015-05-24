package com.limayeapps.flikrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.limayeapps.flikrdemo.flikrapi.PhotoInfo;
import com.limayeapps.flikrdemo.flikrapi.PhotoInfoResponse;
import com.limayeapps.flikrdemo.flikrapi.PhotoResponse;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RestAdapter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;


public class GridViewActivity extends Activity {

    @InjectView(R.id.gridView) GridView gridView;
    private FlikrService flikrService;
    private Subscription metaInfoSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        ButterKnife.inject(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Show new activity
                Intent intent = new Intent(GridViewActivity.this, PhotoActivity.class);
                startActivity(intent);
            }
        });
        initializePhotoStream();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (metaInfoSubscription != null && !metaInfoSubscription.isUnsubscribed()) {
            metaInfoSubscription.unsubscribe();
        }
    }

    private void initializePhotoStream() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlikrSettings.BASE_URL)
                .build();

        flikrService = restAdapter.create(FlikrService.class);

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

    private void createAndSetAdapter(List<PhotoInfo> photo) {
        this.gridView.setAdapter(new ImageAdapter(this,photo,flikrService));
    }

    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<PhotoInfo> metaInfo;
        private FlikrService service;

        public ImageAdapter(Context context, List<PhotoInfo> metaInfo, FlikrService service) {
            this.context = context;
            this.metaInfo = metaInfo;
            this.service = service;
        }

        @Override
        public int getCount() {
            return metaInfo.size();
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
            final ImageView imageView;
            if (view == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8,8,8,8);
            }
            else {
                imageView = (ImageView)view;
            }
            PhotoInfo info = metaInfo.get(i);
            service.getPhoto(FlikrSettings.getPhotoWithId(info.id))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<PhotoResponse>() {
                        @Override
                        public void call(PhotoResponse photoResponse) {
                            Picasso.with(context).load(photoResponse.getUrl()).into(imageView);
                        }
                    });
            return imageView;
        }
    }
}
