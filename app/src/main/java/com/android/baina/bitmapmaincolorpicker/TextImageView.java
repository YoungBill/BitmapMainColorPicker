package com.android.baina.bitmapmaincolorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by taochen on 18-8-16.
 */

public class TextImageView extends FrameLayout {

    private static final String TAG = TextImageView.class.getSimpleName();

    private ImageView mIv;
    private TextView mTv;
    private Drawable mDrawable;
    private Handler mUiHandler;

    public TextImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mUiHandler = new Handler();
        TypedArray t = getResources().obtainAttributes(attrs, R.styleable.TextImageView);
        mDrawable = t.getDrawable(R.styleable.TextImageView_imageSrc);
        t.recycle();
        LayoutInflater.from(context).inflate(R.layout.layout_textimage, this);
        mIv = findViewById(R.id.iv);
        mIv.setImageDrawable(mDrawable);
        mTv = findViewById(R.id.tv);
        calculateTextColor();
    }

    private void calculateTextColor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = drawable2Bitmap(mDrawable);
                if (bitmap == null)
                    return;
                final float average = mainColorPicker(bitmap);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateTextColor(average);
                    }
                });
            }
        }).start();
    }

    private void updateTextColor(float average) {
        int defaultColor = getResources().getColor(R.color.text_color_default);
        int grayColor = getResources().getColor(R.color.text_color_gray);
        if (average > AppConstants.COLOR_VALUE_WHITE) {
            // main color of the picture is white,so text color should not use white
            mTv.setTextColor(grayColor);
        } else {
            // main color of the picture is not white,so text color can use white
            mTv.setTextColor(defaultColor);
        }
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * get main color for bitmap
     *
     * @param bitmap
     * @return
     */
    private float mainColorPicker(Bitmap bitmap) {
        Rect clipRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        double sum = 0;
        int step = 5;
        int count = 0;
        for (int i = clipRect.left; i < clipRect.right; i += step) {
            for (int j = clipRect.top; j < clipRect.bottom; j += step) {
                if (bitmap.isRecycled()) {
                    return 0;
                }
                int pixel = bitmap.getPixel(i, j);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                double f = r * 0.299 + g * 0.587 + b * 0.114;
                sum += f;
                count++;
            }
        }
        return (float) (sum / count);
    }

}
