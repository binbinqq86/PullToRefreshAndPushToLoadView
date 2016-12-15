package com.binbin.pulltorefreshandpushtoloadview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends AppCompatActivity implements View.OnClickListener {

    private Button btLv,btGv,btRv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btLv= (Button) findViewById(R.id.btlv);
        btGv= (Button) findViewById(R.id.btgv);
        btRv= (Button) findViewById(R.id.btrv);

        btRv.setOnClickListener(this);
        btLv.setOnClickListener(this);
        btGv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btlv:
                startActivity(new Intent(Main.this,ListViewActivity.class));
                break;
            case R.id.btgv:
                startActivity(new Intent(Main.this,GridViewActivity.class));
                break;
            case R.id.btrv:
                startActivity(new Intent(Main.this,RecyclerViewActivity.class));
                break;
        }
    }
}
