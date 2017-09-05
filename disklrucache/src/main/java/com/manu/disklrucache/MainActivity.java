package com.manu.disklrucache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.manu.disklrucache.util.LruCacheUtils;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "cache_test";
    public static String CACHE_DIR = "diskCache";  //缓存目录
    public static int CACHE_SIZE = 1024 * 1024 * 10; //缓存大小
    private ImageView imageView;
    private LruCache<String, String> lruCache;
    private LruCacheUtils cacheUtils;
    private String url = "http://img06.tooopen.com/images/20161012/tooopen_sy_181713275376.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cacheUtils = LruCacheUtils.getInstance();
        //创建内存缓存和磁盘缓存
        cacheUtils.createCache(this,CACHE_DIR,CACHE_SIZE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cacheUtils.flush();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cacheUtils.close();
    }

    public void loadImage(View view){
        load(url,imageView);
    }
    public void removeLruCache(View view){
        Log.i(TAG, "移出内存缓存...");
        cacheUtils.removeLruCache(url);
    }
    public void removeDiskLruCache(View view){
        Log.i(TAG, "移出磁盘缓存...");
        cacheUtils.removeDiskLruCache(url);
    }
    private void load(String url, final ImageView imageView){
        //从内存中获取图片
        Bitmap bitmap = cacheUtils.getBitmapFromCache(url);
        if (bitmap == null){
            //从磁盘中获取图片
            InputStream is = cacheUtils.getDiskCache(url);
            if (is == null){
                //从网络上获取图片
                cacheUtils.putCache(url, new LruCacheUtils.CallBack<Bitmap>() {
                    @Override
                    public void response(Bitmap bitmap1) {
                        Log.i(TAG, "从网络中获取图片...");
                        Log.i(TAG, "正在从网络中下载图片...");
                        imageView.setImageBitmap(bitmap1);
                        Log.i(TAG, "从网络中获取图片成功...");
                    }
                });
            }else{
                Log.i(TAG, "从磁盘中获取图片...");
                bitmap = BitmapFactory.decodeStream(is);
                imageView.setImageBitmap(bitmap);
            }
        }else{
            Log.i(TAG, "从内存中获取图片...");
            imageView.setImageBitmap(bitmap);
        }
    }
}
