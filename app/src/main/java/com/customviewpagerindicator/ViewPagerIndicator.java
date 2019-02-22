package com.customviewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import androidx.viewpager.widget.ViewPager;

/**
 * 自定义ViewPagerIndicator，参考慕课网hyman的视频写的
 * Created by lannyxu on 2019/2/21.
 */
public class ViewPagerIndicator extends LinearLayout {

    //用于三角形的画笔
    private Paint mPaint;

    // 用于绘制三角形的边
    private Path mPath;
    // 三角形的宽
    private int mTriangleWidth;
    // 三角形的高
    private int mTriangleHeight;

    private static final float RADIO_TRIANGLE_WIDTH = 1 / 6F;// 用于设置三角形的宽和tab底边的比例，用于屏幕适配
    /**
     * 三角形底边的最大宽度
     */
    private final float DIMENSION_TRIANGLE_WIDTH_MAX = getScreenWidth() / 3 * RADIO_TRIANGLE_WIDTH;


    private int mInitTranslationX;//第一个三角形初始化的偏移位置
    private int mTranslateX;// 移动时候的三角形偏移位置

    private int mTabVisibleCount;// 可见tab的数量

    private static final int COUNT_DEFAULT_TAB = 4;// 默认可见tab为4个

    private List<String> mTitles;// 接收传递过来的title
    private static final int COLOR_TEXT_NORMAL = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT_HIGHLIGHT = Color.parseColor("#FF4CDA0F");


    public ViewPagerIndicator(Context context) {
        this(context, null);
    }


    public ViewPagerIndicator(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attribute = context.obtainStyledAttributes(attrs,
                R.styleable.ViewPagerIndicator);

        mTabVisibleCount = attribute.getInt(
                R.styleable.ViewPagerIndicator_visible_tab_count, COUNT_DEFAULT_TAB);

        if (mTabVisibleCount < 0) {
            mTabVisibleCount = COUNT_DEFAULT_TAB;
        }

        //用完必须释放
        attribute.recycle();

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#FFFFFF"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3));
    }

    /**
     * 重写dispatchDraw方法绘制字内容
     *
     * @param canvas
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        canvas.save();
        //getHeight()代表三角形在Linearlayout的底部
        canvas.translate(mInitTranslationX + mTranslateX, getHeight() + 2);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    /**
     * 设置三角形的大小
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // w/3为每个tab的宽度，目前可见为3个
        mTriangleWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGLE_WIDTH);
        //选取最小的那一个作为宽
        mTriangleWidth = (int) Math.min(mTriangleWidth, DIMENSION_TRIANGLE_WIDTH_MAX);
        // 第一个三角形的偏移位置
        mInitTranslationX = w / mTabVisibleCount / 2 - mTriangleWidth / 2;
        initTriangle();
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {
        // mTriangleHeight = mTriangleWidth / 2;
        // 将三角形角度设置为30度
        mTriangleHeight = (int) (mTriangleWidth / 2 * Math.tan(Math.PI / 6));

        mPath = new Path();
        mPath.moveTo(0, 0);//0，0不是屏幕左上角的坐标，这里的0，0表示以canvas为坐标系，初学者很多对这里表示迷惑
        mPath.lineTo(mTriangleWidth, 0);
        mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
        // 关闭当前轮廓，完成闭合
        mPath.close();

    }

    /**
     * 三角形跟随ViewPager移动
     *
     * @param position
     * @param positionOffset
     */
    public void scroll(int position, float positionOffset) {

        int tabWidth = getWidth() / mTabVisibleCount;
        mTranslateX = (int) (tabWidth * (position + positionOffset));


        /**
         * 容器移动,在tab处于移动至最后一个时
         */
        if (position >= (mTabVisibleCount - 2) && positionOffset > 0 &&
                getChildCount() > mTabVisibleCount) {
            if (mTabVisibleCount != 1) {
                this.scrollTo((position - (mTabVisibleCount - 2)) * tabWidth +
                        (int) (tabWidth * positionOffset), 0);
            } else {
                this.scrollTo(position * tabWidth + (int) (tabWidth * positionOffset), 0);
            }
        }

        invalidate();//重绘

    }

    /**
     * xml加载完成之后，回调此方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LinearLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.weight = 0;
            params.width = getScreenWidth() / mTabVisibleCount;
            view.setLayoutParams(params);


        }

        setItemClickEvent();
    }

    /**
     * 动态获取tab的数量
     *
     * @param count
     */
    public void setVisibleTabCount(int count) {
        mTabVisibleCount = count;
    }

    /**
     * 动态设置tab
     *
     * @param titles
     */
    public void setTabItemTitles(List<String> titles) {
        if (titles != null && titles.size() > 0) {
            this.removeAllViews();
            mTitles = titles;
            for (String title : mTitles) {
                this.addView(generateTextView(title));
            }
            //设置title点击关联
            setItemClickEvent();
        }
    }

    /**
     * 根据title创建tab
     *
     * @param title
     * @return
     */
    private View generateTextView(String title) {

        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        );
        params.width = getScreenWidth() / mTabVisibleCount;
        textView.setText(title);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(COLOR_TEXT_NORMAL);
        textView.setLayoutParams(params);
        return textView;
    }


    // 接收关联的ViewPager
    private ViewPager mViewPager;

    /**
     * 提供一个接口供外部ViewPager使用
     */
    public interface PageOnChangeListener {
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    public PageOnChangeListener mListener;

    public void setViewPagerOnPageChangeListener(PageOnChangeListener listener) {
        mListener = listener;
    }

    /**
     * 设置关联的ViewPager
     *
     * @param viewPager
     * @param position
     */
    public void setViewPager(ViewPager viewPager, int position) {
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mListener != null) {
                    mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }

                // 三角形跟随ViewPager移动的距离就是：
                // tabWidth*positionOffset+position*tabWidth
                scroll(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {

                if (mListener != null) {
                    mListener.onPageSelected(position);

                }

                highLightTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);
                }
            }
        });

        mViewPager.setCurrentItem(position);
        highLightTextView(position);
    }

    /**
     * 高亮被点击的tab
     *
     * @param position
     */
    private void highLightTextView(int position) {

        resetTextViewColor();
        View view = getChildAt(position);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(COLOR_TEXT_HIGHLIGHT);
        }


    }

    /**
     * 重置tab文本颜色
     */
    private void resetTextViewColor() {

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
            }
        }
    }

    /**
     * 设置Tab的点击事件
     */
    private void setItemClickEvent() {

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    private int getScreenWidth() {

        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE
        );
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
