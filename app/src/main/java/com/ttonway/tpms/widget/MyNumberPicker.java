package com.ttonway.tpms.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ttonway.tpms.R;

/**
 * Created by ttonway on 2017/1/15.
 */

public class MyNumberPicker extends LinearLayout {

    TextView mTextView;
    ImageButton mAddBtn;
    ImageButton mMinusBtn;

    float mValue;
    float mDeltaValue;
    float mMax;
    float mMin;

    ValueFormatter mValueFormatter;

    public MyNumberPicker(Context context) {
        this(context, null);
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(true);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.numberpicker_layout, this, true);

        mTextView = (TextView) findViewById(R.id.text_center);
        mAddBtn = (ImageButton) findViewById(R.id.btn_add);
        mMinusBtn = (ImageButton) findViewById(R.id.btn_minus);
        mAddBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseOne();
            }
        });
        mMinusBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseOne();
            }
        });

    }

    public ValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    public void setValueFormatter(ValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;

        updateValue();
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        this.mValue = value;

        updateValue();
    }

    public void initialize(float delta, float min, float max) {
        mDeltaValue = delta;
        mMin = min;
        mMax = max;
    }

    private void increaseOne() {
        mValue += mDeltaValue;
        mValue = Math.min(mValue, mMax);

        updateValue();
    }

    private void decreaseOne() {
        mValue -= mDeltaValue;
        mValue = Math.max(mValue, mMin);

        updateValue();
    }


    private void updateValue() {
        if (mValueFormatter == null) {
            mTextView.setText(String.valueOf(mValue));
        } else {
            mTextView.setText(mValueFormatter.getHint(mValue));
        }
    }

    public interface ValueFormatter {
        String getHint(float value);
    }

}
