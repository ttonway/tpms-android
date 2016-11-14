package com.ttonway.tpms.ui;

import android.support.v4.app.Fragment;

import com.google.common.eventbus.EventBus;
import com.ttonway.tpms.usb.TpmsDevice;

/**
 * Created by ttonway on 2016/11/8.
 */
public class BaseFragment extends Fragment {

    TpmsDevice getTpmeDevice() {
        return MainActivity.device;
    }

    EventBus getEventBus() {
        return MainActivity.eventBus;
    }

    public final void runOnUiThread(Runnable action) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(action);
        }
    }
}
