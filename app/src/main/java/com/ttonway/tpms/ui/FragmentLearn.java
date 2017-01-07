package com.ttonway.tpms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.R;
import com.ttonway.tpms.core.ErrorEvent;
import com.ttonway.tpms.core.StateChangeEvent;
import com.ttonway.tpms.core.TireMatchedEvent;
import com.ttonway.tpms.core.TpmsDevice;
import com.ttonway.tpms.core.TpmsDriver;
import com.ttonway.tpms.utils.MediaPlayerQueue;

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
    private Unbinder mUnbinder;

    byte mMatchingTire = TpmsDevice.TIRE_NONE;
    DialogLearn mProgressDialog;

    @Subscribe
    public void onTimeout(ErrorEvent e) {
        resetMatching();
    }

    @Subscribe
    public void onDeviceStateChanged(final StateChangeEvent e) {
        if (TpmsDevice.getInstance(getActivity()).getState() != TpmsDriver.STATE_OPEN) {
            resetMatching();
        }
    }

    void resetMatching() {
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

                switch (e.tire) {
                    case TpmsDevice.TIRE_LEFT_FRONT:
                        DialogAlert.showDialog(getChildFragmentManager(), getString(R.string.alert_message_tire1_matched));
                        MediaPlayerQueue.getInstance().addresource(R.raw.voice_tire1_match_success);
                        break;
                    case TpmsDevice.TIRE_LEFT_END:
                        DialogAlert.showDialog(getChildFragmentManager(), getString(R.string.alert_message_tire2_matched));
                        MediaPlayerQueue.getInstance().addresource(R.raw.voice_tire2_match_success);
                        break;
                    case TpmsDevice.TIRE_RIGHT_FRONT:
                        DialogAlert.showDialog(getChildFragmentManager(), getString(R.string.alert_message_tire3_matched));
                        MediaPlayerQueue.getInstance().addresource(R.raw.voice_tire3_match_success);
                        break;
                    case TpmsDevice.TIRE_RIGHT_END:
                        DialogAlert.showDialog(getChildFragmentManager(), getString(R.string.alert_message_tire4_matched));
                        MediaPlayerQueue.getInstance().addresource(R.raw.voice_tire4_match_success);
                        break;

                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_learn, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        getTpmeDevice().registerReceiver(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

        getTpmeDevice().unregisterReceiver(this);
    }

    @OnClick({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    void onClickTire(LinearLayout board) {
        int boradId = board.getId();
        byte tire = FragmentExchange.getTireIndex(boradId);
        getTpmeDevice().startTireMatch(tire);
        mMatchingTire = tire;
        mProgressDialog = DialogLearn.newInstance();
        mProgressDialog.show(getChildFragmentManager(), "learn");
    }

    public void cancelTireMatch() {
        if (mMatchingTire != TpmsDevice.TIRE_NONE) {
            getTpmeDevice().stopTireMatch();
            mMatchingTire = TpmsDevice.TIRE_NONE;
        }
    }

}
