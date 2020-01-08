package com.example.companycheckinbyfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

public class EnterPost extends AppCompatActivity {

    EditText title,content;
    Button summit,back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_post);
        ActionBar ab=getSupportActionBar();
        ab.hide();
        title=findViewById(R.id.et_titleE);
        content=findViewById(R.id.et_contentE);
        summit=findViewById(R.id.btn_submitE);
        back=findViewById(R.id.btn_backE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnterPost.this, MainActivity.class));
                finish();
            }
        });

        summit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database;
                final DatabaseReference myRef;
                database = FirebaseDatabase.getInstance();
                myRef=database.getReference();
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String date_Time = sDateFormat.format(new java.util.Date());
                        myRef.child("post"+(dataSnapshot.getChildrenCount()))
                                .child("title").setValue(title.getText().toString());
                        myRef.child("post"+(dataSnapshot.getChildrenCount()))
                                .child("content").setValue(content.getText().toString());
                        myRef.child("post"+(dataSnapshot.getChildrenCount()))
                                .child("time").setValue(date_Time);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(EnterPost.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
                    }
                });
                startActivity(new Intent(EnterPost.this, MainActivity.class));
                finish();
            }
        });
    }
}
