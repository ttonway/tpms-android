package com.ttonway.tpms.ui;

import android.support.v4.app.Fragment;

import com.ttonway.tpms.core.TpmsDevice;

/**
 * Created by ttonway on 2016/11/8.
 */
public class BaseFragment extends Fragment {

    TpmsDevice getTpmeDevice() {
        return TpmsDevice.getInstance(getActivity());
    }

    public final void runOnUiThread(Runnable action) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(action);
        }
    }
}
