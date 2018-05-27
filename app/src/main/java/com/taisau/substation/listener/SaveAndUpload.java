package com.taisau.substation.listener;

import android.graphics.Bitmap;

/**
 * Created by whx on 2017-11-14
 */

public class SaveAndUpload {

    private static OnSaveAndUploadListener listener;

    public static void setOnSaveAndUploadListener(OnSaveAndUploadListener onSaveAndUploadListener) {
        listener = onSaveAndUploadListener;
    }

    public static void OnSaveAndUpload(final long historyId, final Bitmap face, final String card_path,
                                       final String face_path, final String com_status, final float score,
                                       final String time, final String registerId, final int result,final int compareType) {
        if (listener != null) {
            listener.OnSaveAndUpload(historyId, face, card_path, face_path,
                    com_status, score, time, registerId, result,compareType);
        }
    }
    public static void OnUploadIdentityCard(String[] info){
        if(listener != null){
            listener.OnUploadIdentityCard(info);
        }
    }
    public interface OnSaveAndUploadListener {
        void OnSaveAndUpload(final long historyId, final Bitmap face, final String card_path,
                             final String face_path, final String com_status, final float score,
                             final String time, final String registerId, final int result,final int compareType);
        void OnUploadIdentityCard(String[] info);
    }
}
