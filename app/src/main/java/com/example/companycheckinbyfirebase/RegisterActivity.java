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

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name,account,password;
    private Button btn_RegisterR,btn_HomeR;
    private HashMap accountHashMap;
    private String firebaseAccount,firebaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar ab=getSupportActionBar();
        ab.hide();
        findview();


        btn_HomeR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });
        btn_RegisterR.setOnClickListener(onClickListener);
    }

    private void findview(){
        name=findViewById(R.id.name);
        account=findViewById(R.id.account);
        password=findViewById(R.id.password);
        btn_RegisterR=findViewById(R.id.btn_RegisterR);
        btn_HomeR=findViewById(R.id.btn_HomeR);
    }

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                        //如果未連線的話，mNetworkInfo會等於null
                        if(mNetworkInfo != null){
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            firebaseAccount=account.getText().toString();
                            firebaseName=name.getText().toString();
                            //欄位不可為空
                            if(name.getText().toString().equals("")||account.getText().toString().equals("")||password.getText().toString().equals("")){
                                Toast.makeText(RegisterActivity.this, "欄位不可為空", Toast.LENGTH_SHORT).show();
                            }else {
                                //限定名字要輸入英文名或中文名
                                if (name.getText().toString().matches("[A-Z][a-z]+") || name.getText().toString().matches("[\\u4e00-\\u9fa5]+")) {
                                    //檢查是否帳號已存在
                                    final DatabaseReference myRef= database.getReference().child("account").child(firebaseAccount);
                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                Toast.makeText(RegisterActivity.this, "帳號已存在，請洽資訊人員處理", Toast.LENGTH_SHORT).show();
                                            }else{
                                                //密碼強度判定
                                                if (password.getText().toString().matches("^.*(?=.{8,12})(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$")) {
                                                    myRef.child("name").setValue(firebaseName);
                                                    myRef.child("password").setValue(password.getText().toString());
                                                    myRef.child("grade").setValue("0");
                                                    Toast.makeText(RegisterActivity.this,"註冊成功",Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "請參考密碼規格建立密碼", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(RegisterActivity.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(RegisterActivity.this, "請輸入英文名或中文名，英文首字需大寫，其餘小寫", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            Toast.makeText(RegisterActivity.this,"請開啟網路",Toast.LENGTH_SHORT).show();
                        }
        }
    };
}
