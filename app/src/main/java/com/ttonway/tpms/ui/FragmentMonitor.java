package com.ttonway.tpms.ui;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.R;
import com.ttonway.tpms.core.TireStatus;
import com.ttonway.tpms.core.TireStatusUpdatedEvent;
import com.ttonway.tpms.core.TpmsDevice;
import com.ttonway.tpms.utils.Utils;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentMonitor extends BaseFragment {

    @BindViews({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    List<LinearLayout> mBoards;
    @BindViews({R.id.battery1, R.id.battery3, R.id.battery4, R.id.battery2})
    List<ImageView> mBatteryImageViews;
    @BindViews({R.id.pressure1, R.id.pressure3, R.id.pressure4, R.id.pressure2})
    List<TextView> mPressureTextViews;
    @BindViews({R.id.temperature1, R.id.temperature3, R.id.temperature4, R.id.temperature2})
    List<TextView> mTempTextViews;

    private Unbinder mUnbinder;

    public static final String NO_VALUE = "------";

    @Subscribe
    public void onStatusUpdated(final TireStatusUpdatedEvent e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        });
    }

    void refreshUI() {
        if (mBoards == null || mBoards.size() != 4) {
            return;
        }

        TpmsDevice device = getTpmeDevice();
        for (byte tire = TpmsDevice.TIRE_LEFT_FRONT; tire <= TpmsDevice.TIRE_LEFT_END; tire++) {
            TireStatus tireStatus = device.getTireStatus(tire);
            LinearLayout board = mBoards.get(tire);
            TextView pressureText = mPressureTextViews.get(tire);
            TextView tempText = mTempTextViews.get(tire);
            ImageView batteryImage = mBatteryImageViews.get(tire);

            if (tireStatus == null || !tireStatus.inited) {

                if (tireStatus.pressureStatus == TireStatus.PRESSURE_NO_SIGNAL) {
                    pressureText.setText(R.string.label_no_signal);
                    pressureText.setSelected(true);
                } else {
                    pressureText.setText(NO_VALUE);
                    pressureText.setSelected(false);
                }
                tempText.setText(NO_VALUE);
                tempText.setSelected(false);
                batteryImage.getDrawable().setLevel(0);
                batteryImage.setSelected(false);
                board.setSelected(false);
            } else {
                board.setSelected(tireStatus.pressureStatus != TireStatus.PRESSURE_NORMAL
                        || tireStatus.temperatureStatus != TireStatus.TEMPERATURE_NORMAL
                        || tireStatus.batteryStatus != TireStatus.BATTERY_NORMAL);
                if (tireStatus.pressureStatus == TireStatus.PRESSURE_ERROR) {
                    pressureText.setText(NO_VALUE);
                } else if (tireStatus.pressureStatus == TireStatus.PRESSURE_LEAKING) {
                    pressureText.setText(R.string.label_leaking);
                } else if (tireStatus.pressureStatus == TireStatus.PRESSURE_NO_SIGNAL) {
                    pressureText.setText(R.string.label_no_signal);
                } else {
                    pressureText.setText(Utils.formatPressure(getActivity(), tireStatus.getPressure()));
                }
                pressureText.setSelected(tireStatus.pressureStatus != TireStatus.PRESSURE_NORMAL);
                tempText.setText(Utils.formatTemperature(getActivity(), tireStatus.getTemperature()));
                tempText.setSelected(tireStatus.temperatureStatus != TireStatus.TEMPERATURE_NORMAL);
                if (tireStatus.getBattery() < 2500) {
                    batteryImage.getDrawable().setLevel(0);
                } else if (tireStatus.getBattery() < 2700) {
                    batteryImage.getDrawable().setLevel(1);
                } else if (tireStatus.getBattery() < 2800) {
                    batteryImage.getDrawable().setLevel(2);
                } else {
                    batteryImage.getDrawable().setLevel(3);
                }
                batteryImage.setSelected(tireStatus.batteryStatus != TireStatus.BATTERY_NORMAL);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_monitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

//        for (LinearLayout board : mBoards) {
//            Drawable drawable = board.getBackground();
//            final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
//            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.board_tintcolor));
//            board.setBackgroundDrawable(wrappedDrawable);
//        }
        for (ImageView imageView : mBatteryImageViews) {
            Drawable drawable = imageView.getDrawable();
            final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.board_content_tintcolor));
            imageView.setImageDrawable(wrappedDrawable);
        }
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/CenturyGothicBold.ttf");
        for (TextView textView : mPressureTextViews) {
            textView.setTypeface(font);
        }

        getTpmeDevice().registerReceiver(this);

        refreshUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        getTpmeDevice().unregisterReceiver(this);
    }
}
