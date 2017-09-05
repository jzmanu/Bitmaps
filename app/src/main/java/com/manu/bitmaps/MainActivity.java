package com.manu.bitmaps;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {
    private Button btnSampleLoad;
    private Button btnCalculateLoad;
    private Button btnOriginalWH;
    private TextView tvOriginalWidth;
    private TextView tvOriginalHeight;
    private TextView tvSampleWidth;
    private TextView tvSampleHeight;
    private TextView tvSampleSize;
    private TextView tvBitmapType;
    private TextView tvSampleMemory;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        btnSampleLoad = (Button) findViewById(R.id.btnSampleLoad);
        btnCalculateLoad = (Button) findViewById(R.id.btnCalculateLoad);
        btnOriginalWH = (Button) findViewById(R.id.btnOriginalWH);
        tvOriginalWidth = (TextView) findViewById(R.id.tvOriginalWidth);
        tvOriginalHeight = (TextView) findViewById(R.id.tvOriginalHeight);
        tvSampleWidth = (TextView) findViewById(R.id.tvSampleWidth);
        tvSampleHeight = (TextView) findViewById(R.id.tvSampleHeight);
        tvSampleSize = (TextView) findViewById(R.id.tvSampleSize);
        tvBitmapType = (TextView) findViewById(R.id.tvOriginalMemory);
        tvSampleMemory = (TextView) findViewById(R.id.tvSampleMemory);

        btnSampleLoad.setOnClickListener(this);
        btnCalculateLoad.setOnClickListener(this);
        btnOriginalWH.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        System.out.println("density:" + dm.density);
        System.out.println("scaledDensity:" + dm.scaledDensity);
        System.out.println("densityDpi:" + dm.densityDpi);
        System.out.println("xdpi:" + dm.xdpi);
        System.out.println("ydpi:" + dm.ydpi);
    }

    /**
     * 获取原始图片的宽高信息
     *
     * @param res
     * @param resId
     */
    private BitmapFactory.Options getOriginalWHOptions(Resources res, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);
        return options;
    }

    /**
     * 位图采样
     * @param res
     * @param resId
     * @return
     */
    public Bitmap decodeSampleFromResource(Resources res, int resId) {
        //BitmapFactory创建设置选项】
        BitmapFactory.Options options = new BitmapFactory.Options();
        //采样比例
        options.inSampleSize = 200;
//        options.inDensity = 480;

        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        System.out.println("采样后的宽度：" + options.outWidth);
        System.out.println("采样后的高度：" + options.outHeight);
        System.out.println("位图像素所占的内存：" + bitmap.getByteCount());

        tvSampleWidth.setText(String.valueOf(options.outWidth));
        tvSampleHeight.setText(String.valueOf(options.outHeight));
        tvSampleSize.setText(String.valueOf(options.inSampleSize));
        tvSampleMemory.setText(String.valueOf(bitmap.getByteCount()));
        tvBitmapType.setText(String.valueOf(bitmap.getConfig().toString()));
        return bitmap;
    }

    /**
     * 位图采样
     * @param resources
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap decodeSampleFromResource(Resources resources, int resId, int reqWidth, int reqHeight) {
        //创建一个位图工厂的设置选项
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置该属性为true,解码时只能获取width、height、mimeType
        options.inJustDecodeBounds = true;
        //解码
        BitmapFactory.decodeResource(resources, resId, options);
        //计算采样比例
        int inSampleSize = options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);
        //设置该属性为false，实现真正解码
        options.inJustDecodeBounds = false;
        //解码
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resId, options);
        System.out.println("采样比例：" + inSampleSize);
        System.out.println("采样之后的宽度：" + options.outWidth);
        System.out.println("采样之后的高度：" + options.outHeight);

        tvSampleWidth.setText(String.valueOf(options.outWidth));
        tvSampleHeight.setText(String.valueOf(options.outHeight));
        tvSampleMemory.setText(String.valueOf(bitmap.getByteCount()));
        tvBitmapType.setText(String.valueOf(bitmap.getConfig().toString()));

        return bitmap;
    }

    /**
     * 计算位图采样比例
     * @param option
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int calculateSampleSize(BitmapFactory.Options option, int reqWidth, int reqHeight) {
        //获得图片的原宽高
        int width = option.outWidth;
        int height = option.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }

        tvSampleSize.setText(String.valueOf(inSampleSize));
        return inSampleSize;
    }

    /**
     * 计算位图采样比例
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int calculateSampleSize1(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        //获得图片的原宽高
        int height = options.outHeight;
        int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            /**
             * 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
             * 一定都会大于等于目标的宽和高。
             */
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSampleLoad:
                loadImage(getResources(), R.drawable.sun);
                break;
            case R.id.btnCalculateLoad:
                loadImage(getResources(), R.drawable.sun, 100, 100);
                break;
            case R.id.btnOriginalWH:
                showOriginalWH();
                break;
        }
    }

    private void loadImage(Resources res, int resId) {
        Bitmap bitmap = decodeSampleFromResource(res, resId);
        imageView.setImageBitmap(bitmap);
    }

    private void loadImage(Resources res, int resId, int reqWidth, int reqHeight) {
        Bitmap bitmap = decodeSampleFromResource(res, resId, reqWidth, reqHeight);
        imageView.setImageBitmap(bitmap);
    }

    private void showOriginalWH() {
        int width = getOriginalWHOptions(getResources(),R.drawable.sun).outWidth;
        int height = getOriginalWHOptions(getResources(),R.drawable.sun).outHeight;
        tvOriginalWidth.setText(String.valueOf(width));
        tvOriginalHeight.setText(String.valueOf(height));
    }
}
