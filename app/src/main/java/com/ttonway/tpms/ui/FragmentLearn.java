package com.ttonway.tpms.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ttonway.tpms.R;
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

    LocalBroadcastManager mBroadcastManager;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(TpmsDevice.ACTION_TIRE_MATCHED)) {
                byte tire = intent.getByteExtra("tire", TpmsDevice.TIRE_NONE);
                String value = intent.getStringExtra("value");

                if (mProgressDialog != null && tire == mMatchingTire) {
                    mProgressDialog.dismissAllowingStateLoss();
                }

                if (mTextViews == null || mTextViews.size() != 4) {
                    return;
                }
                if (tire >= 0 && tire < mTextViews.size()) {
                    mTextViews.get(tire).setText(value);
                }
            } else if (intent.getAction().equals(TpmsDevice.ACTION_COMMAND_ERROR)) {
                mMatchingTire = TpmsDevice.TIRE_NONE;
                if (mProgressDialog != null) {
                    mProgressDialog.dismissAllowingStateLoss();
                }
            }
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

        mBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TpmsDevice.ACTION_TIRE_MATCHED);
        intentFilter.addAction(TpmsDevice.ACTION_COMMAND_ERROR);
        mBroadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        mBroadcastManager.unregisterReceiver(mReceiver);
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
