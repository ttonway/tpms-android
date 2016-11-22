package com.ttonway.tpms.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by ttonway on 2016/11/21.
 */
public class TTSUtils {
    private static final String TAG = TTSUtils.class.getSimpleName();

    static final String[] data = new String[]{
            "voice_tire1_pressure_high",
            "voice_tire2_pressure_high",
            "voice_tire3_pressure_high",
            "voice_tire4_pressure_high",
            "voice_tire1_pressure_low",
            "voice_tire2_pressure_low",
            "voice_tire3_pressure_low",
            "voice_tire4_pressure_low",
            "voice_tire1_pressure_error",
            "voice_tire2_pressure_error",
            "voice_tire3_pressure_error",
            "voice_tire4_pressure_error",
            "voice_tire1_temp_high",
            "voice_tire2_temp_high",
            "voice_tire3_temp_high",
            "voice_tire4_temp_high",
            "voice_tire1_battery_low",
            "voice_tire2_battery_low",
            "voice_tire3_battery_low",
            "voice_tire4_battery_low",
            "voice_tire1_match_success",
            "voice_tire2_match_success",
            "voice_tire3_match_success",
            "voice_tire4_match_success"
    };


    Context mContext;

    int mIndex = 0;
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

            mTTS.destroy();

            if (mIndex < data.length) {
                generateWav();
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    public TTSUtils(Context context) {
        this.mContext = context;

        generateWav();
    }

    private void generateWav() {
        String name = data[mIndex++];
        Log.d(TAG, "make " + name);

        mTTS = SpeechSynthesizer.createSynthesizer(mContext, null);
        //2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer 类
        // 设置发音人(更多在线发音人，用户可参见 附录13.2
        mTTS.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTTS.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTTS.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mTTS.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTTS.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTTS.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/ttonway/" + name + ".wav");

        int resId = mContext.getResources().getIdentifier(name, "string", mContext.getPackageName());
        String str = mContext.getString(resId);
        mTTS.startSpeaking(str, mSpeakListener);
    }
}
