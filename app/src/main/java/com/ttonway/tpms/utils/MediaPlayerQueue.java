package com.ttonway.tpms.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.ttonway.tpms.SPManager;

import java.util.ArrayList;

/**
 * Created by junjie on 2016/3/31.
 */
public class MediaPlayerQueue {
    private static final String TAG = MediaPlayerQueue.class.getSimpleName();

    static MediaPlayerQueue instance;

    private Context mContext;
    private MediaPlayer mPlayer;
    private ArrayList<Integer> mResources;
    private final MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playAndSetData();
        }
    };

    public static synchronized MediaPlayerQueue getInstance() {
        if (instance == null) {
            instance = new MediaPlayerQueue();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (mPlayer != null) {
            return;
        }
        mContext = context;
        mResources = new ArrayList<>();
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(mOnCompletionListener);
    }

    public void release() {
        if (mPlayer == null) {
            return;
        }
        mContext = null;
        mPlayer.setOnCompletionListener(null);
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mPlayer.release();
        mPlayer = null;
        mResources.clear();
        mResources = null;
    }

    public void addresource(int resource) {
        if (!SPManager.getBoolean(mContext, SPManager.KEY_VOICE_OPEN, true)) {
            Log.w(TAG, "voice closed");
            return;
        }
        if (mPlayer == null) {
            Log.e(TAG, "queue not initialized");
            return;
        }
        if (mResources.size() > 3) {
            Log.e(TAG, "too much resources in the queue");
            return;
        }
        Log.d(TAG, "addresource success");
        mResources.add(resource);
        readyPlayer();
    }


    private void readyPlayer() {
        if (!mPlayer.isPlaying()) {
            playAndSetData();
        }
    }


    private void playAndSetData() {
        try {
            if (mResources.size() == 0) {
                return;
            }
            Integer resId = mResources.remove(0);
            mPlayer.reset();
            mPlayer.setDataSource(mContext, Uri.parse("android.resource://"
                    + mContext.getPackageName() + "/" + resId.intValue()));
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "playAndSetData fail.", e);
        }
    }
}