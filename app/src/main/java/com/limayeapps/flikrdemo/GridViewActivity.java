package com.limayeapps.flikrdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.limayeapps.flikrdemo.flikrapi.PhotoInfo;
import com.limayeapps.flikrdemo.flikrapi.PhotoInfoResponse;

import org.askerov.dynamicgrid.DynamicGridUtils;
import org.askerov.dynamicgrid.DynamicGridView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action0;
import rx.functions.Action1;


public class GridViewActivity extends ActionBarActivity {

    private static final String PHOTOS_KEY = "photos";
    private static final String SEARCH_TERM_KEY = "search_term";

    @InjectView(R.id.gridView) DynamicGridView gridView;
    @InjectView(R.id.search_text) EditText editText;

    private FlikrService flikrService;
    private Subscription metaInfoSubscription;
    private ArrayList<PhotoWithUrl> photosWithUrls = new ArrayList<>();

    private String searchTerm = "";
    private int currentcolumnCount = 3;
    private void setGridViewColumns() {
        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            gridView.setNumColumns(4);
            currentcolumnCount = 4;
        }
        else {
            gridView.setNumColumns(3);
            currentcolumnCount = 3;
        }
    }

    @OnClick(R.id.search_button)
    public void onClick(View view) {
        if (searchTerm.isEmpty() || searchTerm.trim().length() == 0) {
            Toast.makeText(GridViewActivity.this, R.string.enter_search_term_toast,Toast.LENGTH_SHORT)
                    .show();
        }
        getMetaInfoFor(getSearchTermPhotosObservable());
    }

    private void setupGridView() {
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
        gridView.setOnDropListener(new DynamicGridView.OnDropListener() {
            @Override
            public void onActionDrop() {
                gridView.stopEditMode();
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                gridView.startEditMode();
                return true;
            }
        });
        gridView.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int i) {

            }

            @Override
            public void onDragPositionsChanged(int from, int to) {
                Log.i("onDragPositionsChanged", String.format("changing from %d to %d", from, to));
                DynamicGridUtils.swap(photosWithUrls, from, to);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (gridView.isEditMode()) {
            gridView.stopEditMode();
        }
        else {
            super.onBackPressed();
        }
    }

    private void setupFlickrService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FlikrSettings.BASE_URL)
                .build();

        flikrService = restAdapter.create(FlikrService.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
        ButterKnife.inject(this);
        setupGridView();
        setupFlickrService();
        WidgetObservable.text(editText).subscribe(new Action1<OnTextChangeEvent>() {
            @Override
            public void call(OnTextChangeEvent event) {
                searchTerm = event.text().toString();
            }
        });
        if (savedInstanceState != null) {
            searchTerm = savedInstanceState.getString(SEARCH_TERM_KEY);
            photosWithUrls = (ArrayList<PhotoWithUrl>)savedInstanceState.getSerializable(PHOTOS_KEY);
        }
        initializePhotoStream();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TERM_KEY, searchTerm);
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
            this.gridView.setAdapter(new ImageAdapter(this, flikrService, photosWithUrls, currentcolumnCount));
        }
        else {
            getMetaInfoFor(getIntestingPhotosObservable());
        }
    }

    private void createAndSetAdapter(List<PhotoInfo> photo) {
        photosWithUrls.clear();
        for (PhotoInfo photoInfo : photo) {
            photosWithUrls.add(PhotoWithUrl.from(photoInfo));
        }
        this.gridView.setAdapter(new ImageAdapter(this, flikrService, photosWithUrls, currentcolumnCount));
    }

    private void getMetaInfoFor(Observable<PhotoInfoResponse> observable) {
        metaInfoSubscription =
                observable
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
                        Log.i("rxjava","metaInfo onComplete");
                    }
                });
    }

    private Observable<PhotoInfoResponse> getIntestingPhotosObservable() {
        Map<String, String> queryMap = FlikrSettings.getInterestingPhotosQueryMap();
        return flikrService.getInterestingPhotos(queryMap);
    }

    private Observable<PhotoInfoResponse> getSearchTermPhotosObservable() {
        Map<String, String> queryMap = FlikrSettings.getPhotosForSearchTerm(this.searchTerm);
        return flikrService.getSearchTermPhotos(queryMap);
    }
}
