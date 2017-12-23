package com.wpy.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

/**
 * Created by dell on 2017/12/23.
 */

public class SlidingMenu extends HorizontalScrollView {
    //菜单宽度
    private int mMenuWidth;
    //手势检测
    private GestureDetector mGestureDetector;
    private boolean mMenuIsOpen = false;
    //判断是否为拦截操作
    private boolean isIntercept = false;
    private View mMenuView;
    private ImageView mShadowView;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        float dimension = array.getDimension(R.styleable.SlidingMenu_rightPadding, dip2px(context, 60));
        //屏幕的宽度 - 菜单据有的距离
        mMenuWidth = (int) (getScreenWidth(context) - dimension);
        array.recycle();
        // 7.1 初始化手势处理类
        mGestureDetector = new GestureDetector(context, new mGestureListener());
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 滚动回调的方法  这个方法是不断的执行的
        // l 代表当前滚动的距离   scrollX   mMenuWidth --> 0
        // Log.e(TAG,l+"");
        // 8.处理菜单的抽屉效果  让菜单移动一段距离
        mMenuView.setTranslationX(l*0.8f);
        // 9.2 滑动到不同的位置改变其阴影透明度
        // 透明度肯定是一个 梯度值
        float scale = l*1f / mMenuWidth;// 600   600/600  1   300/600  0.5   0/600  0   1 --> 0
        float alphaScale = 1 - scale; // 0 -- > 1
        mShadowView.setAlpha(alphaScale);


    }

    //事件拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        isIntercept = false;
        if (mMenuIsOpen) {
            float x = ev.getX();
            if (x > mMenuWidth) {
                closeMenu();
                isIntercept = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private class mGestureListener extends GestureDetector.SimpleOnGestureListener {
        // 只需要复写快速滑动
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Log.e(TAG,velocityX+"");
            // Bug  判断左右还是上下   只有左右快速滑动才切换
            if (Math.abs(velocityY) > Math.abs(velocityX)) {
                // 代表上下快速划  这个时候不做处理
                return super.onFling(e1, e2, velocityX, velocityX);
            }

            // 向右边快速滑动 是一个 大于 0 的数
            // 向左边快速滑动 是一个 小于 0 的数
            // 逻辑  如果是菜单打开  向左边快速滑动的时候 应该切换菜单状态
            if (mMenuIsOpen) {
                if (velocityX < 0) {
                    toggleMenu();
                    return true;
                }
            }
            //       如果是菜单关闭  向右边快速滑动的时候 应该切换菜单状态
            else {
                if (velocityX > 0) {
                    toggleMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityX);
        }
    }

    /**
     * 7.切换菜单的状态
     */
    public void toggleMenu() {
        if (mMenuIsOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    //重新对布局进行处理
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取根布局
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("至少需要来两个布局");
        }
        //获取菜单布局
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuLayoutParams = mMenuView.getLayoutParams();
        menuLayoutParams.width = mMenuWidth;
        mMenuView.setLayoutParams(menuLayoutParams);

        //获取内容布局
        View mConTent = container.getChildAt(1);
        ViewGroup.LayoutParams layoutParams = mConTent.getLayoutParams();
        layoutParams.width = getScreenWidth(getContext());
        mConTent.setLayoutParams(layoutParams);


        // 9.处理内容的阴影效果
        // 9.1 思路在内容布局的外面加一层阴影  ImageView
        // 9.1.1 把原来的内容从根布局里面移除
        //移除老的布局
        container.removeView(mConTent);
        FrameLayout newFrameLayout = new FrameLayout(getContext());
        //把原来的布局添加到新的容器里
        newFrameLayout.addView(mConTent);
        // 9.1.2.2 把阴影加入新的内容容器
        mShadowView = new ImageView(getContext());
        mShadowView.setBackgroundColor(Color.parseColor("#99000000"));
        newFrameLayout.addView(mShadowView);
        // 9.1.3 把新的容器再放回原来的位置
        container.addView(newFrameLayout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //已走事件拦截
        if (isIntercept) {
            return true;
        }
        // 7.2 处理手指快速滑动  手势处理类使用  拦截
        if (mGestureDetector.onTouchEvent(ev)) {
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            //得到当前滑动的坐标
            int scrollX = getScrollX();
            if (scrollX > mMenuWidth / 2) {
                //关闭
                closeMenu();
            } else {
                //打开
                openMenu();
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    //打开菜单的方法
    private void openMenu() {
        mMenuIsOpen = true;
        smoothScrollTo(0, 0);
    }

    //关闭菜单的方法
    private void closeMenu() {
        mMenuIsOpen = false;
        smoothScrollTo(mMenuWidth, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //刚打开时  主页显示的是菜单内容
        scrollTo(mMenuWidth, 0);
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * Dip into pixels
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
