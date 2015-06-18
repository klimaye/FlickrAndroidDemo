package com.limayeapps.flikrdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.limayeapps.flikrdemo.flickrapi.PhotoInfo;
import com.limayeapps.flikrdemo.flickrapi.PhotoInfoResponse;

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
import rx.schedulers.Schedulers;


public class GridViewActivity extends ActionBarActivity {

    private static final String PHOTOS_KEY = "photos";
    private static final String SEARCH_TERM_KEY = "search_term";
    private static final String MAX_PAGE_KEY = "max_page_key";

    @InjectView(R.id.gridView) DynamicGridView gridView;
    @InjectView(R.id.search_text) EditText editText;

    private FlickrService flickrService;
    private Subscription metaInfoSubscription;
    private ArrayList<PhotoWithUrl> photosWithUrls = new ArrayList<>();

    private String searchTerm = "";
    private int currentcolumnCount = 3;
    private int maxPage = 1;
    private int lastTotalCount = 0;

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
        this.maxPage = 1;
        getMetaInfoFor(getSearchTermPhotosObservable(maxPage), new Action1<List<PhotoInfo>>() {
            @Override
            public void call(List<PhotoInfo> photoInfos) {
                createAndSetAdapter(photoInfos);
            }
        });
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
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == lastTotalCount) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    lastTotalCount = totalItemCount;
                    maxPage += 1;
                    Log.e("scrollMax", "about to get more photo infos " + maxPage);
                    getMetaInfoFor(getSearchTermPhotosObservable(maxPage), new Action1<List<PhotoInfo>>() {
                        @Override
                        public void call(final List<PhotoInfo> photoInfos) {
                            updateAdapter(photoInfos);
                        }
                    });
                }
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
                .setEndpoint(FlickrSettings.BASE_URL)
                .build();

        flickrService = restAdapter.create(FlickrService.class);
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
            maxPage = savedInstanceState.getInt(MAX_PAGE_KEY);
            searchTerm = savedInstanceState.getString(SEARCH_TERM_KEY);
            photosWithUrls = (ArrayList<PhotoWithUrl>)savedInstanceState.getSerializable(PHOTOS_KEY);
        }
        initializePhotoStream();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MAX_PAGE_KEY, maxPage);
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
            this.gridView.setAdapter(new ImageAdapter(this, flickrService, photosWithUrls, currentcolumnCount));
        }
    }

    private void updateAdapter(List<PhotoInfo> photos) {
        ImageAdapter adapter = ((ImageAdapter)gridView.getAdapter());
        int startIndex = photosWithUrls.size();
        for (PhotoInfo photoInfo : photos) {
            PhotoWithUrl pUrl = PhotoWithUrl.from(photoInfo);
            photosWithUrls.add(pUrl);
            adapter.add(startIndex++, pUrl);
        }
        adapter.notifyDataSetChanged();
        gridView.invalidateViews();
    }

    private void createAndSetAdapter(List<PhotoInfo> photo) {
        photosWithUrls.clear();
        for (PhotoInfo photoInfo : photo) {
            photosWithUrls.add(PhotoWithUrl.from(photoInfo));
        }
        this.gridView.setAdapter(new ImageAdapter(this, flickrService, photosWithUrls, currentcolumnCount));
    }

    private void getMetaInfoFor(Observable<PhotoInfoResponse> observable, final Action1<List<PhotoInfo>> action) {
        metaInfoSubscription =
                observable
                        .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PhotoInfoResponse>() {
                    @Override
                    public void call(PhotoInfoResponse response) {
                        action.call(response.photos.photo);
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

    private Observable<PhotoInfoResponse> getIntestingPhotosObservable(int pageNumber) {
        Map<String, String> queryMap = FlickrSettings.getInterestingPhotosQueryMap(pageNumber);
        return flickrService.getInterestingPhotos(queryMap);
    }

    private Observable<PhotoInfoResponse> getSearchTermPhotosObservable(int pageNumber) {
        Map<String, String> queryMap = FlickrSettings.getPhotosForSearchTerm(this.searchTerm, pageNumber);
        return flickrService.getSearchTermPhotos(queryMap);
    }
}
