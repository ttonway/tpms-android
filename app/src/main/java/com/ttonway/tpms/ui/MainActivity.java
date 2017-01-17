package com.ttonway.tpms.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.BuildConfig;
import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.core.BackgroundService;
import com.ttonway.tpms.core.SettingChangeEvent;
import com.ttonway.tpms.core.StateChangeEvent;
import com.ttonway.tpms.core.TpmsDevice;
import com.ttonway.tpms.core.TpmsDriver;
import com.ttonway.tpms.utils.Utils;
import com.ttonway.tpms.widget.TabManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STATE_TAB = "MainActivity:tab";

    static final Class<?>[] TAB_CLS = new Class[]{FragmentMonitor.class, FragmentLearn.class, FragmentExchange.class, FragmentSetting.class};
    static String[] TAB_TAGS = new String[]{"tab-1", "tab-2", "tab-3", "tab-4"};
    static int[] TAB_ICONS = new int[]{R.drawable.ic_monitor, R.drawable.ic_learn, R.drawable.ic_exchange, R.drawable.ic_setting};
    static final int TAB_COUNT = TAB_CLS.length;
    final Button[] TAB_BUTTON = new Button[TAB_COUNT];

    @BindView(R.id.btn_monitor)
    Button mMonitorBtn;
    @BindView(R.id.btn_learn)
    Button mLearnBtn;
    @BindView(R.id.btn_exchange)
    Button mExchangeBtn;
    @BindView(R.id.btn_setting)
    Button mSettingBtn;
    @BindView(R.id.btn_bluetooth)
    Button mBluetoothBtn;
    @BindView(R.id.progress_bluetooth)
    ProgressBar mBluetoothProgressBar;

    View mMenuContainer;
    ImageView mMenuBackground;

    @BindView(R.id.box_bluetooth)
    FrameLayout mBluetoothBox;

    @BindView(R.id.image_background)
    ImageView mBackgroundImageView;
    @BindView(android.R.id.tabhost)
    TabHost mTabHost;
    TabManager mTabManager;

    TpmsDevice device;

    Handler mHandler = new Handler();
    DialogAlert mErrorDialog;
    final Runnable mAudoDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mErrorDialog != null) {
                mErrorDialog.dismissAllowingStateLoss();
                mErrorDialog = null;
            }
        }
    };

    @Subscribe
    public void onSettingChanged(final SettingChangeEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (e.command == TpmsDevice.CMD_QUERY_SETTING) {
                    Log.d(TAG, "dismiss error dialog");
                    if (mErrorDialog != null) {
                        mErrorDialog.dismissAllowingStateLoss();
                        mErrorDialog = null;
                    }
                    mHandler.removeCallbacks(mAudoDismissRunnable);
                }
            }
        });
    }

    @Subscribe
    public void onDeviceStateChanged(final StateChangeEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                refreshBluetoothState();
            }
        });
    }


    public void setThemeImage(int theme) {
        if (mBackgroundImageView != null) {
            switch (theme) {
                case SPManager.THEME_PLAIN:
                    mBackgroundImageView.setImageResource(R.drawable.background_plain);
                    break;
                case SPManager.THEME_STAR:
                    mBackgroundImageView.setImageResource(R.drawable.background_star);
                    break;
                case SPManager.THEME_MODERN:
                    mBackgroundImageView.setImageResource(R.drawable.background_modern);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        Toast.makeText(this, metrics.toString(), Toast.LENGTH_LONG).show();

        Utils.setupLocale(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMenuContainer = findViewById(R.id.menu_container);
        mMenuBackground = (ImageView) findViewById(R.id.menu_background);

        if (!BuildConfig.FLAVOR.startsWith("bluetooth")) {
            mBluetoothBox.setVisibility(View.GONE);
        }
        setThemeImage(SPManager.getInt(this, SPManager.KEY_THEME, SPManager.THEME_PLAIN));
        initTabHost();

        int tab = 0;
        if (getIntent().getBooleanExtra("restart-setting", false)) {
            tab = 3;
        }
        if (savedInstanceState != null) {
            tab = savedInstanceState.getInt(STATE_TAB, tab);
        }
        selectTab(tab);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.screenBrightness = 0;
//        getWindow().setAttributes(lp);
        initTpmsDevice(savedInstanceState == null);
        device.registerReceiver(this);
        BackgroundService.startService(this);

        refreshBluetoothState();


//        new TTSUtils(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TAB, mTabHost.getCurrentTab());
        super.onSaveInstanceState(outState);
    }

    void refreshBluetoothState() {
        if (mBluetoothBtn == null || mBluetoothProgressBar == null) {
            return;
        }

        int state = device.getState();
        if (state == TpmsDriver.STATE_OPEN) {
            mBluetoothBtn.setVisibility(View.VISIBLE);
            mBluetoothProgressBar.setVisibility(View.GONE);
            mBluetoothBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_bluetooth, 0, 0);
        } else if (state == TpmsDriver.STATE_CLOSE) {
            mBluetoothBtn.setVisibility(View.VISIBLE);
            mBluetoothProgressBar.setVisibility(View.GONE);
            mBluetoothBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_bluetooth_off, 0, 0);
        } else {
            mBluetoothBtn.setVisibility(View.INVISIBLE);
            mBluetoothProgressBar.setVisibility(View.VISIBLE);
        }
    }

    void initTpmsDevice(boolean showAlert) {
        device = TpmsDevice.getInstance(this);
        String error = device.getTpmsDriver().isDriverSupported();
        if (error != null) {
            if (showAlert) {
                DialogAlert.showDialog(getSupportFragmentManager(), error);
            }
        } else {
            if (device.openDevice()) {
//                device.querySettings();
            } else if (showAlert) {
                if (mErrorDialog != null) {
                    mErrorDialog.dismissAllowingStateLoss();
                    mErrorDialog = null;
                }
                mHandler.removeCallbacks(mAudoDismissRunnable);
                mErrorDialog = DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
                mHandler.postDelayed(mAudoDismissRunnable, 3000);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (device.isOpen() && device.hasError()) {
            device.closeDevice();
        }
        if (!device.isOpen()) {
            if (device.openDevice()) {
//                device.querySettings();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mErrorDialog != null) {
            mErrorDialog.dismissAllowingStateLoss();
            mErrorDialog = null;
        }
        mHandler.removeCallbacks(mAudoDismissRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        device.unregisterReceiver(this);

        BackgroundService.startService(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged " + newConfig);
    }

    private void initTabHost() {
        mTabHost.setup();

        mTabManager = new TabManager(this, this.getSupportFragmentManager(),
                mTabHost, R.id.realtabcontent);

        TAB_BUTTON[0] = mMonitorBtn;
        TAB_BUTTON[1] = mLearnBtn;
        TAB_BUTTON[2] = mExchangeBtn;
        TAB_BUTTON[3] = mSettingBtn;

        for (int i = 0; i < TAB_COUNT; i++) {
            String title = TAB_TAGS[i];
            Drawable icon = getResources().getDrawable(TAB_ICONS[i]);
            mTabManager.addTab(
                    mTabHost.newTabSpec(title).setIndicator(title, icon),
                    TAB_CLS[i], null);

            Button btn = TAB_BUTTON[i];
            final Drawable wrappedDrawable = DrawableCompat.wrap(btn.getCompoundDrawables()[1]);
            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.tab_tintcolor));
            wrappedDrawable.setBounds(0, 0, wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight());
            btn.setCompoundDrawables(null, wrappedDrawable, null, null);
        }
    }

    @OnClick(R.id.btn_monitor)
    public void onMonitorClick() {
        boolean visiable = mLearnBtn.isShown() && mExchangeBtn.isShown();
        Log.d(TAG, "buttons visiable? " + visiable);
        if (visiable) {
            setButtonMenuVisibility(View.GONE);
        } else if (mMonitorBtn.isSelected()) {
            if (mMenuBackground != null) {
                mMenuBackground.setVisibility(View.VISIBLE);
            }
            setButtonMenuVisibility(View.VISIBLE);
        }

        selectTab(0);
//        ObjectAnimator animator;
//        if (visiable) {
//
//            animator = ObjectAnimator.ofFloat(mMenuContainer, "translationX", -mMenuContainer.getHeight(), 0);
//            animator.addListener(new AnimatorListenerAdapter() {
//
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    mMenuContainer.setVisibility(View.VISIBLE);
//                }
//            });
//        } else {
//
//            animator = ObjectAnimator.ofFloat(mMenuContainer, "translationX", 0, -mMenuContainer.getWidth());
//            animator.addListener(new AnimatorListenerAdapter() {
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mMenuContainer.setVisibility(View.INVISIBLE);
//                }
//            });
//        }
//        animator.setDuration(300);
//        animator.start();
    }

    @OnClick(R.id.btn_learn)
    public void onLearnClick() {
        if (mMenuBackground != null) {
            mMenuBackground.setVisibility(View.GONE);
        }
        selectTab(1);
    }

    @OnClick(R.id.btn_exchange)
    public void onExchangeClick() {
        if (mMenuBackground != null) {
            mMenuBackground.setVisibility(View.GONE);
        }
        selectTab(2);
    }

    @OnClick(R.id.btn_setting)
    public void onSettingClick() {
        setButtonMenuVisibility(View.GONE);

        selectTab(3);
    }

    void setButtonMenuVisibility(int visibility) {
        if (mMenuContainer != null) {
            mMenuContainer.setVisibility(visibility);
        } else {
            FrameLayout parent1 = (FrameLayout) mLearnBtn.getParent();
            FrameLayout parent2 = (FrameLayout) mExchangeBtn.getParent();
            parent1.setVisibility(visibility);
            parent2.setVisibility(visibility);
        }
    }

    void selectTab(int index) {
        mTabHost.setCurrentTabByTag(TAB_TAGS[index]);

        for (int i = 0; i < TAB_COUNT; i++) {
            Button btn = TAB_BUTTON[i];
            FrameLayout parent = (FrameLayout) btn.getParent();
            if (i == index) {
                parent.setSelected(true);
            } else {
                parent.setSelected(false);
            }
        }
    }

    @OnClick(R.id.box_bluetooth)
    void gotoScanActivity() {
        startActivity(new Intent(this, DeviceScanActivity.class));
    }
}
