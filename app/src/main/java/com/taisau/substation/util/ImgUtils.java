package com.taisau.substation.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Base64;

import com.GFace;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.taisau.substation.util.Constant.LIB_DIR;

/**
 * Created by Administrator on 2017/2/22 0022.
 */

public class ImgUtils {
    private static final String tempFile = LIB_DIR + "/temp_mod.png";
    private static ImgUtils utils = new ImgUtils();

    public static ImgUtils getUtils() {
        return utils;
    }

    public float[] getImgFea(Bitmap temp) {
        int[] pixels = new int[temp.getHeight() * temp.getWidth()];
        temp.getPixels(pixels, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());
        byte[] rgb = new byte[temp.getHeight() * temp.getWidth()];
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = (byte) RgbToGray(pixels[i]);
        }
        byte[] ret = GFace.detectFace(rgb, temp.getWidth(), temp.getHeight());
        if (ret != null && ret[0] > 0) {
            GFace.FaceInfo aa = GFace.getFaceInfo(ret);
            GFace.FacePointInfo info = aa.info[0];
            float[] fea = GFace.getFea(rgb, temp.getWidth(), temp.getHeight(), (int) info.ptEyeLeft.x, (int) info.ptEyeLeft.y, (int) info.ptEyeRight.x, (int) info.ptEyeRight.y,
                    (int) info.ptNose.x, (int) info.ptNose.y, (int) info.ptMouthLeft.x, (int) info.ptMouthLeft.y,
                    (int) info.ptMouthRight.x, (int) info.ptMouthRight.y);
            return fea;
        } else
            return null;
        // DetectInfo info=new DetectInfo();
        // Mat crop = null;
        // info.setBb(GFace.detectFace(rgb, temp.getWidth(), temp.getHeight()));

    /*if (info.getBb()!=null) {
        info.initFacePos();
        crop=cropImg(info.getPos(),tempFile);
    }
    else
        return null;
    if (crop!=null)
    {
        if (Imgcodecs.imwrite(LIB_DIR+"/temp_crop.jpg",crop))
        {
            float []res= CaffeMobile.getBlobTest(LIB_DIR+"/temp_crop.jpg");
            return res;
        }
    }*/
        // return null;
    }

    public byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the
        return temp;
    }

    public void saveBitmap(Bitmap bm, String path) {

        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (NullPointerException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int RgbToGray(int r, int g, int b) {
        return (r * 30 + g * 59 + b * 11) / 100;
    }

    public int RgbToGray(int xrgb) {
        return RgbToGray((xrgb >> 16) & 0xff,
                (xrgb >> 8) & 0xff,
                (xrgb) & 0xff);
    }

    public float[] std_points = new float[]{89.3095f, 72.9025f, 169.3095f, 72.9025f, 127.8949f, 127.0441f, 96.8796f, 184.8907f, 159.1065f, 184.7601f};



    public Bitmap adjustBitmap(Bitmap bitmap, GFace.FaceInfo aa, int scaleCount){

        int x, right;
        if (aa.rc[0].left > 30)
            x = (int) aa.rc[0].left - 30;
        else if (aa.rc[0].left > 0 && aa.rc[0].left <= 30)
            x = (int) aa.rc[0].left;
        else
            x = 0;
        if (aa.rc[0].right < (bitmap.getWidth()/scaleCount-30))
            right = (int) aa.rc[0].right + 30;
        else if (aa.rc[0].right >= (bitmap.getWidth()/scaleCount-30) && aa.rc[0].right < bitmap.getWidth()/scaleCount)
            right = (int) aa.rc[0].right;
        else
            right = bitmap.getWidth()/scaleCount;
        int y, height;
        if (aa.rc[0].top > 60)
            y = (int) aa.rc[0].top - 60;
        else
            y = 0;
        if (aa.rc[0].bottom < (bitmap.getHeight()/scaleCount-100))
            height = (int) aa.rc[0].bottom + 100;
        else
            height = bitmap.getHeight()/scaleCount;
        if (x < 0 || x > bitmap.getWidth()/scaleCount)
            x = 0;
        if (y < 0 || y > bitmap.getHeight()/scaleCount)
            y = 0;
        if (right > bitmap.getWidth()/scaleCount || right < 0)
            right = bitmap.getWidth()/scaleCount;
        if (height > bitmap.getHeight()/scaleCount || height < 0)
            height = bitmap.getHeight()/scaleCount;
        if (right - x <= bitmap.getWidth()/scaleCount && height - y <= bitmap.getHeight()/scaleCount && x >= 0 && y >= 0) {
            x = x * scaleCount;
            y = y * scaleCount;
            right = right * scaleCount;
            height = height * scaleCount;
            bitmap = Bitmap.createBitmap(bitmap, x, y, right - x, height - y);
        }
        return bitmap;
    }

    /**
     *
     * @param bitmap
     * @param orientationDegree 0 - 360 范围
     * @return
     */
    public Bitmap adjustPhotoRotation(Bitmap bitmap, int orientationDegree) {


        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bitmap.getHeight();
            targetY = 0;
        } else {
            targetX = bitmap.getHeight();
            targetY = bitmap.getWidth();
        }


        final float[] values = new float[9];
        matrix.getValues(values);


        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];


        matrix.postTranslate(targetX - x1, targetY - y1);


        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(),
                Bitmap.Config.ARGB_8888);


        Paint paint = new Paint();
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(bitmap, matrix, paint);


        return canvasBitmap;
    }

    /**
     * base64ToBitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    /**
     * bitmap转为base64
     */
    public String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 把Bitmap转Byte[]
     */
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * 按长方形裁切图片
     */
    public static Bitmap cropBitmapWithRect(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        if (w <= newWidth || h <= newHeight) {
            throw new IllegalArgumentException("目标bitmap的宽高不能大于等于原始bitmap宽高");
        }
        int retX = (w - newWidth) / 2;
        int retY = (h - newHeight) / 2;

        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, newWidth, newHeight, null, false);
        if (!bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 按比例缩放
     */
    public static Bitmap scaleBitmap(Bitmap origin, float scale) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }
}
