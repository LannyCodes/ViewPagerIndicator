package com.customviewpagerindicator;

import android.os.Bundle;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends FragmentActivity {


    private ViewPager mViewpager;
    private ViewPagerIndicator mViewPagerIndicator;

    private List<String> mTitles = Arrays.asList("短信1", "收藏2", "推荐3", "短信4",
            "收藏5", "推荐6", "短信7", "收藏8", "推荐9");

    private List<VpSimpleFragment> mContents = new ArrayList<VpSimpleFragment>();

    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        setContentView(R.layout.activity_main);
        initViews();
        initDatas();

        mViewPagerIndicator.setVisibleTabCount(4);
        mViewPagerIndicator.setTabItemTitles(mTitles);

        mViewpager.setAdapter(mAdapter);
        mViewPagerIndicator.setViewPager(mViewpager, 0);
    }


    private void initDatas() {
        for (String title : mTitles) {
            VpSimpleFragment fragment = VpSimpleFragment.newInstance(title);
            mContents.add(fragment);
        }

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mContents.get(position);
            }

            @Override
            public int getCount() {
                return mContents.size();
            }
        };

    }

    /**
     * 初始化视图
     */
    private void initViews() {

        mViewpager = findViewById(R.id.viewpager);

        mViewPagerIndicator = findViewById(R.id.indicator);
    }
}
