package com.taisau.substation.ui.main.utils;

import android.os.Handler;
import android.os.HandlerThread;

import com.GFace;

/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class FeaAction {

    HandlerThread getFeaInThread;
    Handler handler;

    public enum FEA_CASE {
        DO_CHECK_WHITE, DO_FACE_COMPARE
    }

    public byte[] data;
    public GFace.FacePointInfo point;
    public GFace.FaceInfo info;

    public FeaAction() {
    }

    public float[] doFeaAction(FEA_CASE feaCase, byte[] data, GFace.FaceInfo face) {
        switch (feaCase) {
            case DO_CHECK_WHITE:
                this.info = face;
                return getFeaInThread(data, face.info[0]);
            case DO_FACE_COMPARE:
                return getFeaInMain(data, face.info[0]);
        }
        return null;
    }

    public float[] getFeaInThread(final byte[] data, final GFace.FacePointInfo point) {
        this.point = point;
        this.data = data;
//        handler.sendEmptyMessage(0);
        return null;
    }

    public float[] getFeaInMain(final byte[] data, final GFace.FacePointInfo point) {
        float[] com_fea = null;
        if (data != null) {
            com_fea = GFace.getFea(data, 300, 200, (int) point.ptEyeLeft.x, (int) point.ptEyeLeft.y, (int) point.ptEyeRight.x, (int) point.ptEyeRight.y,
                    (int) point.ptNose.x, (int) point.ptNose.y, (int) point.ptMouthLeft.x, (int) point.ptMouthLeft.y,
                    (int) point.ptMouthRight.x, (int) point.ptMouthRight.y);
        }

        return com_fea;
    }
}
