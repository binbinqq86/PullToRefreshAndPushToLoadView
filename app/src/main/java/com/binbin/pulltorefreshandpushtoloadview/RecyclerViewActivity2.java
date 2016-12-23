package com.binbin.pulltorefreshandpushtoloadview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.binbin.pulltorefreshandpushtoloadview.view.PullToRefreshAndPushToLoadView3;
import com.binbin.pulltorefreshandpushtoloadview.view.PullToRefreshAndPushToLoadView4;

public class RecyclerViewActivity2 extends AppCompatActivity {

    private PullToRefreshAndPushToLoadView4 pullToRefreshAndPushToLoadView;
    private RecyclerView recyclerView;
    private String[] items = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_activity_main2);
        pullToRefreshAndPushToLoadView = (PullToRefreshAndPushToLoadView4) findViewById(R.id.prpt);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
//        items=new String[]{};
        adapter = new MyAdapter(items,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new GridLayoutManager(this,2);
//        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation(),false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        pullToRefreshAndPushToLoadView.setOnRefreshListener(new PullToRefreshAndPushToLoadView4.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        }, 0);
    }

    private void refresh() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(200 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pullToRefreshAndPushToLoadView.finishRefreshing();
            }
        }.start();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private String[] datas;
        private Context mContext;
        public MyAdapter(String[] datas,Context mContext){
            this.datas=datas;
            this.mContext=mContext;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item, parent,
                    false));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.tv.setText(datas[position]);
        }

        @Override
        public int getItemCount() {
            return datas.length;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv;

            public MyViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.tv);
            }
        }
    }
}
