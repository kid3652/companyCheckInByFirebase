package com.example.companycheckinbyfirebase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PostActivity extends AppCompatActivity {

    private TextView tv_PostTitle,tv_Content;
    private Button backP;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ActionBar ab=getSupportActionBar();
        ab.hide();
        findview();

        Intent intent = this.getIntent();
        source = intent.getStringExtra("source");
        loadData();
        Log.d("111111111111",source);
        backP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    private void findview(){
        tv_PostTitle = findViewById(R.id.tv_PostTitle);
        tv_Content = findViewById(R.id.tv_Content);
        backP=findViewById(R.id.btn_backP);
    }

    private void loadData(){
        Log.d("1111111","source"+source);
        int begin=source.indexOf("content=");
        int end=source.indexOf(", title=");
        if(end>begin){
            tv_PostTitle.setText(source.split("title=")[1].replace("}",""));
            tv_Content.setText(source.substring(begin,end).replace("content=","").replace("  ","\n\n"));
        }else{
            tv_PostTitle.setText(source.split("title=")[1].split(", content=")[0]);
            tv_Content.setText(source.split("content=")[1].replace("  ","\n\n").replace("}",""));
        }
    }
}
