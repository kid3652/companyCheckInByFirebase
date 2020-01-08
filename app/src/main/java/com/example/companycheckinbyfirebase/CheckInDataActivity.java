package com.example.companycheckinbyfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckInDataActivity extends AppCompatActivity {

    ListView checkInData;
    Button backC;
    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_data);
        ActionBar ab=getSupportActionBar();
        ab.hide();
        checkInData = findViewById(R.id.list_checkInData);
        backC=findViewById(R.id.btn_backC);
        bar=findViewById(R.id.progressBar2);

        backC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInDataActivity.this, MainActivity.class));
                finish();
            }
        });


        bar.setVisibility(View.VISIBLE);
        String account=SharedPrefManager.getInstance(this).getUser().getAccount();
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("account/"+account);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<HashMap<String, String>> arrayList=new ArrayList();
                HashMap<String, String> hashMap = null;
                for(int i=0;i<10;i++) {
                    if(dataSnapshot.child("checkIn"+((int)dataSnapshot.getChildrenCount()-i)).exists()) {
                        hashMap = (HashMap) dataSnapshot.child("checkIn" + ((int) dataSnapshot.getChildrenCount() - i)).getValue();
                        arrayList.add(hashMap);
                        Log.d("111111",arrayList+"  i="+i);
                    }
                }
                SimpleAdapter adapter = new SimpleAdapter(
                        CheckInDataActivity.this,
                        arrayList,
                        R.layout.checkin_data_style,
                        new String[]{"address", "time"},
                        new int[]{R.id.tv_address, R.id.date_time}
                );
                checkInData.setAdapter(adapter);
                bar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CheckInDataActivity.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
