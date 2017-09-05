package com.manu.lrucache.test;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.manu.lrucache.R;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener{
    private static final String TAG = "bitmap";
    private Button btnLoadLruCache;
    private Button btnRemoveBitmapL;
    private ImageView imageView;
    private ImageLoader imageLoader;
    private String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=" +
            "1503825301140&di=83e69431181133991318a40bc02eff0f&imgtype=0&src=http%3A%2F%2Fimg" +
            ".sc115.com%2Fuploads%2Fsc%2Fjpgs%2F0211apic225_sc115.com.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoadLruCache = (Button) findViewById(R.id.btnLoadLruCache);
        btnRemoveBitmapL = (Button) findViewById(R.id.btnRemoveBitmapL);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnLoadLruCache.setOnClickListener(this);
        btnRemoveBitmapL.setOnClickListener(this);
        imageLoader = new ImageLoader();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLoadLruCache:
                loadImage();
                break;
            case R.id.btnRemoveBitmapL:
                removeBitmapFromL("bitmap");
                break;
        }
    }

    //加载图片
    private void loadImage(){
        Bitmap bitmap = imageLoader.getBitmapFromLruCache("bitmap");
       if (bitmap==null){
           Log.i(TAG,"从网络获取图片");
           new LoadImageThread(this,imageLoader,imageView,url).start();
       }else{
           Log.i(TAG,"从缓存中获取图片");
           imageView.setImageBitmap(bitmap);
       }
    }

    // 移出缓存
    private void removeBitmapFromL(String key){
        imageLoader.removeBitmapFromLruCache(key);
    }
}
