package com.example.newstep.Util;

import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    private static final long DOUBLE_CLICK_DELAY = 300;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_DELAY) {
            onDoubleClick(v);
            lastClickTime = 0;
        } else {
            lastClickTime = clickTime;
        }
    }

    public abstract void onDoubleClick(View v);
}