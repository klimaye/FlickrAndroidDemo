package com.limayeapps.flikrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class PhotoActivity extends ActionBarActivity {

    public static Intent createIntent(Context context, PhotoWithUrl photoWithUrl) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra("url",photoWithUrl.url);
        intent.putExtra("title",photoWithUrl.title);
        return intent;
    }

    @InjectView(R.id.image) ImageView imageView;
    @InjectView(R.id.title) TextView title;

    private String getUrl() {
        return this.getIntent().getStringExtra("url");
    }

    private String getImageTitle() {
        return this.getIntent().getStringExtra("title");
    }

    private String getImageOwner() {
        return this.getIntent().getStringExtra("owner");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Photo Details");
        setContentView(R.layout.activity_photo);
        ButterKnife.inject(this);
        title.setText(getImageTitle());
        Picasso.with(this).load(getUrl()).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
