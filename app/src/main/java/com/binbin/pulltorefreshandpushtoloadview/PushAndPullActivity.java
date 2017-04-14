package com.binbin.pulltorefreshandpushtoloadview;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.binbin.pulltorefreshandpushtoloadview.view.PullToRefreshAndPushToLoadView5;
import com.binbin.pulltorefreshandpushtoloadview.view.PullToRefreshAndPushToLoadView6;

public class PushAndPullActivity extends AppCompatActivity {

    private static final String TAG = "PushAndPullActivity";
    private PullToRefreshAndPushToLoadView6 pullToRefreshAndPushToLoadView;
    private String[] items = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    private String[] items2={"A", "B", "C"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_list_activity_main);
        pullToRefreshAndPushToLoadView = (PullToRefreshAndPushToLoadView6) findViewById(R.id.prpt);
//        pullToRefreshAndPushToLoadView.setCanRefresh(false);
//        pullToRefreshAndPushToLoadView.setCanLoadMore(false);
//        pullToRefreshAndPushToLoadView.setCanAutoLoadMore(true);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pullToRefreshAndPushToLoadView.autoRefresh();
//            }
//        },2000);

//        grid();
//        list();
        recycler();

        pullToRefreshAndPushToLoadView.setOnRefreshAndLoadMoreListener(new PullToRefreshAndPushToLoadView6.PullToRefreshAndPushToLoadMoreListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {
                load();
            }
        }, 0);
    }

    private void list(){
//        items=new String[]{};
        ListView listView = new ListView(this);
        ViewGroup.MarginLayoutParams mlp=new ViewGroup.MarginLayoutParams(-1,-1);
//        mlp.leftMargin=100;
        listView.setLayoutParams(mlp);
        pullToRefreshAndPushToLoadView.addView(listView);
//        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.item, items));
        listView.setAdapter(new MyLVAdapter(items,this));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PushAndPullActivity.this,"===setOnItemClickListener======"+position,Toast.LENGTH_SHORT).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PushAndPullActivity.this,"===setOnItemLongClickListener======"+position,Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void grid(){
        GridView gridView = new GridView(this);
        gridView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
        gridView.setNumColumns(3);
        pullToRefreshAndPushToLoadView.addView(gridView);
        gridView.setAdapter(new ArrayAdapter<String>(this, R.layout.item, items));
    }

    private View recycler(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
//        items=new String[]{};
        recyclerView.setAdapter(new MyAdapter(items,this));
        recyclerView.setHasFixedSize(true);
//        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this, Color.DKGRAY,2,2);
        dividerItemDecoration.setDrawBorderTopAndBottom(true);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return recyclerView;
    }

    private void refresh() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pullToRefreshAndPushToLoadView.finishRefreshing();
            }
        }.start();
    }

    private void load() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pullToRefreshAndPushToLoadView.finishLoading();
            }
        }.start();
    }

    class MyLVAdapter extends BaseAdapter{

        private String[] datas;
        private Context mContext;
        public MyLVAdapter(String[] datas,Context mContext){
            this.datas=datas;
            this.mContext=mContext;
        }


        @Override
        public int getCount() {
            return datas.length;
        }

        @Override
        public Object getItem(int position) {
            return datas[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            if(convertView==null){
                convertView=LayoutInflater.from(mContext).inflate(R.layout.item,null);
                viewHolder=new ViewHolder();
                viewHolder.tv= (TextView) convertView.findViewById(R.id.tv);
                convertView.setTag(viewHolder);
            }else{
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.tv.setText(datas[position]);
            viewHolder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(PushAndPullActivity.this,"onClick====="+position,Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(PushAndPullActivity.this,"onLongClick====="+position,Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(PushAndPullActivity.this).setTitle("hello").setMessage(position+":"+items[position]).setNegativeButton("cancel",null).show();
                    return true;
                }
            });
            return convertView;
        }

    }

    static class ViewHolder{
        TextView tv;
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
            holder.tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(PushAndPullActivity.this,"onClick====="+holder.getAdapterPosition(),Toast.LENGTH_SHORT).show();
                }
            });
            holder.tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(PushAndPullActivity.this,"onLongClick====="+holder.getAdapterPosition(),Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(PushAndPullActivity.this).setTitle("hello").setMessage(holder.getAdapterPosition()+":"+items[holder.getAdapterPosition()]).setNegativeButton("cancel",null).show();
                    return true;
                }
            });
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
