package com.example.wzhnsc.testrecyclerviewwithheadfootview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecyclerViewWithHeadFootView extends RecyclerView {

    private Context mContext;

    private View mHeadView;
    private View mFootView;

    private int mHeadViewDefaultHeight = -1; // 默认高度
    private int mHeadViewMaxHeight     = -1; // 最大高度

    private boolean mIsTouching = false; // 是否正在手指触摸的标识

    private boolean mIsLoadingData = false; // 是否正在加载数据

    private LoadDataListener mLoadDataListener;

    // 刷新和加载更多数据的监听接口
    public interface LoadDataListener {
        // 执行刷新
        void onRefresh();
        // 执行加载更多
        void onLoadMore();
    }

    // 刷新数据
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // 如下代码实现下拉时 Head View 的高度
            int headViewScaleHeight = mHeadView.getHeight() - mHeadViewDefaultHeight;

//            Log.e("RecyclerHeadFoot",
//                  "handleMessage - mHeadView.getLayoutParams().height = " + mHeadView.getLayoutParams().height + "\n" +
//                  "mHeadView.getHeight() = " + mHeadView.getHeight() + "\n" +
//                  "mHeadViewDefaultHeight = " + mHeadViewDefaultHeight + "\n" +
//                  "mHeadViewMaxHeight = " + mHeadViewMaxHeight + "\n" +
//                  "headViewScaleHeight = " + headViewScaleHeight + "\n" +
//                  "msg.arg1 = " + msg.arg1);

            if (headViewScaleHeight < (mHeadViewMaxHeight - mHeadViewDefaultHeight) / 3) {
                mHeadView.getLayoutParams().height -= msg.arg1;
            }
            else if (headViewScaleHeight > (mHeadViewMaxHeight - mHeadViewDefaultHeight) / 3 * 2) {
                mHeadView.getLayoutParams().height -= msg.arg1 / 3 * 2;
            }
            else {
                mHeadView.getLayoutParams().height -= msg.arg1 / 3 * 1.5;
            }

            mHeadView.setVisibility(VISIBLE);

            if (mHeadView.getHeight() >= mHeadViewMaxHeight) {
                ((TextView) mHeadView.findViewById(R.id.tv_header_view)).setText("Prepare to refresh ...");
                mHeadView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

            mHeadView.requestLayout();
        }
    }

    private Handler mHandler = new MyHandler();

    private MyLinearLayoutManager.OverScrollListener mOverScrollListener = new MyLinearLayoutManager.OverScrollListener() {
        @Override
        public void overScrollBy(int dy) {
            //Log.e("RecyclerHeadFoot", "overScrollBy - dy: " + dy);

            // dy 为滑动过度时每毫秒拉伸的距离，正数表示向上拉伸，负数表示向下拉伸
            if (!mIsLoadingData
             && mIsTouching
             && (dy < 0)
             && (mHeadView.getHeight() < mHeadViewMaxHeight)) {
//              || (dy > 0 && mHeadView.getHeight() > mHeadViewDefaultHeight))) {
                mHandler.obtainMessage(0, dy, 0, null)
                        .sendToTarget();
            }
        }
    };

    // 带有 Head or Foot 的适配器
    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {

        private RecyclerView.Adapter mAdapter;

        public WrapAdapter(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        // 判断当前位置是不是 Head View
        public boolean isHeader(int position) {
            return (0 == position);
        }

        // 判断当前位置是不是 Foot View
        public boolean isFooter(int position) {
            return ((getItemCount() - 1) == position);
        }

        @Override
        public int getItemCount() {
            // 多加 2 是因为包含 Head & Foot View
            if (null != mAdapter) {
                return (mAdapter.getItemCount() + 2);
            }
            else {
                return 2;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HeadViewHolder.class.getCanonicalName().hashCode()) {
                Log.e("RecyclerHeadFoot", "onCreateViewHolder - HeadOrFootViewHolder(mHeadView) - " + viewType);
                return new HeadViewHolder(mHeadView);
            }
            else if (viewType == FootViewHolder.class.getCanonicalName().hashCode()) {
                Log.e("RecyclerHeadFoot", "onCreateViewHolder - HeadOrFootViewHolder(mFootView) - " + viewType);
                return new FootViewHolder(mFootView);
            }

            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            // Head or Foot View 无需绑定数据
            if (isHeader(position) || isFooter(position)) {
                return;
            }

            if (null != mAdapter) {
                // 除去 Head or Foot View 外
                mAdapter.onBindViewHolder(holder, (position - 1));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeader(position)) {
                Log.e("RecyclerHeadFoot", "onCreateViewHolder - isHeader(position) - " + HeadViewHolder.class.getCanonicalName().hashCode());
                return HeadViewHolder.class.getCanonicalName().hashCode();
            }

            if (isFooter(position)) {
                Log.e("RecyclerHeadFoot", "onCreateViewHolder - isFooter(position) - " + FootViewHolder.class.getCanonicalName().hashCode());
                return FootViewHolder.class.getCanonicalName().hashCode();
            }

            return mAdapter.getItemViewType(position - 1);
        }

        private class HeadViewHolder extends RecyclerView.ViewHolder {
            HeadViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class FootViewHolder extends RecyclerView.ViewHolder {
            FootViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public RecyclerViewWithHeadFootView(Context context) {
        this(context, null);
    }

    public RecyclerViewWithHeadFootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewWithHeadFootView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setOverScrollMode(OVER_SCROLL_NEVER);
        post(new Runnable() {
            @Override
            public void run() {
                ((MyLinearLayoutManager)getLayoutManager()).setOverScrollListener(mOverScrollListener);
                // 脚部先隐藏
                mFootView.setVisibility(GONE);
            }
        });
    }

    // 设置刷新和加载更多数据的监听
    public void setLoadDataListener(LoadDataListener listener) {
        mLoadDataListener = listener;
    }

    // 加载更多数据完成后调用，必须在 UI 线程中
    public void loadMoreComplate() {
        mIsLoadingData = false;

        mFootView.setVisibility(GONE);
    }

    // 刷新数据完成后调用，必须在UI线程中
    public void refreshComplate() {
        mIsLoadingData = false;

        mHeadView.setVisibility(GONE);

        shrinkHeadView();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mHeadView = LayoutInflater.from(mContext).inflate(R.layout.header_view, null);
        mHeadViewDefaultHeight = 0; // 就是不显示
        // 此例子的 Head View 为 130 dp
        mHeadViewMaxHeight = DimensionUtil.dip2px(mContext, 130);

        mFootView = LayoutInflater.from(mContext).inflate(R.layout.footer_view, this, false);

        // 使用包装了头部和脚部的适配器
        Adapter mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

//        Log.e("RecyclerHeadFoot",
//              "onScrollStateChanged - state = " + state + "\n" +
//              "mIsLoadingData = " + mIsLoadingData + "\n" +
//              "mLoadDataListener = " + mLoadDataListener + "\n");

        // 当前不滚动，且不是正在刷新或加载数据
        if ((RecyclerView.SCROLL_STATE_IDLE == state)
         && (null != mLoadDataListener)
         && !mIsLoadingData) {
            // 获取最后一个正在显示的条目位置
            int lastVisibleItemPosition = ((MyLinearLayoutManager)getLayoutManager()).findLastVisibleItemPosition();

//            Log.e("RecyclerHeadFoot",
//                  "onScrollStateChanged - getLayoutManager().getChildCount() = " + getLayoutManager().getChildCount() + "\n" +
//                  "lastVisibleItemPosition = " + lastVisibleItemPosition + "\n");

            if ((getLayoutManager().getChildCount() > 0)
             && (lastVisibleItemPosition >= (getLayoutManager().getItemCount() - 1))) {
                mFootView.setVisibility(VISIBLE);

//                Log.e("RecyclerHeadFoot", "onScrollStateChanged - Prepare to load more!");

                // 加载更多
                mIsLoadingData = true;

                mLoadDataListener.onLoadMore();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouching = true;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mIsTouching = false;

                if (mHeadView.getHeight() > mHeadViewDefaultHeight) {
//                    Log.e("RecyclerHeadFoot",
//                          "onTouchEvent - mHeadView.getHeight() = " + mHeadView.getHeight() + "\n" +
//                          "mIsLoadingData = " + mIsLoadingData + "\n" +
//                          "mLoadDataListener = " + mLoadDataListener + "\n" +
//                          "mHeadViewMaxHeight = " + mHeadViewMaxHeight);

                    if ((mHeadView.getHeight() >= mHeadViewMaxHeight)
                     && (null != mLoadDataListener && !mIsLoadingData)) {
                        refresh();
                    }

                    shrinkHeadView();

                    return true;
                }

                break;
        }

        return super.onTouchEvent(ev);
    }

    // 负责收缩 Head View
    private void shrinkHeadView() {
        ((TextView)mHeadView.findViewById(R.id.tv_header_view)).setText("I'm head view!");
        mHeadView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        if (mHeadView.getHeight() > mHeadViewDefaultHeight) {
            ValueAnimator animator = ValueAnimator.ofInt(mHeadView.getHeight(),
                                                         0);
            animator.setDuration(300);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mHeadView.getLayoutParams().height = (int)animation.getAnimatedValue();

//                    Log.e("RecyclerHeadFoot",
//                          "onAnimationUpdate - mHeadView.getLayoutParams().height = " +
//                          mHeadView.getLayoutParams().height);

                    mHeadView.requestLayout();
                }
            });

            animator.start();
        }
    }

    // 设置是否执行刷新
    public void setRefresh(boolean isRefrsh) {
        if (isRefrsh) {
            refresh();
        }
        else {
            refreshComplate();
        }
    }

    // 刷新
    private void refresh() {
        mIsLoadingData = true;

        mLoadDataListener.onRefresh();
    }
}
