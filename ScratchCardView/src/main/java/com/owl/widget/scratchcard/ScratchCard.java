package com.owl.widget.scratchcard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Scratch Card View
 * Created by Alamusi on 2017/5/16.
 */

public class ScratchCard extends View {

    private Paint mOutPaint;

    private Path mPath;

    private Canvas mCanvas;

    private Bitmap mBitmap;

    private int mPointX;
    private int mPointY;

    private String mText;

    private Paint mTextPaint;
    private Rect mTextBound;
    private int mTextSize;
    private int mTextColor;
    /**
     * 遮盖层区域是否达到消除阈值的标志
     */
    private volatile boolean mClearFlag;
    private OnScratchCompleteListener mScratchCompleteListener;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int width = getWidth();
            int height = getHeight();

            float wipeArea = 0;
            float totalArea = width * height;

            Bitmap bitmap = mBitmap;

            int[] mPixels = new int[width * height];

            bitmap.getPixels(mPixels, 0, width, 0, 0, width, height);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int index = i + j * width;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percentage = (int) (wipeArea * 100 / totalArea);
                if (percentage > 60) {
                    mClearFlag = true;
                    postInvalidate();
                }
            }
        }
    };

    private Bitmap mOutBitmap;

    public ScratchCard(Context context) {
        this(context, null);
    }

    public ScratchCard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScratchCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScratchCard, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ScratchCard_text) {
                mText = a.getString(attr);

            } else if (attr == R.styleable.ScratchCard_textColor) {
                mTextColor = a.getColor(attr, Color.BLACK);

            } else if (attr == R.styleable.ScratchCard_textSize) {
                mTextSize = (int) a.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));

            }
        }
        a.recycle();

        init();
    }

    private void init() {
        mOutPaint = new Paint();
        mPath = new Path();

        mTextPaint = new Paint();
        mTextBound = new Rect();
        mClearFlag = false;

        setupOutPaint();
        setupTextPaint();

        mOutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scratch_foreground);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                int width = getMeasuredWidth();
                int height = getMeasuredHeight();

                mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);

                mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutPaint);
                mCanvas.drawBitmap(mOutBitmap, null, new Rect(0, 0, width, height), null);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPointX = x;
                mPointY = y;
                mPath.moveTo(mPointX, mPointY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mPointX);
                int dy = Math.abs(y - mPointY);
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mPointX = x;
                mPointY = y;
                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2, getHeight() / 2 + mTextBound.height() / 2, mTextPaint);
        if (mClearFlag) {
            if (mScratchCompleteListener != null) {
                mScratchCompleteListener.onComplete();
            }
        }
        if (!mClearFlag) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    private void drawPath() {
        mOutPaint.setStyle(Paint.Style.STROKE);
        mOutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutPaint);
    }

    /**
     * 设置绘制Path画笔的属性
     */
    private void setupOutPaint() {
        mOutPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutPaint.setAntiAlias(true);
        mOutPaint.setDither(true);
        mOutPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutPaint.setStyle(Paint.Style.FILL);
        mOutPaint.setStrokeWidth(20);
    }

    /**
     * 设置绘制内部信息的画笔属性
     */
    private void setupTextPaint() {
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);
        // 获得当前画笔绘制文本的宽和高
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
        mTextPaint.setColor(mTextColor);
    }

    public void setScratchCompleteListener(OnScratchCompleteListener scratchCompleteListener) {
        mScratchCompleteListener = scratchCompleteListener;
    }

    public void setText(String text) {
        mText = text;
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    public interface OnScratchCompleteListener {
        void onComplete();
    }
}