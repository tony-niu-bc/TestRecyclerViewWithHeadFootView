package com.example.wzhnsc.testrecyclerviewwithheadfootview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mDatas;

    private ListFragment mListFragment;
    private Handler mHandler = new Handler();

    // 添加数据
    private void addData() {
        for (int i = 0; i < 13; i++) {
            mDatas.add("加载后条目--" + (mDatas.size() + 1));
        }
    }

    public void newData() {
        mDatas.clear();

        for (int i = 0; i < 13; i++) {
            mDatas.add("条目--" + (mDatas.size() + 1));
        }
    }

    private class MyRunnable implements Runnable {

        boolean isRefresh;

        MyRunnable(boolean isRefresh) {
            this.isRefresh = isRefresh;
        }

        @Override
        public void run() {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRefresh) {
                        newData();
                        mListFragment.refreshComplate();
                    }
                    else {
                        addData();
                        mListFragment.loadMoreComplate();
                    }
                }
            }, 2000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDatas = new ArrayList<>();

        mListFragment = ListFragment.newInstance(mDatas);

        // 设置刷新和加载更多数据的监听，分别在 onRefresh() 和 onLoadMore() 方法中执行刷新和加载更多操作
        mListFragment.setLoadDataListener(new RecyclerViewWithHeadFootView.LoadDataListener() {
            @Override
            public void onRefresh() {
                new Thread(new MyRunnable(true)).start();
            }

            @Override
            public void onLoadMore() {
                new Thread(new MyRunnable(false)).start();
            }
        });

        getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mListFragment)
                            .commit();
    }
}
