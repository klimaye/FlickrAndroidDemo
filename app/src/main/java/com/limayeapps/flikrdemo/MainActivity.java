package com.limayeapps.flikrdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.limayeapps.flikrdemo.flickrapi.PhotoInfo;
import com.limayeapps.flikrdemo.flickrapi.PhotoInfoResponse;
import com.limayeapps.flikrdemo.flickrapi.PhotoResponse;
import com.squareup.picasso.Callback;
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

public class MainActivity extends Activity {

    private List<PhotoInfo> metaInfoList;
    private FlikrAdapter adapter;
    @InjectView(R.id.list)
    RecyclerView listView;

    private Subscription metaInfoSubscription;
    private FlickrService flickrService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        metaInfoList = new ArrayList<>();
        listView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initializePhotoStream() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlickrSettings.BASE_URL)
                .build();

        flickrService = restAdapter.create(FlickrService.class);

        Map<String, String> queryMap = FlickrSettings.getInterestingPhotosQueryMap();

        metaInfoSubscription = flickrService.getInterestingPhotos(queryMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PhotoInfoResponse>() {
                    @Override
                    public void call(PhotoInfoResponse response) {
                        createAdapter(response.photos.photo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(MainActivity.this, R.string.could_not_fetch, Toast.LENGTH_SHORT).show();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });
    }

    private void createAdapter(List<PhotoInfo> photoInfos) {
        adapter = new FlikrAdapter(this, photoInfos, flickrService);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.initializePhotoStream();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.metaInfoSubscription != null && !metaInfoSubscription.isUnsubscribed()) {
            metaInfoSubscription.unsubscribe();
        }
    }

    public static class FlikrAdapter extends RecyclerView.Adapter<PhotoVH> {

        private List<PhotoInfo> photoList;
        private Context context;
        private FlickrService flickrService;

        public FlikrAdapter(Context context, List<PhotoInfo> list, FlickrService flickrService) {
            photoList = list;
            this.context = context;
            this.flickrService = flickrService;
        }

        @Override
        public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new PhotoVH(inflater.inflate(R.layout.photo_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final PhotoVH holder, int position) {
            PhotoInfo item = photoList.get(position);
            flickrService.getPhoto(FlickrSettings.getPhotoWithId(item.id))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<PhotoResponse>() {
                        @Override
                        public void call(PhotoResponse photoResponse) {
                            String actualUrl = photoResponse.getUrl();

                            Picasso.with(context)
                                    .load(actualUrl)
                                    .into(holder.imageView, new Callback.EmptyCallback() {
                                        @Override
                                        public void onSuccess() {
                                            super.onSuccess();
                                            
                                        }
                                    });
                        }
                    });
            holder.imageTitle.setText(item.title);
        }

        @Override
        public int getItemCount() {
            return photoList.size();
        }
    }

    public static class PhotoVH extends RecyclerView.ViewHolder {
        @InjectView(R.id.imageView)
        ImageView imageView;
        @InjectView(R.id.imageTitle)
        TextView imageTitle;

        public PhotoVH(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
