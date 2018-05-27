package com.taisau.substation.util;

import android.content.Context;
import android.graphics.Paint;

import com.orhanobut.logger.Logger;

/**
 * Created by ds on 2016/12/15.
 */
public class DisplayUtil {

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     *            （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //android 的默认字体大小为12.0
    /**
     * 获得默认字体的高度
     */
    public static int getTextHeight(Paint paint) {
        Paint.FontMetrics sF = paint.getFontMetrics();
        return   (int) Math.ceil(sF.descent - sF.top) + 2;
    }

    /**
     * 获得默认字体的高度
     */
    public static int getFontHeight(float fontSize)
    {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.ascent) + 2;
    }


    /**
     * 获得默认字体的宽度
     */
    public static int getFontWidth(Paint paint, String str){
        return Math.round(paint.measureText(str));
    }

    public static void testpx2dp(Context context, float px){
        Logger.e("DisplayUtil", "testpx2dp:"+"px = "+px +", dp = "+px2dip(context,px) );
    }
    public static void testpx2dps(Context context, float[] pxs){
        for (float px :pxs){
            testpx2dp(context,px);
        }
    }
    public static void testpx2sp(Context context, float px){
        Logger.e("DisplayUtil", "testpx2sp:"+"px = "+px +", sp = "+px2sp(context,px) );
    }
    public static void testpx2sps(Context context, float[] pxs){
        for (float px :pxs){
            testpx2sp(context,px);
        }
    }
}
