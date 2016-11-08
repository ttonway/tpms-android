package com.ttonway.tpms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.ttonway.tpms.R;

/**
 * Created by ttonway on 2016/11/3.
 */
public class DialogAlert extends BaseDialogFragment {
    private static final String KEY_MESSAGE = "DialogAlert:msg";

    public static void showDialog(FragmentManager fm, CharSequence message) {
        DialogAlert dialog = newInstance(message);
        dialog.show(fm, "alert");
    }

    public static DialogAlert newInstance(CharSequence message) {
        DialogAlert frag = new DialogAlert();
        Bundle b = new Bundle();
        b.putCharSequence(KEY_MESSAGE, message);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView.setText(getArguments().getCharSequence(KEY_MESSAGE));
        mNegativeBtn.setVisibility(View.GONE);
    }
}
