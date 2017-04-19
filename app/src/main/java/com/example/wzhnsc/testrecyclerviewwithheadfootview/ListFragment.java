package com.example.wzhnsc.testrecyclerviewwithheadfootview;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    private static final String ARG_DATAS = "arg_datas";

    private List<String> mDatas;

    public static ListFragment newInstance(ArrayList<String> datas) {
        Bundle b = new Bundle();
        b.putStringArrayList(ARG_DATAS, datas);

        ListFragment f = new ListFragment();
        f.setArguments(b);

        return f;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(getActivity());
            view.setGravity(Gravity.CENTER);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                            DimensionUtil.dip2px(getActivity(), 50)));

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.mTextView.setText(mDatas.get(position));
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView;
        }
    }

    private RecyclerViewWithHeadFootView mRecyclerView;
    private RecyclerViewWithHeadFootView.LoadDataListener mLoadDataListener;

    // 设置刷新和加载更多数据的监听
    public void setLoadDataListener(RecyclerViewWithHeadFootView.LoadDataListener listener) {
        mLoadDataListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDatas = getArguments().getStringArrayList(ARG_DATAS);

        if (null == mRecyclerView) {
            // 自定义的RecyclerView, 也可以在布局文件中正常使用
            mRecyclerView = new RecyclerViewWithHeadFootView(getActivity());

            // 使用重写后的线性布局管理器
            MyLinearLayoutManager manager = new MyLinearLayoutManager(getActivity(),
                                                                      MyLinearLayoutManager.VERTICAL,
                                                                      false);
            mRecyclerView.setLayoutManager(manager);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), manager.getOrientation(), true));
            // 设置适配器
            mRecyclerView.setAdapter(new MyAdapter());
            // 设置刷新和加载更多数据的监听，分别在onRefresh()和onLoadMore()方法中执行刷新和加载更多操作
            mRecyclerView.setLoadDataListener(mLoadDataListener);

            // 刷新
            mRecyclerView.setRefresh(true);
        }

        return mRecyclerView;
    }

    public void refreshComplate() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
        // 刷新完成后调用，必须在UI线程中
        mRecyclerView.refreshComplate();
    }

    public void loadMoreComplate() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
        // 加载更多完成后调用，必须在UI线程中
        mRecyclerView.loadMoreComplate();
    }
}
