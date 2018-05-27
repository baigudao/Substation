package com.taisau.substation.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;

import com.taisau.substation.ui.WelcomeActivity;

import java.util.List;

/**
 * Created by Administrator on 2017/7/13 0013.
 */

public class CameraUtils2 {

    private Context context;
    private Camera.ErrorCallback callback;
    private AlertDialog dialog;

    public enum FAIL_STATUS {
        NORMAL, NO_NUMBER_OF_CAMERA, NOT_FIND_CAMERA,NO_SUPPORT_PREVIEW_SIZE
    }
    public static Camera camera;
    //摄像头参数
    public int cameraFrontIndex = -1;
    public int cameraBackIndex = -1;
    public int cameraIndex = -1;
    public static FAIL_STATUS status= FAIL_STATUS.NOT_FIND_CAMERA;
    public boolean support720 = false;
    public boolean support1080 = false;
    public Handler handler=new Handler();
    public  CameraUtils2(Context context, Camera.ErrorCallback callback) {
        this.callback=callback;
        this.context=context;
        initCamera();
    }
    public void initCamera(){
        FAIL_STATUS s = FAIL_STATUS.NORMAL;
        setCameraIndex();
        if (s == FAIL_STATUS.NORMAL)
            startCamera();
        if (s== FAIL_STATUS.NORMAL&&camera!=null)
            camera.setErrorCallback(callback);
        else
        {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog = new AlertDialog.Builder(context).setTitle("摄像头始化失败")
                            .setMessage("请确认摄像头是否有效，或者重新插拔摄像头点击确定键重新初始化")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (camera!=null)
                                        releaseCamera();
                                    initCamera();
                                }
                            })
                            .setNegativeButton("关闭应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setClass(context, WelcomeActivity.class);
                                    intent.putExtra("exit_flag", "exit_id");
                                    context.startActivity(intent);
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    initCamera();
                }
            }
        }, 5000);
    }
    public static void setStatus(FAIL_STATUS s) {
        status = s;
    }

    public static FAIL_STATUS getStatus() {
        return status;
    }

    /**
     * 获取当前相机预览画面所使用的摄像头
     *
     * @return
     */
    public int getCameraIndex() {
        return cameraIndex;
    }

    /**
     * 获取android.hardware.Camera 生成的实例对象
     *
     * @return
     */

    public Camera getCamera() {
        return camera;
    }


    /**
     * 设置相机为前置摄像头
     *
     * @return true为设置成功 false为没有前置摄像头
     */
    public boolean setFrontIndex() {
        if (cameraFrontIndex != -1) {
            cameraIndex = cameraFrontIndex;
            return true;
        } else
            return false;
    }


    /**
     * 设置相机为前置摄像头
     *
     * @return true为设置成功 false为没有前置摄像头
     */
    public boolean setBackIndex() {
        if (cameraFrontIndex != -1) {
            cameraIndex = cameraFrontIndex;
            return true;
        } else
            return false;
    }

    /**
     * @return -1尚未生成系统camera
     * 1设置尺寸成功
     */
    public int checkSupportPreviewSize() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizes) {
                if (!support720 && size.width == 1280 && size.height == 720)
                    support720 = true;
                if (!support1080 && size.width == 1920 && size.height == 1080)
                    support1080 = true;
            }
            return 0;
        } else
            return -1;
    }


    /**
     * 用于获取手机摄像头信息
     * 判断手机是否有摄像头
     * cameraFrontIndex为前摄像头
     * cameraBackIndex为后置摄像头
     * 默认打开前置摄像头,如果没有则为后置
     */
    public void setCameraIndex() {
        if (Camera.getNumberOfCameras() > 0)
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraFrontIndex = i;
                }
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraBackIndex = i;
                }
                if (cameraFrontIndex != -1)
                    cameraIndex = cameraFrontIndex;
                else if (cameraBackIndex != -1)
                    cameraIndex = cameraBackIndex;
            }
        else {
            status = FAIL_STATUS.NO_NUMBER_OF_CAMERA;
        }
    }


    // 初始化Camera
    public int startCamera() {
            //打开摄像头
            try {
                camera = Camera.open(cameraIndex);
                if (camera != null) {
                    //   camera.setDisplayOrientation(90);
                    //设置旋转90度
                    // camera.setDisplayOrientation(180);
                    // 通过SurfaceView显示取景画面
                    checkSupportPreviewSize();
                    Camera.Parameters parameters = camera.getParameters();
                    if (support720) {
                        parameters.setPreviewSize(1280, 720);
                    } else if (support1080) {
                        parameters.setPreviewSize(1920, 1080);
                    }
                    else {
                        status = FAIL_STATUS.NO_SUPPORT_PREVIEW_SIZE;
                        return -1;
                    }
                    /*    parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);*/
                    //parameters.setExposureCompensation();
                       /* System.out.println("facedetect support:"+camera.getParameters().getMaxNumDetectedFaces());
                        System.out.println("iso:"+parameters.get("iso-values")+" now："+parameters.get("iso"));
                        // parameters.set("iso","ISO3200");
                        System.out.println("support type:"+supportType);*/
                    camera.setParameters(parameters);
                    // 开始预览
                    // 自动对焦
                }
                else
                    status= FAIL_STATUS.NOT_FIND_CAMERA;
            } catch (Exception e) {
                e.printStackTrace();
                status= FAIL_STATUS.NOT_FIND_CAMERA;
                return -2;
            }
        return 0;
    }

    // 释放摄像头图像显示
    public void releaseCamera() {
        // 释放摄像头
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
