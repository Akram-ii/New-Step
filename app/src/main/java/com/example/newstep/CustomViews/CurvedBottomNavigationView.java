package com.example.newstep.CustomViews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CurvedBottomNavigationView extends BottomNavigationView {

    private Path mPath;
    private Paint mPaint;
    private float curveRadius = 128f;
    private float animatedCenterX = 0;
    private int selectedIndex = 0;

    public CurvedBottomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(0xB36A96E6);
        mPaint.setAntiAlias(true);
    }

    public void setSelectedPosition(int index) {
        int itemCount = getMenu().size();
        if (itemCount == 0) return;

        float targetX = (getWidth() / (float) itemCount) * index + (getWidth() / (float) itemCount) / 2;

        if (animatedCenterX == targetX) return;

        ValueAnimator animator = ValueAnimator.ofFloat(animatedCenterX, targetX);
        animator.setDuration(350);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            animatedCenterX = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();

        selectedIndex = index;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawCurve(canvas);


    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();


        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getClass().getSimpleName().equals("BottomNavigationMenuView")) {
                child.setBackgroundColor(Color.parseColor("#E0DEFE"));


                child.setPadding(20, 20, 20, 20);
                child.setElevation(8f);


                GradientDrawable roundedBg = new GradientDrawable();

                roundedBg.setCornerRadii(new float[]{
                        40f, 40f,
                        40f, 40f,
                        0f, 0f,
                        0f, 0f
                });

                child.setBackground(roundedBg);
            }
        }
    }




    private void drawCurve(Canvas canvas) {
        mPath.reset();

        float navBarWidth = getWidth();
        float navBarHeight = getHeight();
        float curveHeight = 120;
        float cornerRadius = 40f;


        mPath.moveTo(0, cornerRadius);
        mPath.quadTo(0, 0, cornerRadius, 0);


        mPath.lineTo(animatedCenterX - curveRadius, 0);


        mPath.cubicTo(animatedCenterX - curveRadius / 2, 0,
                animatedCenterX - curveRadius / 2, curveHeight,
                animatedCenterX, curveHeight);

        mPath.cubicTo(animatedCenterX + curveRadius / 2, curveHeight,
                animatedCenterX + curveRadius / 2, 0,
                animatedCenterX + curveRadius, 0);


        mPath.lineTo(navBarWidth - cornerRadius, 0);


        mPath.quadTo(navBarWidth, 0, navBarWidth, cornerRadius);

        mPath.lineTo(navBarWidth, navBarHeight);
        mPath.lineTo(0, navBarHeight);
        mPath.close();

        canvas.drawPath(mPath, mPaint);
        setBackgroundColor(Color.TRANSPARENT);


    }


}



