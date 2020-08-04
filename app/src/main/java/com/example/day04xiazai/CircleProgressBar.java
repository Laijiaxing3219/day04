package com.example.day04xiazai;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import io.reactivex.annotations.Nullable;

/**
 * 1.写一个类,继承View/ViewGroup,复写构造
 * 2.在values新建一个attrs.xml,在里面去定义自定义的属性
 * 3.在布局xml中使用自定义View及自定义属性
 * 4.在自定义View中获取自定义的属性
 * 5.绘制自己想要的效果,在绘制的过程中使用自定义的属性
 */
public class CircleProgressBar extends View{
    public String TAG = "CircleProgressBar";

    private float mSweepAngle;
    private float mTextSize;
    private int mTextColor;
    private int mCircleColor;
    private int mRingColor;
    private float mRingWidth;
    private float mStartAngle;
    private RectF mRectF;
    private Paint mRingPaint;
    private int mCx;
    private int mCy;
    private int mRadius;
    private Paint mCirclePaint;
    private String mText;
    private Paint mTextPaint;
    private float mDy;

    public CircleProgressBar(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param attrs 这个就是属性的集合,系统将我们再布局xml中写的属性封装成了这个对象
     */
    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //4.在自定义View中获取自定义的属性
        //通过这个TypedArray对象去获取到属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        if (ta != null){
            mRingColor = ta.getColor(R.styleable.CircleProgressBar_ringColor, 0);
            mCircleColor = ta.getColor(R.styleable.CircleProgressBar_circleColor, 0);
            mTextColor = ta.getColor(R.styleable.CircleProgressBar_android_textColor, 0);

            mRingWidth = ta.getDimension(R.styleable.CircleProgressBar_ringWidth, 20);
            mTextSize = ta.getDimension(R.styleable.CircleProgressBar_android_textSize, 20);

            mStartAngle = ta.getFloat(R.styleable.CircleProgressBar_startAngle, -90);
            mSweepAngle = ta.getFloat(R.styleable.CircleProgressBar_sweepAngle, 0);

            mText = ta.getString(R.styleable.CircleProgressBar_android_text);
            // 30px   10dp   3:1
            // px = dp * dpi / 160
            //dpi : 屏幕像素密度 480/inch 每英寸上像素的个数,1 inch = 2.54cm
            Log.d(TAG, "ringWidth: "+mRingWidth+",mSweepAngle:"+mSweepAngle);
            //关闭回收资源
            ta.recycle();
        }

        //环的画笔
        mRingPaint = new Paint();
        mRingPaint.setColor(mRingColor);
        mRingPaint.setStrokeWidth(mRingWidth);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setStyle(Paint.Style.STROKE);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);//水平居中

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        mDy = textHeight/2 - fontMetrics.descent;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //判断使用者给定的控件是否未正方形,如果不是,强制改为正方形
        int newMeasureSpec = widthMeasureSpec;
        int max = Math.max(width, height);
        if (width != height){
            newMeasureSpec = MeasureSpec.makeMeasureSpec(max,MeasureSpec.EXACTLY);
        }
        super.onMeasure(newMeasureSpec, newMeasureSpec);

        //环的矩形区域
        mRectF = new RectF(0.1f*max, 0.1f*max, 0.9f*max, 0.9f*max);

        //内圆圆心
        mCx = max/2;
        mCy = max/2;
        //半径
        mRadius = max/4;
    }

    //5.绘制自己想要的效果,在绘制的过程中使用自定义的属性
    @Override
    protected void onDraw(Canvas canvas) {
        //1.画环
        //扇形的矩形区域需要由使用者
        canvas.drawArc(mRectF,mStartAngle,mSweepAngle,false,mRingPaint);
        //2.画内圆
        canvas.drawCircle(mCx,mCy,mRadius,mCirclePaint);
        //3.画文字
        canvas.drawText(mText,mCx,mCy+mDy,mTextPaint);
    }


    /**
     *
     * @param progress 0 到100
     */
    public void setProgresss(int progress) {
        mSweepAngle = progress * 3.6f;
        mText = progress +" %";
        //重新绘制,ui线程调用
        //invalidate();
        //非ui线程调用这个刷新界面
        postInvalidate();
    }
}
