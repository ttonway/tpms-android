package com.ttonway.tpms.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TabHost;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.ttonway.tpms.MyApp;
import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.usb.TimeoutEvent;
import com.ttonway.tpms.usb.TireStatus;
import com.ttonway.tpms.usb.TireStatusUpdatedEvent;
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

    SpeechSynthesizer mTTS;
    SynthesizerListener mSpeakListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            Log.d(TAG, "onCompleted " + speechError);
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    static EventBus eventBus;
    static TpmsDevice device;

    Object mReceiver = new Object() {
        @Subscribe
        public void onTimeout(TimeoutEvent e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
                }
            });
        }

        @Subscribe
        public void onStatusUpdated(final TireStatusUpdatedEvent e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVoiceBtn.isSelected() && device != null && e.tire != TpmsDevice.TIRE_NONE) {
                        TireStatus status = device.getTireStatus(e.tire);
                        speakAlert(e.tire, status);
                    }
                }
            });
        }
    };

    void speakAlert(byte tire, TireStatus status) {
        String prefix = "voice_";
        switch (tire) {
            case TpmsDevice.TIRE_LEFT_FRONT:
                prefix += "tire1_";
                break;
            case TpmsDevice.TIRE_RIGHT_FRONT:
                prefix += "tire3_";
                break;
            case TpmsDevice.TIRE_RIGHT_END:
                prefix += "tire4_";
                break;
            case TpmsDevice.TIRE_LEFT_END:
                prefix += "tire2_";
                break;
        }

        switch (status.pressureStatus) {
            case TireStatus.PRESSURE_HIGH:
                speakAlert(prefix + "pressure_high");
                break;
            case TireStatus.PRESSURE_LOW:
                speakAlert(prefix + "pressure_low");
                break;
            case TireStatus.PRESSURE_ERROR:
                speakAlert(prefix + "pressure_error");
                break;
        }
        if (status.temperatureStatus == TireStatus.TEMPERATURE_HIGH) {
            speakAlert(prefix + "temp_high");
        }
        if (status.batteryStatus == TireStatus.BATTERY_LOW) {
            speakAlert(prefix + "battery_low");
        }
    }

    void speakAlert(String strName) {
        int id = getResources().getIdentifier(strName, "string", getPackageName());
        if (id == 0) {
            Log.e(TAG, "getIdentifier for " + strName + " fail.");
            return;
        }

        String str = getResources().getString(id);
        Log.d(TAG, "speak " + str);
        mTTS.startSpeaking(str, mSpeakListener);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        Locale locale = SPManager.getCurrentLocale(this);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, dm);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVoiceBtn.setSelected(SPManager.getBoolean(this, SPManager.KEY_VOICE_OPEN, true));
        initTabHost();

        if (getIntent().getBooleanExtra("restart-setting", false)) {
            selectTab(3);
        } else {
            selectTab(0);
        }

        initTTS();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持常亮的屏幕的状态
        initTpmsDevice();
    }

    void initTpmsDevice() {
        if (MyApp.driver == null) {
            MyApp.driver = new CH34xUARTDriver(
                    (UsbManager) getSystemService(Context.USB_SERVICE), this,
                    ACTION_USB_PERMISSION);
        }
        if (eventBus == null) {
            eventBus = new EventBus();
        }
        if (!MyApp.driver.UsbFeatureSupported()) {// 判断系统是否支持USB HOST
            DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_host_unavailable));
        } else {
            if (device == null) {
                device = new TpmsDevice(MyApp.driver, eventBus);
            }

            if (device.openDevice()) {
                device.querySettings();
            } else {
                DialogAlert.showDialog(getSupportFragmentManager(), getString(R.string.alert_message_usb_io_error));
            }
        }
    }

    void initTTS() {
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        mTTS = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer 类
        // 设置发音人(更多在线发音人，用户可参见 附录13.2
        mTTS.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTTS.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTTS.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mTTS.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        eventBus.register(mReceiver);

        if (device != null && !device.isOpen()) {
            if (device.openDevice()) {
                device.querySettings();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        eventBus.unregister(mReceiver);

        if (device != null) {
            device.closeDevice();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        mTTS.destroy();
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
