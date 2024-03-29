package com.maybe.maybe.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SpeedyLinearLayoutManager extends LinearLayoutManager {

    private int height, childCount, posDiff;
    private Activity activity;

    public SpeedyLinearLayoutManager(Context context) {
        super(context);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPosDiff(int posDiff) {
        this.posDiff = posDiff;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return super.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                float singleHeight = ((float) height) / childCount;
                float distancePx = singleHeight * posDiff;
                float ydpi = 0;
                //wait for api 34
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
                    Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
                    ydpi = (windowMetrics.getBounds().height() - insets.top - insets.bottom) / windowMetrics.getDensity();
                } else {*/
                DisplayMetrics dm = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                ydpi = dm.ydpi;
                //}
                float distanceIn = (float) distancePx / ydpi;
                float speed = 1000 / distanceIn;//default is 25f
                return speed / displayMetrics.densityDpi;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}