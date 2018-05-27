package com.taisau.substation.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2017/2/22 0022.
 */

public class YUVUtils {
    /**
     * YUV缩放一半
     * @param data  原数据
     * @param imageWidth    宽度
     * @param imageHeight   高度
     * @return  返回数据
     */
    public static byte[] halveYUV420(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth/2 * imageHeight/2];
        // halve yuma
        int i = 0;
        for (int y = 0; y < imageHeight; y+=2) {
            for (int x = 0; x < imageWidth; x+=2) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // halve U and V color components
       /* for (int y = 0; y < imageHeight / 2; y+=2) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv;
    }
    public static byte[] scaleYUV300_200(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[3000*200];
        int rx=imageWidth/2-300;
        int ry=imageHeight/2-200;
        // halve yuma
        int i = 0;
        for (int y = ry; y < imageHeight-ry; y+=2) {
            for (int x = rx; x < imageWidth-rx; x+=2) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // halve U and V color components
       /* for (int y = 0; y < imageHeight / 2; y+=2) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv;
    }
    public static byte[] scaleYUV480_360(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[480*360];
        // halve yuma
        int i = 0;
        for (int y = 0; y < 720; y+=2) {
            for (int x = 160; x < 1120; x+=2) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // halve U and V color components
       /* for (int y = 0; y < imageHeight / 2; y+=2) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv;
    }
    /**
     * YUV缩放1/3(提高速度)
     * @param data  原数据
     * @param imageWidth    宽度
     * @param imageHeight   高度
     * @return  返回数据
     */
    public static byte[] oneOfThreeYUV420(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth/3 * imageHeight/3];
        // halve yuma
        int i = 0;
        for (int y = 0; y < imageHeight; y+=3) {
            for (int x = 0; x < imageWidth; x+=3) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        /*
        // halve U and V color components
        for (int y = 0; y < imageHeight / 2; y+=2) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv;
    }

    /**
     * YUV缩放1/4(提高速度)
     * @param data  原数据
     * @param imageWidth    宽度
     * @param imageHeight   高度
     * @return  返回数据
     */
    public static byte[] quarterYUV420(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth/4 * imageHeight/4];
        // halve yuma
        int i = 0;
        for (int y = 0; y < imageHeight; y+=4) {
            for (int x = 0; x < imageWidth; x+=4) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        /*
        // halve U and V color components
        for (int y = 0; y < imageHeight / 2; y+=2) {
            for (int x = 0; x < imageWidth; x += 4) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i++;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x + 1)];
                i++;
            }
        }*/
        return yuv;
    }

    /**
     * YUV旋转90度
     *
     *  * @param dst  旋转后的数组
     * @param src   旋转前的数组
     * @param srcWidth  旋转前的图片宽度
     * @param srcHeight    旋转前的图片高度
     * @return
     */
    public static void rotateYUV420Degree90(byte[] dst, byte[] src, int srcWidth, int srcHeight) {
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (srcWidth != nWidth || srcHeight != nHeight) {
            nWidth = srcWidth;
            nHeight = srcHeight;
            wh = srcWidth * srcHeight;
            uvHeight = srcHeight >> 1;//uvHeight = height / 2
        }
        //旋转Y
        int k = 0;
        for (int i = 0; i < srcWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < srcHeight; j++) {
                dst[k] = src[nPos + i];
                k++;
                nPos += srcWidth;
            }
        }
        for (int i = 0; i < srcWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos + i];
                dst[k + 1] = src[nPos + i + 1];
                k += 2;
                nPos += srcWidth;
            }
        }
        return;
    }

    /**
     * YUV旋转270度
     *
     * @param dst  旋转后的数组
     * @param src   旋转前的数组
     * @param srcWidth  旋转前的图片宽度
     * @param height    旋转前的图片高度
     * @return
     */
    public static int rotateYUV420Degree270(byte[] dst, byte[] src, int srcWidth, int height) {

        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (srcWidth != nWidth || height != nHeight) {
            nWidth = srcWidth;
            nHeight = height;
            wh = srcWidth * height;
            uvHeight = height >> 1;//uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for (int i = 0; i < srcWidth; i++) {
            int nPos = srcWidth - 1;
            for (int j = 0; j < height; j++) {
                dst[k] = src[nPos - i];
                k++;
                nPos += srcWidth;
            }
        }

        for (int i = 0; i < srcWidth; i += 2) {
            int nPos = wh + srcWidth - 1;
            for (int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos - i - 1];
                dst[k + 1] = src[nPos - i];
                k += 2;
                nPos += srcWidth;
            }
        }
        return 0;
    }

    public static String saveYUV(String path, byte []data, int width, int height){
        YuvImage img = new YuvImage(data, ImageFormat.NV21, width,height, null);
        File file = new File(path);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            img.compressToJpeg(new Rect(0, 0, img.getWidth(),img.getHeight()), 100, stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
    public static byte[] saveYUV(byte[] data, int width, int height) {
        YuvImage img = new YuvImage(data, ImageFormat.NV21, width, height, null);
        try {
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            // Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, img.getWidth(), img.getHeight());
            img.compressToJpeg(rect, 100, output_stream);
            return output_stream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }
    public static Bitmap yuv2Bitmap(byte []data, int width, int height){
        YuvImage img = new YuvImage(data, ImageFormat.NV21, width,height, null);
        Bitmap bmp=null;
        try {
            if(img!=null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                img.compressToJpeg(new Rect(0, 0, img.getWidth(),img.getHeight()), 80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                //TODO：此处可以对位图进行处理，如显示，保存等
                stream.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return bmp;
    }
}
