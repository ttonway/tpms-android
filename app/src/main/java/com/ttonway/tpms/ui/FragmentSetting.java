package com.ttonway.tpms.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ttonway.tpms.BuildConfig;
import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.core.TpmsDevice;
import com.ttonway.tpms.utils.Utils;
import com.ttonway.tpms.widget.MyNumberPicker;


import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentSetting extends BaseFragment {

    @BindView(R.id.group_voice)
    RadioGroup mGroupVoice;
    @BindView(R.id.group_language)
    RadioGroup mGroupLanguage;
    @BindView(R.id.group_pressure_unit)
    RadioGroup mGroupPressureUnit;
    @BindView(R.id.group_temp_unit)
    RadioGroup mGroupTempUnit;
    @BindView(R.id.group_theme)
    RadioGroup mGroupTheme;
    //    @BindView(R.id.pressure_upper_limit)
//    SeekBar mSeekBarUpperLimit;
//    @BindView(R.id.pressure_lower_limit)
//    SeekBar mSeekBarLowerLimit;
//    @BindView(R.id.temp_upper_limit)
//    SeekBar mSeekBarTempUpperLimit;
    @BindView(R.id.pressure_upper_limit)
    MyNumberPicker mPickerUpperLimit;
    @BindView(R.id.pressure_lower_limit)
    MyNumberPicker mPickerLowerLimit;
    @BindView(R.id.temp_upper_limit)
    MyNumberPicker mPickerTempLowerLimit;

    @BindView(R.id.text_version)
    TextView mVersionTextView;

    private Unbinder mUnbinder;

    void refreshUI() {
        TpmsDevice device = getTpmeDevice();
        float upperLimit = device.mPressureHighLimit;
        float lowerLimit = device.mPressureLowLimit;
        int upperLimit2 = device.mTemperatureLimit;

//        mSeekBarUpperLimit.setProgress((int) ((upperLimit - SPManager.PRESSURE_UPPER_LIMIT_MIN) / SPManager.PRESSURE_UPPER_LIMIT_RANGE * 100 + 0.5f));
//        mSeekBarLowerLimit.setProgress((int) ((lowerLimit - SPManager.PRESSURE_LOWER_LIMIT_MIN) / SPManager.PRESSURE_LOWER_LIMIT_RANGE * 100 + 0.5f));
//        mSeekBarTempUpperLimit.setProgress((int) ((float) (upperLimit2 - SPManager.TEMP_UPPER_LIMIT_MIN) / SPManager.TEMP_UPPER_LIMIT_RANGE * 100 + 0.5f));
        mPickerUpperLimit.setValue(upperLimit);
        mPickerLowerLimit.setValue(lowerLimit);
        mPickerTempLowerLimit.setValue(upperLimit2);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

        StringBuilder sb = new StringBuilder();
        sb.append("Version").append("\n");
        sb.append("App Version: " + Utils.getAppVersion(getActivity()) + "(" + Utils.getAppVersionCode(getActivity()) + ")").append("\n");
        sb.append("BuildConfig: {applicationId=" + BuildConfig.APPLICATION_ID + ", buildType=" + BuildConfig.BUILD_TYPE + ", flavor=" + BuildConfig.FLAVOR
                + ", debug=" + BuildConfig.DEBUG + ", versionName=" + BuildConfig.VERSION_NAME + ", versionCode=" + BuildConfig.VERSION_CODE + "}").append("\n");
        sb.append("System: {Model=" + Build.MODEL + ", version.sdk=" + Build.VERSION.SDK + ", version.release=" + Build.VERSION.RELEASE + "}").append("\n");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        sb.append("DisplayMetrics: " + metrics);
        mVersionTextView.setText(sb);

        boolean voiceOn = SPManager.getBoolean(getActivity(), SPManager.KEY_VOICE_OPEN, true);
        mGroupVoice.check(voiceOn ? R.id.voice_open : R.id.voice_close);
        mGroupVoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SPManager.setBoolean(getActivity(), SPManager.KEY_VOICE_OPEN, checkedId != R.id.voice_close);
            }
        });

        Locale locale = SPManager.getCurrentLocale(getActivity());
        if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
            mGroupLanguage.check(R.id.language_simplified_chinese);
        } else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
            mGroupLanguage.check(R.id.language_traditional_chinese);
        } else if (Locale.ENGLISH.equals(locale)) {
            mGroupLanguage.check(R.id.language_english);
        }
        mGroupLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Resources resources = getActivity().getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                int language = SPManager.LANGUAGE_SIMPLIFIED_CHINESE;
                switch (checkedId) {
                    case R.id.language_simplified_chinese:
                        config.locale = Locale.SIMPLIFIED_CHINESE;
                        language = SPManager.LANGUAGE_SIMPLIFIED_CHINESE;
                        break;
                    case R.id.language_traditional_chinese:
                        config.locale = Locale.TRADITIONAL_CHINESE;
                        language = SPManager.LANGUAGE_TRADITIONAL_CHINESE;
                        break;
                    case R.id.language_english:
                        config.locale = Locale.ENGLISH;
                        language = SPManager.LANGUAGE_ENGLISH;
                        break;
                }
                resources.updateConfiguration(config, dm);
                SPManager.setInt(getContext(), SPManager.KEY_LANGUAGE, language);

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("restart-setting", true);
                getActivity().startActivity(intent);
            }
        });

        int pressureUnit = SPManager.getInt(getActivity(), SPManager.KEY_PRESSURE_UNIT, SPManager.UNIT_BAR);
        switch (pressureUnit) {
            case SPManager.UNIT_BAR:
                mGroupPressureUnit.check(R.id.unit_bar);
                break;
            case SPManager.UNIT_PSI:
                mGroupPressureUnit.check(R.id.unit_psi);
                break;
            case SPManager.UNIT_KPA:
                mGroupPressureUnit.check(R.id.unit_kpa);
                break;
            case SPManager.UNIT_KG:
                mGroupPressureUnit.check(R.id.unit_kg);
                break;
        }
        mGroupPressureUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int unit = SPManager.UNIT_BAR;
                switch (checkedId) {
                    case R.id.unit_bar:
                        unit = SPManager.UNIT_BAR;
                        break;
                    case R.id.unit_psi:
                        unit = SPManager.UNIT_PSI;
                        break;
                    case R.id.unit_kpa:
                        unit = SPManager.UNIT_KPA;
                        break;
                    case R.id.unit_kg:
                        unit = SPManager.UNIT_KG;
                        break;
                }
                SPManager.setInt(getActivity(), SPManager.KEY_PRESSURE_UNIT, unit);

                setupProgressLabels();
            }
        });

        int tempUnit = SPManager.getInt(getActivity(), SPManager.KEY_TEMP_UNIT, SPManager.TEMP_UNIT_CELSIUS);
        switch (tempUnit) {
            case SPManager.TEMP_UNIT_CELSIUS:
                mGroupTempUnit.check(R.id.degree_celsius);
                break;
            case SPManager.TEMP_UNIT_FAHRENHEIT:
                mGroupTempUnit.check(R.id.degree_fahrenheit);
                break;
        }
        mGroupTempUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int unit = SPManager.TEMP_UNIT_CELSIUS;
                switch (checkedId) {
                    case R.id.degree_celsius:
                        unit = SPManager.TEMP_UNIT_CELSIUS;
                        break;
                    case R.id.degree_fahrenheit:
                        unit = SPManager.TEMP_UNIT_FAHRENHEIT;
                        break;
                }
                SPManager.setInt(getActivity(), SPManager.KEY_TEMP_UNIT, unit);

                setupProgressLabels();
            }
        });

        int theme = SPManager.getInt(getActivity(), SPManager.KEY_THEME, SPManager.THEME_PLAIN);
        switch (theme) {
            case SPManager.THEME_PLAIN:
                mGroupTheme.check(R.id.theme_plain);
                break;
            case SPManager.THEME_STAR:
                mGroupTheme.check(R.id.theme_star);
                break;
            case SPManager.THEME_MODERN:
                mGroupTheme.check(R.id.theme_modern);
                break;
        }
        mGroupTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int t = SPManager.THEME_PLAIN;
                switch (checkedId) {
                    case R.id.theme_plain:
                        t = SPManager.THEME_PLAIN;
                        break;
                    case R.id.theme_star:
                        t = SPManager.THEME_STAR;
                        break;
                    case R.id.theme_modern:
                        t = SPManager.THEME_MODERN;
                        break;
                }
                SPManager.setInt(getActivity(), SPManager.KEY_THEME, t);

                ((MainActivity) getActivity()).setThemeImage(t);
            }
        });


//        mSeekBarUpperLimit.setMax(100);
//        mSeekBarLowerLimit.setMax(100);
//        mSeekBarTempUpperLimit.setMax(100);
        mPickerLowerLimit.initialize(0.1f, SPManager.PRESSURE_LOWER_LIMIT_MIN, SPManager.PRESSURE_LOWER_LIMIT_MAX);
        mPickerUpperLimit.initialize(0.1f, SPManager.PRESSURE_UPPER_LIMIT_MIN, SPManager.PRESSURE_UPPER_LIMIT_MAX);
        mPickerTempLowerLimit.initialize(1f, SPManager.TEMP_UPPER_LIMIT_MIN, SPManager.TEMP_UPPER_LIMIT_MAX);
        setupProgressLabels();
        refreshUI();
    }

    void setupProgressLabels() {
        mPickerLowerLimit.setValueFormatter(new MyNumberPicker.ValueFormatter() {
            @Override
            public String getHint(float value) {
                return Utils.formatPressure(getActivity(), value);
            }
        });
        mPickerUpperLimit.setValueFormatter(new MyNumberPicker.ValueFormatter() {
            @Override
            public String getHint(float value) {
                return Utils.formatPressure(getActivity(), value);
            }
        });
        mPickerTempLowerLimit.setValueFormatter(new MyNumberPicker.ValueFormatter() {
            @Override
            public String getHint(float value) {
                return Utils.formatTemperature(getActivity(), (int) value);
            }
        });

//        mSeekBarUpperLimit.getHintDelegate()
//                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
//                    @Override
//                    public String getHint(android.widget.SeekBar seekBar, int progress) {
//                        float bar = SPManager.PRESSURE_UPPER_LIMIT_MIN + (float) progress / 100 * SPManager.PRESSURE_UPPER_LIMIT_RANGE;
//                        return Utils.formatPressure(getActivity(), bar);
//                    }
//                });
//        mSeekBarLowerLimit.getHintDelegate()
//                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
//                    @Override
//                    public String getHint(android.widget.SeekBar seekBar, int progress) {
//                        float bar = SPManager.PRESSURE_LOWER_LIMIT_MIN + (float) progress / 100 * SPManager.PRESSURE_LOWER_LIMIT_RANGE;
//                        return Utils.formatPressure(getActivity(), bar);
//                    }
//                });
//        mSeekBarTempUpperLimit.getHintDelegate()
//                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
//                    @Override
//                    public String getHint(android.widget.SeekBar seekBar, int progress) {
//                        int degree = (int) (SPManager.TEMP_UPPER_LIMIT_MIN + (float) progress / 100 * SPManager.TEMP_UPPER_LIMIT_RANGE + .5f);
//                        return Utils.formatTemperature(getActivity(), degree);
//                    }
//                });
    }

    @Override
    public void onPause() {
        super.onPause();

//        float bar1 = SPManager.PRESSURE_UPPER_LIMIT_MIN + (float) mSeekBarUpperLimit.getProgress() / 100 * SPManager.PRESSURE_UPPER_LIMIT_RANGE;
//        float bar2 = SPManager.PRESSURE_LOWER_LIMIT_MIN + (float) mSeekBarLowerLimit.getProgress() / 100 * SPManager.PRESSURE_LOWER_LIMIT_RANGE;
//        int degree = (int) (SPManager.TEMP_UPPER_LIMIT_MIN + (float) mSeekBarTempUpperLimit.getProgress() / 100 * SPManager.TEMP_UPPER_LIMIT_RANGE + .5f);
        float bar1 = mPickerUpperLimit.getValue();
        float bar2 = mPickerLowerLimit.getValue();
        int degree = (int) mPickerTempLowerLimit.getValue();


        TpmsDevice device = getTpmeDevice();
        if (device.isSettingsChanged(bar2, bar1, degree)) {
            device.saveSettings(bar2, bar1, degree);
        }
    }

    @OnClick(R.id.btn_reset)
    void resetSetting() {
        SPManager.clear(getActivity());
        getTpmeDevice().clearData();

        TpmsDevice device = getTpmeDevice();
        if (device.isOpen()) {
            device.saveSettings(SPManager.PRESSURE_LOWER_LIMIT_DEFAULT, SPManager.PRESSURE_UPPER_LIMIT_DEFAULT, SPManager.TEMP_UPPER_LIMIT_DEFAULT);
        }
        device.closeDeviceSafely();

        mPickerUpperLimit.setValue(SPManager.PRESSURE_UPPER_LIMIT_DEFAULT);
        mPickerLowerLimit.setValue(SPManager.PRESSURE_LOWER_LIMIT_DEFAULT);
        mPickerTempLowerLimit.setValue(SPManager.TEMP_UPPER_LIMIT_DEFAULT);

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
