package com.ttonway.tpms.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ttonway.tpms.R;
import com.ttonway.tpms.core.TpmsDevice;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentExchange extends BaseFragment {

    @BindViews({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    List<LinearLayout> mBoards;
    @BindViews({R.id.image1, R.id.image3, R.id.image4, R.id.image2})
    List<ImageView> mImageViews;

    int mSelectedBorad1;
    int mSelectedBorad2;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_exchange, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        for (LinearLayout board : mBoards) {
            Drawable drawable = board.getBackground();
            final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.board_tintcolor));
            board.setBackgroundDrawable(wrappedDrawable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

        clearSelection();
    }

    void clearSelection() {
        for (LinearLayout board : mBoards) {
            selectBoard(board, false);
        }
    }

    void selectBoard(LinearLayout board, boolean selected) {
        board.setSelected(selected);
        int index = mBoards.indexOf(board);
        mImageViews.get(index).setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    public void exchangeTire() {
        byte tire1 = getTireIndex(mSelectedBorad1);
        byte tire2 = getTireIndex(mSelectedBorad2);
        if (tire1 != TpmsDevice.TIRE_NONE && tire2 != TpmsDevice.TIRE_NONE) {
            TpmsDevice device = getTpmeDevice();
            device.exchangeTire(tire1, tire2);
        }
    }

    @OnClick({R.id.board1, R.id.board3, R.id.board4, R.id.board2})
    void onClickTire(LinearLayout board) {
        boolean selected = !board.isSelected();
        selectBoard(board, selected);

        mSelectedBorad1 = View.NO_ID;
        mSelectedBorad2 = View.NO_ID;
        for (LinearLayout b : mBoards) {
            if (b.isSelected()) {
                if (mSelectedBorad1 == View.NO_ID) {
                    mSelectedBorad1 = b.getId();
                } else if (mSelectedBorad2 == View.NO_ID) {
                    mSelectedBorad2 = b.getId();
                    break;
                }
            }
        }

        if (mSelectedBorad1 != View.NO_ID && mSelectedBorad2 != View.NO_ID) {
            String name1 = getTireName(mSelectedBorad1);
            String name2 = getTireName(mSelectedBorad2);
            String str = getString(R.string.alert_message_exchange, name1, name2);
            SpannableStringBuilder builder = new SpannableStringBuilder(str);
            ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.common_green));
            int index1 = str.indexOf(name1);
            builder.setSpan(colorSpan1, index1, index1 + name1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.common_green));
            int index2 = str.indexOf(name2);
            builder.setSpan(colorSpan2, index2, index2 + name2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            DialogConfirm dialog = DialogConfirm.newInstance(builder);
            dialog.show(getChildFragmentManager(), "confirm");
        }
    }

    String getTireName(int boardId) {
        switch (boardId) {
            case R.id.board1:
                return getString(R.string.btn_tire1);
            case R.id.board2:
                return getString(R.string.btn_tire2);
            case R.id.board3:
                return getString(R.string.btn_tire3);
            case R.id.board4:
                return getString(R.string.btn_tire4);
            default:
                return null;
        }
    }

    public static byte getTireIndex(int boardId) {
        switch (boardId) {
            case R.id.board1:
                return TpmsDevice.TIRE_LEFT_FRONT;
            case R.id.board2:
                return TpmsDevice.TIRE_LEFT_END;
            case R.id.board3:
                return TpmsDevice.TIRE_RIGHT_FRONT;
            case R.id.board4:
                return TpmsDevice.TIRE_RIGHT_END;
            default:
                return TpmsDevice.TIRE_NONE;
        }
    }
}
