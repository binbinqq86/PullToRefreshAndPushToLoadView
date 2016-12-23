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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.binbin.pulltorefreshandpushtoloadview.view.PullToRefreshAndPushToLoadView5;

public class PushAndPullActivity extends AppCompatActivity {

    private PullToRefreshAndPushToLoadView5 pullToRefreshAndPushToLoadView;
    private String[] items = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pullToRefreshAndPushToLoadView = new PullToRefreshAndPushToLoadView5(this);
        setContentView(pullToRefreshAndPushToLoadView);

        list();
//        grid();
//        recycler();
        
        pullToRefreshAndPushToLoadView.setOnRefreshAndLoadMoreListener(new PullToRefreshAndPushToLoadView5.PullToRefreshAndPushToLoadMoreListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {

            }
        }, 0);
    }

    private void list(){
        ListView listView = new ListView(this);
        pullToRefreshAndPushToLoadView.addView(listView);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.item, items));
    }

    private void grid(){
        GridView gridView = new GridView(this);
        gridView.setNumColumns(3);
        pullToRefreshAndPushToLoadView.addView(gridView);
        gridView.setAdapter(new ArrayAdapter<String>(this, R.layout.item, items));
    }

    private void recycler(){
        RecyclerView recyclerView = new RecyclerView(this);
//        items=new String[]{};
        recyclerView.setAdapter(new MyAdapter(items,this));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new GridLayoutManager(this,2);
//        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void refresh() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(2 * 1000);
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
        public void onBindViewHolder(final MyAdapter.MyViewHolder holder, final int position) {
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
