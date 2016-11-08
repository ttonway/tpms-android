package com.ttonway.tpms.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ttonway.tpms.R;

/**
 * Created by ttonway on 2016/11/3.
 */
public class BaseDialogFragment extends DialogFragment {

    TextView mTextView;
    Button mPositiveBtn;
    Button mNegativeBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_custom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextView = (TextView) view.findViewById(R.id.text1);
        mPositiveBtn = (Button) view.findViewById(R.id.btn1);
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
                onPosiviveButtonClicked();
            }
        });
        mNegativeBtn = (Button) view.findViewById(R.id.btn2);
        mNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mTextView = null;
        mPositiveBtn = mNegativeBtn = null;
    }

    void onPosiviveButtonClicked() {

    }

    //    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return new AlertDialog.Builder(getActivity())
//                .setMessage(getArguments().getCharSequence(KEY_MESSAGE))
//                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setNegativeButton(R.string.btn_cancel, null)
//                .create();
//    }

}
