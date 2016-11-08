package com.ttonway.tpms.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.ttonway.tpms.MyApp;
import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.usb.TpmsDevice;
import com.ttonway.tpms.widget.TabManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

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
    @BindView(R.id.btn_voice)
    Button mVoiceBtn;
    @BindView(R.id.btn_setting)
    Button mSettingBtn;
    @BindView(android.R.id.tabhost)
    TabHost mTabHost;
    TabManager mTabManager;


    TpmsDevice mDevice;

    LocalBroadcastManager mBroadcastManager;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale locale = SPManager.getCurrentLocale(this);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, dm);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initTabHost();

        if (getIntent().getBooleanExtra("restart-setting", false)) {
            selectTab(3);
        } else {
            selectTab(0);
        }

        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        MyApp.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                ACTION_USB_PERMISSION);
        if (!MyApp.driver.UsbFeatureSupported()) {// 判断系统是否支持USB HOST
            DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_host_unavailable));
        } else {
            mDevice = new TpmsDevice(this, MyApp.driver);
            if (mDevice.openDevice()) {
                mDevice.querySettings();
            } else {
                DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastManager.registerReceiver(mReceiver, new IntentFilter(TpmsDevice.ACTION_COMMAND_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDevice != null) {
            mDevice.closeDevice();
        }
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

            final int tab = i;
            Button btn = TAB_BUTTON[i];
            final Drawable wrappedDrawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTintList(wrappedDrawable, getResources().getColorStateList(R.color.tab_tintcolor));
            wrappedDrawable.setBounds(0, 0, wrappedDrawable.getIntrinsicWidth(), wrappedDrawable.getIntrinsicHeight());
            btn.setCompoundDrawables(null, wrappedDrawable, null, null);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectTab(tab);
                }
            });
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

    @OnClick(R.id.btn_voice)
    void toggleVoice() {
        mVoiceBtn.setSelected(!mVoiceBtn.isSelected());
    }


}
