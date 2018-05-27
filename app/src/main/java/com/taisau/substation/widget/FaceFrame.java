package com.taisau.substation.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.taisau.substation.R;


/**
 * Created by Administrator on 2016/6/22 0022.
 * 自定义的一个人脸框控件
 */
public class FaceFrame extends View {

    private Rect mBound;
    private Paint mPaint;
    private Context mContext;
    private RectF rect1 = new RectF(), rect2 = new RectF(), rect3 = new RectF(), rect4 = new RectF();

    public FaceFrame(Context context) {
        this(context, null);
    }

    public FaceFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mPaint = new Paint();
        mBound = new Rect();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FaceFrame(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = 0;
        int height = 0;

        /**
         * 设置宽度
         */
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                width = getPaddingLeft() + getPaddingRight() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                width = getPaddingLeft() + getPaddingRight() + mBound.width();
                break;
        }

        /**
         * 设置高度
         */
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:// 明确指定了
                height = getPaddingTop() + getPaddingBottom() + specSize;
                break;
            case MeasureSpec.AT_MOST:// 一般为WARP_CONTENT
                height = getPaddingTop() + getPaddingBottom() + mBound.height();
                break;
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        try {
            mPaint.setColor(mContext.getResources().getColor(R.color.color_00f6ff));
//            canvas.drawRect(0, 0, getMeasuredWidth(), 4, mPaint);
//            canvas.drawRect(0, 0, 4, getMeasuredHeight(), mPaint);
//            canvas.drawRect(0, getMeasuredHeight() - 4, getMeasuredWidth(), getMeasuredHeight(), mPaint);
//            canvas.drawRect(getMeasuredWidth() - 4, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
            rect1.set(0, 0, getMeasuredWidth(), 4);
            canvas.drawRoundRect(rect1, 2, 2, mPaint);
            rect2.set(0, 0, 4, getMeasuredHeight());
            canvas.drawRoundRect(rect2, 2, 2, mPaint);
            rect3.set(0, getMeasuredHeight() - 4, getMeasuredWidth(), getMeasuredHeight());
            canvas.drawRoundRect(rect3, 2, 2, mPaint);
            rect4.set(getMeasuredWidth() - 4, 0, getMeasuredWidth(), getMeasuredHeight());
            canvas.drawRoundRect(rect4, 2, 2, mPaint);
        } catch (Resources.NotFoundException | NullPointerException e) {
            e.printStackTrace();
        }


    }

    //绘制人脸框
    //imageView 为自定义的view
    public void createFrame(FrameLayout faceFrame, long[] position, int pic_width, int pic_height) {
        int frameWidth = faceFrame.getWidth();
        int frameHeight = faceFrame.getHeight();
        if (getParent() == null)
            faceFrame.addView(this);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.setMargins((pic_width - (int) position[2]) * frameWidth / pic_width, (int) position[1] * frameHeight / pic_height, 0, 0);
        params.height = ((int) position[3] - (int) position[1]) * frameHeight / pic_height;
        params.width = ((int) position[2] - (int) position[0]) * frameWidth / pic_width;
        setLayoutParams(params);
    }
}
