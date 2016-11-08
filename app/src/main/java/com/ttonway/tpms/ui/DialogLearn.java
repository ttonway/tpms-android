package com.ttonway.tpms.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.ttonway.tpms.R;

/**
 * Created by ttonway on 2016/11/3.
 */
public class DialogLearn extends BaseDialogFragment {

    public static DialogLearn newInstance() {
        DialogLearn frag = new DialogLearn();
        return frag;
    }

    int mTimeout = 120;
    final Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTextView != null) {
                mTextView.setText(getString(R.string.alert_message_learn, mTimeout + "s"));
                mTimeout--;
                if (mTimeout < 0) {
                    dismissAllowingStateLoss();
                } else {
                    mTextView.postDelayed(mTimeoutRunnable, 1000);
                }
            }
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView.post(mTimeoutRunnable);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mTextView.removeCallbacks(mTimeoutRunnable);

        FragmentLearn frag = (FragmentLearn) getParentFragment();
        frag.cancelTireMatch();
    }
}
