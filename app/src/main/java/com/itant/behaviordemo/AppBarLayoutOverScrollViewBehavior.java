package com.itant.behaviordemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class AppBarLayoutOverScrollViewBehavior extends AppBarLayout.Behavior {
    private static final String TAG = "overScroll";
    private static final float TARGET_HEIGHT = 500;
    private View imageView;
    private int mParentHeight;
    private boolean isAnimate;


    // 我的
    private int rawAppbarBottom = 0;
    private int currentAppbarBottom = 0;
    private int rawImageHeight = 0;
    private int currentImageHeight = 0;
    private int rawAppbarHeight = 0;
    private int currentAppbarHeight = 0;

    public AppBarLayoutOverScrollViewBehavior() {
    }

    public AppBarLayoutOverScrollViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int layoutDirection) {
        boolean handled = super.onLayoutChild(parent, abl, layoutDirection);
        // 需要在调用过super.onLayoutChild()方法之后获取
        if (imageView == null) {
            imageView = parent.findViewWithTag(TAG);
            if (imageView != null) {
                initial(abl);
            }
        }
        return handled;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        isAnimate = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        return isAnimate;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        if (imageView != null && ((dy < 0 && child.getBottom() >= mParentHeight) || (dy > 0 && child.getBottom() > mParentHeight))) {
            scale(child, target, dy);
        } else {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY) {
        if (Math.abs(velocityY) > 100) {
            isAnimate = false;
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl, View target, int type) {
        recovery(abl);
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
    }

    private void initial(AppBarLayout abl) {
        mParentHeight = abl.getHeight();
        abl.setClipChildren(false);
        rawAppbarBottom = abl.getBottom();
        rawImageHeight = imageView.getHeight();
        currentImageHeight = rawImageHeight;
        currentAppbarBottom = rawAppbarBottom;
        rawAppbarHeight = abl.getHeight();
        currentAppbarHeight = rawAppbarHeight;
    }

    private void scale(AppBarLayout abl, View target, int dy) {
        currentImageHeight += -dy;
        currentImageHeight = (int) Math.min(currentImageHeight, rawImageHeight+TARGET_HEIGHT);
        currentAppbarBottom = rawAppbarBottom + currentImageHeight-rawImageHeight;
        //ViewCompat.setScaleX(imageView, mLastScale);
        //ViewCompat.setScaleY(imageView, mLastScale);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = currentImageHeight;
        imageView.setLayoutParams(params);

        currentAppbarHeight = rawAppbarHeight+currentImageHeight-rawImageHeight;
        ViewGroup.LayoutParams ablParams = abl.getLayoutParams();
        ablParams.height = currentAppbarHeight;
        abl.setLayoutParams(ablParams);
        //abl.setBottom(currentAppbarBottom);
        //target.setScrollY(0);
    }

    private void recovery(final AppBarLayout abl) {
        if (currentImageHeight > rawImageHeight) {
            if (isAnimate) {
                ValueAnimator anim = ValueAnimator.ofInt(0, currentImageHeight-rawImageHeight).setDuration(200);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        //ViewCompat.setScaleX(imageView, value);
                        //ViewCompat.setScaleY(imageView, value);
                        ViewGroup.LayoutParams params = imageView.getLayoutParams();
                        params.height = currentImageHeight-value;
                        imageView.setLayoutParams(params);


                        //abl.setBottom(rawAppbarBottom +currentImageHeight-rawImageHeight-value);
                        currentAppbarHeight = rawAppbarHeight+currentImageHeight-rawImageHeight-value;
                        ViewGroup.LayoutParams ablParams = abl.getLayoutParams();
                        ablParams.height = currentAppbarHeight;
                        abl.setLayoutParams(ablParams);
                        if (value == currentImageHeight-rawImageHeight) {
                            currentImageHeight = rawImageHeight;
                            currentAppbarHeight = rawAppbarHeight;
                        }
                    }
                });
                anim.start();
            } else {
                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.height = rawImageHeight;
                imageView.setLayoutParams(params);
                abl.setBottom(rawAppbarBottom);
            }
        }
    }
}