package com.taisau.substation.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by whx on 2017-09-12
 */

public class SoundUtils {
    private static SoundUtils soundUtils;
    private static AudioManager audioManager;

    private SoundUtils() {
    }

    public static void init(Context context) {
        audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static SoundUtils getInstance() {
        if (soundUtils == null) {
            synchronized (SoundUtils.class) {
                if (soundUtils == null) {
                    soundUtils = new SoundUtils();
                }
            }
        }
        return soundUtils;
    }

    /**
     * 音量 +1
     * streamType 音乐流
     */
    public void addSound(int streamType) {
        int max = audioManager.getStreamMaxVolume(streamType);
        int current = audioManager.getStreamVolume(streamType);
        if (current < max) {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        } else {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    /**
     * 音量 -1
     * streamType 音乐流
     */
    public void decreaseSound(int streamType) {
        int current = audioManager.getStreamVolume(streamType);
        if (current > 0) {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        } else {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    /**
     * 音量 最大
     * streamType 音乐流
     */
    public void maxSound(int streamType) {
        int max = audioManager.getStreamMaxVolume(streamType);
        int current = audioManager.getStreamVolume(streamType);
        if (current != max) {
            audioManager.setStreamVolume(streamType, max, AudioManager.FLAG_PLAY_SOUND);
        } else {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    /**
     * 静音
     * streamType 音乐流
     */
    public void muteSound(int streamType) {
        audioManager.setStreamVolume(streamType, 0, AudioManager.FLAG_PLAY_SOUND);
    }

    /**
     * 获取目标音乐流的最大音量
     */
    public int getMaxVolume(int streamType) {
        return audioManager.getStreamMaxVolume(streamType);
    }

    /**
     * 直接设置音量
     */
    public void setSound(int streamType, int num) {
        int max = audioManager.getStreamMaxVolume(streamType);
        if (num > max || num < 0) {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
        } else {
            audioManager.setStreamVolume(streamType, num, AudioManager.FLAG_PLAY_SOUND);
        }
    }
    /**
     * 直接设置音量
     */
    public void setSoundMute(int streamType, int num) {
        int max = audioManager.getStreamMaxVolume(streamType);
        if (num > max || num < 0) {
            audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_SAME, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        } else {
            audioManager.setStreamVolume(streamType, num, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    /**获取目标流的当前音量*/
    public int getCurrentVolume(int streamType) {
        return audioManager.getStreamVolume(streamType);
    }
}
