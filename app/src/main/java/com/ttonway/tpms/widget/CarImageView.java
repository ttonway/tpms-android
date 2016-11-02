package com.ttonway.tpms.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by ttonway on 2016/11/2.
 */
public class CarImageView extends ImageView {
    private static final String TAG = CarImageView.class.getSimpleName();

    private final AspectRatioMeasure.Spec mMeasureSpec = new AspectRatioMeasure.Spec();

    public CarImageView(Context context) {
        super(context);
    }

    public CarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure");
        float aspectRatio = 1.f;
        Drawable drawable = getDrawable();
        if (drawable != null) {
            aspectRatio = (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
        }

        mMeasureSpec.width = widthMeasureSpec;
        mMeasureSpec.height = heightMeasureSpec;
        AspectRatioMeasure.updateMeasureSpec(
                mMeasureSpec,
                aspectRatio,
                getLayoutParams(),
                getPaddingLeft() + getPaddingRight(),
                getPaddingTop() + getPaddingBottom());
        super.onMeasure(mMeasureSpec.width, mMeasureSpec.height);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int margin = -getMeasuredWidth() * 67 / 342;
        if (lp.leftMargin != margin || lp.rightMargin != margin) {
            lp.leftMargin = margin;
            lp.rightMargin = margin;
            setLayoutParams(lp);
        }
    }
}
