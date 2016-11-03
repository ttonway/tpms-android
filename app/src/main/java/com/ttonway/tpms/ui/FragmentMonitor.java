package com.ttonway.tpms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttonway.tpms.R;
import com.ttonway.tpms.utils.Utils;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentMonitor extends Fragment {

    @BindViews({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    List<LinearLayout> mBoards;
    @BindViews({R.id.battery1, R.id.battery3, R.id.battery4, R.id.battery2})
    List<ImageView> mBatteryImageViews;
    @BindViews({R.id.pressure1, R.id.pressure3, R.id.pressure4, R.id.pressure2})
    List<TextView> mPressureTextViews;
    @BindViews({R.id.temperature1, R.id.temperature3, R.id.temperature4, R.id.temperature2})
    List<TextView> mTempTextViews;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_monitor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

        for (TextView textView : mPressureTextViews) {
            textView.setText(Utils.formatPressure(getActivity(), 0.f));
        }
        for (TextView textView : mTempTextViews) {
            textView.setText(Utils.formatTemperature(getActivity(), 0));
        }
    }
}
