package com.ttonway.tpms.ui;

import android.support.v4.app.Fragment;

import com.ttonway.tpms.usb.TpmsDevice;

/**
 * Created by ttonway on 2016/11/8.
 */
public class BaseFragment extends Fragment {

    TpmsDevice getTpmeDevice() {
        MainActivity activity = (MainActivity) getActivity();
        return activity.mDevice;
    }
}
