package com.ttonway.tpms.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.ttonway.tpms.R;
import com.ttonway.tpms.SPManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.techery.progresshint.ProgressHintDelegate;
import io.techery.progresshint.addition.widget.SeekBar;

/**
 * Created by ttonway on 2016/10/29.
 */
public class FragmentSetting extends Fragment {

    @BindView(R.id.group_language)
    RadioGroup mGroupLanguage;
    @BindView(R.id.group_pressure_unit)
    RadioGroup mGroupPressureUnit;
    @BindView(R.id.group_temp_unit)
    RadioGroup mGroupTempUnit;
    @BindView(R.id.pressure_upper_limit)
    SeekBar mSeekBarUpperLimit;
    @BindView(R.id.pressure_lower_limit)
    SeekBar mSeekBarLowerLimit;
    @BindView(R.id.temp_upper_limit)
    SeekBar mSeekBarTempUpperLimit;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);

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

        setupProgressLabels();
        float upperLimit = SPManager.getFloat(getActivity(), SPManager.KEY_PRESSURE_UPPER_LIMIT, SPManager.PRESSURE_UPPER_LIMIT_DEFAULT);
        float lowerLimit = SPManager.getFloat(getActivity(), SPManager.KEY_PRESSURE_LOWER_LIMIT, SPManager.PRESSURE_LOWER_LIMIT_DEFAULT);
        int upperLimit2 = SPManager.getInt(getActivity(), SPManager.KEY_TEMP_UPPER_LIMIT, SPManager.TEMP_UPPER_LIMIT_DEFAULT);
        mSeekBarUpperLimit.setMax(100);
        mSeekBarLowerLimit.setMax(100);
        mSeekBarTempUpperLimit.setMax(100);
        mSeekBarUpperLimit.setProgress((int) ((upperLimit - SPManager.PRESSURE_UPPER_LIMIT_MIN) / SPManager.PRESSURE_UPPER_LIMIT_RANGE * 100));
        mSeekBarLowerLimit.setProgress((int) ((lowerLimit - SPManager.PRESSURE_LOWER_LIMIT_MIN) / SPManager.PRESSURE_LOWER_LIMIT_RANGE * 100));
        mSeekBarTempUpperLimit.setProgress((int) ((float) (upperLimit2 - SPManager.TEMP_UPPER_LIMIT_MIN) / SPManager.TEMP_UPPER_LIMIT_RANGE * 100 + .5f));

    }

    void setupProgressLabels() {
        mSeekBarUpperLimit.getHintDelegate()
                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
                    @Override
                    public String getHint(android.widget.SeekBar seekBar, int progress) {
                        float bar = SPManager.PRESSURE_UPPER_LIMIT_MIN + (float) progress / 100 * SPManager.PRESSURE_UPPER_LIMIT_RANGE;
                        return formatPressure(getActivity(), bar);
                    }
                });
        mSeekBarLowerLimit.getHintDelegate()
                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
                    @Override
                    public String getHint(android.widget.SeekBar seekBar, int progress) {
                        float bar = SPManager.PRESSURE_LOWER_LIMIT_MIN + (float) progress / 100 * SPManager.PRESSURE_LOWER_LIMIT_RANGE;
                        return formatPressure(getActivity(), bar);
                    }
                });
        mSeekBarTempUpperLimit.getHintDelegate()
                .setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
                    @Override
                    public String getHint(android.widget.SeekBar seekBar, int progress) {
                        int degree = (int) (SPManager.TEMP_UPPER_LIMIT_MIN + (float) progress / 100 * SPManager.TEMP_UPPER_LIMIT_RANGE + .5f);
                        return formatTemperature(getActivity(), degree);
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        float bar1 = SPManager.PRESSURE_UPPER_LIMIT_MIN + (float) mSeekBarUpperLimit.getProgress() / 100 * SPManager.PRESSURE_UPPER_LIMIT_RANGE;
        float bar2 = SPManager.PRESSURE_LOWER_LIMIT_MIN + (float) mSeekBarLowerLimit.getProgress() / 100 * SPManager.PRESSURE_LOWER_LIMIT_RANGE;
        int degree = (int) (SPManager.TEMP_UPPER_LIMIT_MIN + (float) mSeekBarTempUpperLimit.getProgress() / 100 * SPManager.TEMP_UPPER_LIMIT_RANGE + .5f);
        SPManager.setFloat(getActivity(), SPManager.KEY_PRESSURE_UPPER_LIMIT, bar1);
        SPManager.setFloat(getActivity(), SPManager.KEY_PRESSURE_LOWER_LIMIT, bar2);
        SPManager.setInt(getActivity(), SPManager.KEY_TEMP_UPPER_LIMIT, degree);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public static String formatPressure(Context context, float bar) {
        int unit = SPManager.getInt(context, SPManager.KEY_PRESSURE_UNIT, SPManager.UNIT_BAR);
        switch (unit) {
            case SPManager.UNIT_BAR:
                return String.format("%.1f%s", bar, context.getString(R.string.unit_bar));
            case SPManager.UNIT_PSI:
                return String.format("%.1f%s", (bar / 14.5f), context.getString(R.string.unit_psi));
            case SPManager.UNIT_KPA:
                return String.format("%.1f%s", (bar - 100) / 100, context.getString(R.string.unit_kpa));
            case SPManager.UNIT_KG:
                return String.format("%.1f%s", bar, context.getString(R.string.unit_kg));
        }
        return null;
    }

    public static String formatTemperature(Context context, int celsiusDegree) {
        int unit = SPManager.getInt(context, SPManager.KEY_TEMP_UNIT, SPManager.TEMP_UNIT_CELSIUS);
        switch (unit) {
            case SPManager.TEMP_UNIT_CELSIUS:
                return String.format("%d%s", celsiusDegree, context.getString(R.string.degree_celsius));
            case SPManager.TEMP_UNIT_FAHRENHEIT:
                // 摄氏度×9/5+32=华氏度
                return String.format("%d%s", (celsiusDegree * 9 / 5 + 32), context.getString(R.string.degree_fahrenheit));
        }
        return null;
    }
}
