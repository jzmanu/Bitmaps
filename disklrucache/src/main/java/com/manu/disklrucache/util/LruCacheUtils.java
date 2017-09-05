package com.manu.disklrucache.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.MediaController;

import com.manu.disklrucache.io.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 缓存工具类
 */
public class LruCacheUtils{
    private static final String TAG = "LruCacheUtils";
    private static LruCacheUtils lruCacheUtils;

    private Context context;
    private DiskLruCache diskLruCache;  //LRU磁盘缓存
    private LruCache<String,Bitmap> lruCache; //LRU内存缓存

    private LruCacheUtils(){}

    public static LruCacheUtils getInstance(){
        if (lruCacheUtils ==null){
            lruCacheUtils = new LruCacheUtils();
        }
        return lruCacheUtils;
    }

    /**
     * 创建 LruCache、DiskLruCache
     * @param context
     * @param diskCacheDir
     * @param diskCacheSize
     */
    public void createCache(Context context, String diskCacheDir, int diskCacheSize){
        Log.i(TAG,"createCache...");
        try {
            this.context = context;
            /*获取当前activity内存大小*/
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = am.getMemoryClass();
            lruCache = new LruCache<>(memoryClass/8 * 1024 * 1024);//字节  8/1的内存作为缓存大小（通常）

            /*  createCache(File directory, int appVersion, int valueCount, long maxSize)
                第一个参数指定的是数据的缓存地址，
                第二个参数指定当前应用程序的版本号，
                第三个参数指定同一个key可以对应多少个缓存文件，基本都是传1
                第四个参数指定最多可以缓存多少字节的数据,通常10MB
             */
            diskLruCache = DiskLruCache.open(getCacheDir(diskCacheDir), getAppVersion(), 1, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加缓存到内存缓存上
     * @param url
     * @param bitmap
     */
    public void addBitmapToCache(String url,Bitmap bitmap){
        Log.i(TAG,"addBitmapToCache...");
        String key = hashKeyForDisk(url);
        if(getBitmapFromCache(key)==null){
            System.out.println("key===="+key);
            System.out.println("bitmap===="+bitmap);
            lruCache.put(key,bitmap);
        }
    }

    /**
     * 读取内存缓存
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCache(String url){
        Log.i(TAG,"getBitmapFromCache...");
        String key = hashKeyForDisk(url);
        return lruCache.get(key);
    }

    /**
     * 获取磁盘缓存
     * @param url
     * @return
     */
    public InputStream getDiskCache(String url) {
        Log.i(TAG,"getDiskCache...");
        String key = hashKeyForDisk(url);
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot!=null){
                return snapshot.getInputStream(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载图片并缓存到内存和磁盘
     * @param url
     * @param callBack
     */
    public void putCache(final String url, final CallBack callBack){
        Log.i(TAG,"putCache...");
        new AsyncTask<String,Void,Bitmap>(){
            @Override
            protected Bitmap doInBackground(String... params) {
                String key = hashKeyForDisk(params[0]);
                DiskLruCache.Editor editor = null;
                Bitmap bitmap = null;
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(1000 * 30);
                    conn.setConnectTimeout(1000 * 30);
                    ByteArrayOutputStream baos = null;
                    if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                        baos = new ByteArrayOutputStream();
                        byte[] bytes = new byte[1024];
                        int len = -1;
                        while((len=bis.read(bytes))!=-1){
                            baos.write(bytes,0,len);
                        }
                        bis.close();
                        baos.close();
                        conn.disconnect();
                    }
                    if (baos!=null){
                        bitmap = decodeSampledBitmapFromStream(baos.toByteArray(),300,200);
                        addBitmapToCache(params[0],bitmap);//添加到内存缓存
                        editor = diskLruCache.edit(key);
                        /*位图压缩后输出（参数：压缩格式，质量(100表示不压缩，30表示压缩70%)，输出流）*/
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, editor.newOutputStream(0));
                        editor.commit();//提交
                    }
                } catch (IOException e) {
                    try {
                        editor.abort();//放弃写入
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                callBack.response(bitmap);
            }
        }.execute(url);
    }

    /**
     * 关闭磁盘缓存
     */
    public void close(){
        Log.i(TAG,"close...");
        if (diskLruCache!=null && !diskLruCache.isClosed()){
            try {
                diskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 刷新磁盘缓存
     */
    public void flush(){
        Log.i(TAG,"flush...");
        if (diskLruCache!=null){
            try {
                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除内存缓存
     * @param url
     */
    public void removeLruCache(String url){
        Log.i(TAG,"removeLruCache...");
        if (url!=null){
            String key = hashKeyForDisk(url);
            lruCache.remove(key);
        }
    }

    /**
     * 删除磁盘缓存
     * @param url
     */
    public void removeDiskLruCache(String url){
        Log.i(TAG,"removeDiskLruCache...");
        if (url!=null){
            String key = hashKeyForDisk(url);
            try {
                diskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface CallBack<T>{
        void response(T entity);
    }

    /**
     * 计算采样比例
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        //获取位图的原宽高
        int w = options.outWidth;
        int h = options.outHeight;
        int inSampleSize = 1;
        if(w>reqWidth || h>reqHeight){
            if(w>h){
                inSampleSize = Math.round((float)h / (float)reqHeight);
            }else{
                inSampleSize = Math.round((float)w / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

    /**
     * 位图采样
     * @param bytes
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromStream(byte[] bytes,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * 计算url的MD5值作为key
     * @param key
     * @return
     */
    private String hashKeyForDisk(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * 获取版本号
     * @return
     */
    private int getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 获取缓存目录
     * @param name
     * @return
     */
    private File getCacheDir(String name) {
        String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                || !Environment.isExternalStorageRemovable() ?
                context.getExternalCacheDir().getPath() :
                context.getCacheDir().getPath();
        name = cachePath + File.separator + name;
        System.out.println(name);
        return new File(name);
    }
}


















