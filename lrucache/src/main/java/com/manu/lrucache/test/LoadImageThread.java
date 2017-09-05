package com.manu.lrucache.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by lcdn on 2017/8/27 0027.
 */
public class LoadImageThread extends Thread {
    private Activity mActivity;
    private String mImageUrl;
    private ImageLoader mImageLoader;
    private ImageView mImageView;

    public LoadImageThread(Activity activity,ImageLoader imageLoader, ImageView imageView,String imageUrl) {
        this.mActivity = activity;
        this.mImageLoader = imageLoader;
        this.mImageView = imageView;
        this.mImageUrl = imageUrl;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            URL url = new URL(mImageUrl);
            connection = (HttpURLConnection) url.openConnection();
            is = connection.getInputStream();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                mImageLoader.addBitmapToLruCache("bitmap",bitmap);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection!=null){
                connection.disconnect();
            }
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
