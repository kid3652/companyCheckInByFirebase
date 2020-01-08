package com.example.companycheckinbyfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText name,password;
    private Button btn_login,btn_register,btn_back;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = FirebaseDatabase.getInstance();

        ActionBar ab=getSupportActionBar();
        ab.hide();
        findview();

        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    private void findview(){
        name=findViewById(R.id.user_name);
        password=findViewById(R.id.user_pass);
        btn_login=findViewById(R.id.btn_login);
        btn_register=findViewById(R.id.btn_register);
        btn_back=findViewById(R.id.btn_Back);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_register) {
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        }else if(v.getId()==R.id.btn_Back){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }else if(v.getId()==R.id.btn_login) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            //如果未連線的話，mNetworkInfo會等於null
            if(mNetworkInfo != null){
                if (name.getText().toString().equals("") || password.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "欄位不可為空", Toast.LENGTH_SHORT).show();
                } else {
                    String login_name = name.getText().toString();
                    final String login_pass = password.getText().toString();
                    myRef= database.getReference("account").child(login_name);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                if(dataSnapshot.child("password").getValue().toString().trim().equals(login_pass)){
                                    Toast.makeText(LoginActivity.this, "登錄成功", Toast.LENGTH_LONG).show();
                                    SharedPrefManager.getInstance(LoginActivity.this)
                                            .userLogin(new User(dataSnapshot.child("name").getValue().toString(),
                                                    dataSnapshot.getKey(),
                                                    dataSnapshot.child("password").getValue().toString()));
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    LoginActivity.this.finish();
                                }else{
                                    Toast.makeText(LoginActivity.this, "帳號密碼錯誤", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(LoginActivity.this, "帳號密碼錯誤", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(LoginActivity.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }else{
                Toast.makeText(LoginActivity.this,"請開啟網路",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
