package com.manu.lrucache.test;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by lcdn on 2017/8/27 0027.
 */
public class ImageLoader {
    private LruCache<String , Bitmap> lruCache;
    public ImageLoader() {
        int memorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = memorySize / 8;
        lruCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //计算每一个缓存Bitmap的所占内存的大小
                return value.getByteCount()/1024;
            }
        };
    }

    /**
     * 添加Bitmapd到LruCache中
     * @param key
     * @param bitmap
     */
    public void addBitmapToLruCache(String key, Bitmap bitmap){
        if (getBitmapFromLruCache(key)==null){
            lruCache.put(key,bitmap);
        }
    }

    /**
     * 获取缓存的Bitmap
     * @param key
     */
    public Bitmap getBitmapFromLruCache(String key){
        if (key!=null){
            return lruCache.get(key);
        }
        return null;
    }

    /**
     * 移出缓存
     * @param key
     */
    public void removeBitmapFromLruCache(String key){
        if (key!=null){
            lruCache.remove(key);
        }
    }
}
