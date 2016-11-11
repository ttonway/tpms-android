package com.ttonway.tpms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.R;
import com.ttonway.tpms.usb.TimeoutEvent;
import com.ttonway.tpms.usb.TireMatchedEvent;
import com.ttonway.tpms.usb.TpmsDevice;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentLearn extends BaseFragment {

    @BindViews({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    List<LinearLayout> mBoards;
    @BindViews({R.id.text1, R.id.text3, R.id.text4, R.id.text2})
    List<TextView> mTextViews;
    private Unbinder mUnbinder;

    byte mMatchingTire = TpmsDevice.TIRE_NONE;
    DialogLearn mProgressDialog;

    Object mReceiver = new Object() {

        @Subscribe
        public void onTimeout(TimeoutEvent e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMatchingTire = TpmsDevice.TIRE_NONE;
                    if (mProgressDialog != null) {
                        mProgressDialog.dismissAllowingStateLoss();
                    }
                }
            });
        }

        @Subscribe
        public void onTireMatched(final TireMatchedEvent e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressDialog != null && e.tire == mMatchingTire) {
                        mProgressDialog.dismissAllowingStateLoss();
                    }

                    if (mTextViews == null || mTextViews.size() != 4) {
                        return;
                    }
                    if (e.tire >= 0 && e.tire < mTextViews.size()) {
                        mTextViews.get(e.tire).setText(e.value);
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_learn, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        getEventBus().register(mReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        getEventBus().unregister(mReceiver);
    }

    @OnClick({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    void onClickTire(LinearLayout board) {
        int boradId = board.getId();
        byte tire = FragmentExchange.getTireIndex(boradId);
        TpmsDevice device = getTpmeDevice();
        if (device != null) {
            device.startTireMatch(tire);
            mMatchingTire = tire;
            mProgressDialog = DialogLearn.newInstance();
            mProgressDialog.show(getChildFragmentManager(), "learn");
        }
    }

    public void cancelTireMatch() {
        TpmsDevice device = getTpmeDevice();
        if (device != null && mMatchingTire != TpmsDevice.TIRE_NONE) {
            device.stopTireMatch();
            mMatchingTire = TpmsDevice.TIRE_NONE;
        }
    }


}
