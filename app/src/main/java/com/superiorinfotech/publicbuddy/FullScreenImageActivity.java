package com.superiorinfotech.publicbuddy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import it.sephiroth.android.library.imagezoom.ImageViewTouch;


public class FullScreenImageActivity extends Activity {
    String mImagePath;
    ImageViewTouch mFullScreenImage;
    ProgressBar mProgressBar;
    private DisplayImageOptions imageOptions;
    ImageLoader imageLoader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        if(intent.getExtras().containsKey("path")){
            mImagePath = intent.getExtras().getString("path");
        }
        imageOptions = new DisplayImageOptions.Builder()

                .showImageForEmptyUri(R.drawable.ribbon_image)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
//                .decodingOptions(resizeOptions)
//                .postProcessor(new BitmapProcessor() {
//                    @Override
//                    public Bitmap process(Bitmap bmp) {
//                        return Bitmap.createScaledBitmap(bmp, 300, 300, false);
//                    }
//                }).build();

        ImageLoaderConfiguration config =new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(imageOptions)
                .build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        mFullScreenImage = (ImageViewTouch)findViewById(R.id.image_full_screen);
        mProgressBar=(ProgressBar)findViewById(R.id.progress_full_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();

        imageLoader.displayImage(mImagePath, mFullScreenImage,imageOptions,new SimpleImageLoadingListener(){
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_screen_image, menu);
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
