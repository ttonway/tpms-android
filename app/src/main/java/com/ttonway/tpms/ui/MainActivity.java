package com.ttonway.tpms.ui;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TabHost;

import com.ttonway.tpms.TpmsApp;
import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.usb.BackgroundService;
import com.ttonway.tpms.usb.TpmsDevice;
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

    TpmsDevice device;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        Toast.makeText(this, metrics.toString(), Toast.LENGTH_LONG).show();

        Utils.setupLocale(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVoiceBtn.setSelected(SPManager.getBoolean(this, SPManager.KEY_VOICE_OPEN, true));
        initTabHost();

        if (getIntent().getBooleanExtra("restart-setting", false)) {
            selectTab(3);
        } else {
            selectTab(0);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        initTpmsDevice();
        BackgroundService.startService(this);

//        new TTSUtils(this);
    }

    void initTpmsDevice() {
        device = TpmsDevice.getInstance(this);
        if (!TpmsApp.driver.UsbFeatureSupported()) {// 判断系统是否支持USB HOST
            DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_host_unavailable));
        } else {
            if (device.openDevice()) {
                //device.querySettings();
            } else {
                DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (!device.isOpen()) {
            if (device.openDevice()) {
                //device.querySettings();
            }
        }

        // register foreground receiver to judge app running in background or foreground
        device.registerReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        device.unregisterReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        BackgroundService.startService(this);
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
        boolean open = !mVoiceBtn.isSelected();
        mVoiceBtn.setSelected(open);
        SPManager.setBoolean(this, SPManager.KEY_VOICE_OPEN, open);
    }
}
