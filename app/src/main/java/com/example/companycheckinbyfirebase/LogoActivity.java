package com.example.companycheckinbyfirebase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LogoActivity extends AppCompatActivity {

    Handler do_thing_later;

    class change_activity implements Runnable{
        @Override
        public void run() {
            Intent go=new Intent(LogoActivity.this,MainActivity.class);
            LogoActivity.this.startActivity(go);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        ActionBar ab=getSupportActionBar();
        ab.hide();
        do_thing_later= new Handler();
        change_activity change=new change_activity();
        do_thing_later.postDelayed(change,2000);
    }
}
