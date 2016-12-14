package com.binbin.pulltorefreshandpushtoloadview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.binbin.pulltorefreshandpushtoloadview.listview.PullToRefreshAndPushToLoadView;
import com.binbin.pulltorefreshandpushtoloadview.listview.PullToRefreshAndPushToLoadView2;

public class MainActivity extends AppCompatActivity {

    private PullToRefreshAndPushToLoadView2 pullToRefreshAndPushToLoadView;
    private ListView listView;
    private String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z" };
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pullToRefreshAndPushToLoadView = (PullToRefreshAndPushToLoadView2) findViewById(R.id.activity_main);
        listView = (ListView) findViewById(R.id.lv);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        pullToRefreshAndPushToLoadView.setOnRefreshListener(new PullToRefreshAndPushToLoadView2.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(50*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pullToRefreshAndPushToLoadView.finishRefreshing();
            }
        }, 0);
    }
}
