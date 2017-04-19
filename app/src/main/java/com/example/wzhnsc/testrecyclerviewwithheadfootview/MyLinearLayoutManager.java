package com.example.wzhnsc.testrecyclerviewwithheadfootview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class MyLinearLayoutManager extends LinearLayoutManager {

    // 滑动过度的监听接口
    public interface OverScrollListener {

        // 滑动过度时调用的方法
        void overScrollBy(int dy); // 每毫秒滑动的距离
    }

    private OverScrollListener mListener;

    public MyLinearLayoutManager(Context context) {
        super(context);
    }

    public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public MyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollRange = super.scrollVerticallyBy(dy, recycler, state);

        mListener.overScrollBy(dy - scrollRange);

        return scrollRange;
    }

    // 设置滑动过度监听
    public void setOverScrollListener(OverScrollListener listener) {
        mListener = listener;
    }
}
