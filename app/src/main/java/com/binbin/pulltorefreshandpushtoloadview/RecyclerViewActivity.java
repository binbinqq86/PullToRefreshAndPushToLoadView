package com.binbin.pulltorefreshandpushtoloadview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.binbin.pulltorefreshandpushtoloadview.listview.PullToRefreshAndPushToLoadView3;

public class RecyclerViewActivity extends AppCompatActivity {

    private PullToRefreshAndPushToLoadView3 pullToRefreshAndPushToLoadView;
    //    private ListView listView;
    private GridView gridView;
    private String[] items = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_activity_main);
        pullToRefreshAndPushToLoadView = (PullToRefreshAndPushToLoadView3) findViewById(R.id.prpt);
//        listView = (ListView) findViewById(R.id.lv);
        gridView = (GridView) findViewById(R.id.gv);
        adapter = new ArrayAdapter<String>(this, R.layout.item, items);
//        listView.setAdapter(adapter);
        gridView.setAdapter(adapter);
        pullToRefreshAndPushToLoadView.setOnRefreshListener(new PullToRefreshAndPushToLoadView3.PullToRefreshListener() {
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
}
