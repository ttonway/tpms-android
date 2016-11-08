package com.ttonway.tpms.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by ttonway on 2016/11/3.
 */
public class DialogConfirm extends BaseDialogFragment {
    private static final String KEY_MESSAGE = "ConfirmFrag:msg";

    public static DialogConfirm newInstance(CharSequence message) {
        DialogConfirm frag = new DialogConfirm();
        Bundle b = new Bundle();
        b.putCharSequence(KEY_MESSAGE, message);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView.setText(getArguments().getCharSequence(KEY_MESSAGE));
    }

    @Override
    void onPosiviveButtonClicked() {
        FragmentExchange frag = (FragmentExchange) getParentFragment();
        frag.exchangeTire();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        FragmentExchange frag = (FragmentExchange) getParentFragment();
        frag.clearSelection();
    }
}
