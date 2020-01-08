package com.example.companycheckinbyfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    FirebaseDatabase database;
    DatabaseReference myRef;
    private Button btncheckin,enterPost,btnLogout,btnUser,btnCheckInData;
    //公告部分的變數設定
    private ListView listPostData;
    private Button btnPrePage,btnNextPage,btnJump;
    private EditText jumpPage;
    private TextView title2,title3,totalPage;
    private String grade,account;
    private int page = 1,totalPageData;
    private ProgressBar progressBar;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab=getSupportActionBar();
        ab.hide();
        findview();
//        View view = LayoutInflater.from(this).inflate(R.layout.post_style,listPostData,false);
//        view.setMinimumHeight(300);
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            noLoginInitial();
        }else{
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            //如果未連線的話，mNetworkInfo會等於null
            if(mNetworkInfo != null){
                database = FirebaseDatabase.getInstance();
                myRef=database.getReference();
                user = SharedPrefManager.getInstance(this).getUser();
                account=user.getAccount();
                btnUser.setText(user.getUsername());
                enterPost.setOnClickListener(this);
                btncheckin.setOnClickListener(this);
                btnLogout.setOnClickListener(this);
                btnCheckInData.setOnClickListener(this);
                btnPrePage.setOnClickListener(this);
                btnNextPage.setOnClickListener(this);
                btnJump.setOnClickListener(this);
                loadData(page);
                listPostData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        //Log.d("111111111111","parent = " + parent.getItemAtPosition(position).toString());
                        String source=parent.getItemAtPosition(position).toString();

                        Intent intent = new Intent(MainActivity.this,PostActivity.class);
                        intent.putExtra("source",source);

                        MainActivity.this.startActivity(intent);

                    }
                });
            }else{
                Toast.makeText(MainActivity.this,"請開啟網路",Toast.LENGTH_SHORT).show();
            }
        }

    }

    //TODO findview()
    private void findview(){
        btnUser=findViewById(R.id.btnUser);
        btncheckin=findViewById(R.id.btn_checkin);
        enterPost=findViewById(R.id.bt_enterPostA);
        btnLogout =findViewById(R.id.btnLogout);
        btnCheckInData=findViewById(R.id.btn_checkInData);
        //公佈欄
        title2=findViewById(R.id.tv_title2);
        title3=findViewById(R.id.tv_title3);
        listPostData=findViewById(R.id.list_postData);
        btnPrePage=findViewById(R.id.bt_PrePage);
        btnNextPage=findViewById(R.id.bt_NextPage);
        btnJump=findViewById(R.id.bt_jump);
        jumpPage=findViewById(R.id.et_JumpPage);
        totalPage=findViewById(R.id.tv_totalPage);
        progressBar=findViewById(R.id.progressBar);
    }

    private void hidden(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title2.setVisibility(View.INVISIBLE);
                title3.setVisibility(View.INVISIBLE);
                listPostData.setVisibility(View.INVISIBLE);
                btnPrePage.setVisibility(View.INVISIBLE);
                btnNextPage.setVisibility(View.INVISIBLE);
                btnJump.setVisibility(View.INVISIBLE);
                jumpPage.setVisibility(View.INVISIBLE);
                totalPage.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void noLoginInitial(){
        hidden();
        btnUser.setText("訪客");
        btnLogout.setText("登入");
        enterPost.setOnClickListener(this);
        btncheckin.setOnClickListener(this);
        btnCheckInData.setOnClickListener(this);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    //TODO openOptionsDialog()
    private void openOptionsDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.loginoutCheck)
                .setPositiveButton("確定離開",
                        new DialogInterface.OnClickListener(){
                            public void onClick(
                                    DialogInterface dialoginterface, int i){
//                                finish();
                                SharedPrefManager.getInstance(MainActivity.this).logout();
                                noLoginInitial();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    //TODO onClick(View v)
    @Override
    public void onClick(View v) {
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            Toast.makeText(MainActivity.this,"請先登入",Toast.LENGTH_SHORT).show();
        }else{
            if(v.getId()==R.id.btnLogout){
                openOptionsDialog();
            }else if(v.getId()==R.id.bt_enterPostA){
                if(grade.equals("1")){
                    startActivity(new Intent(MainActivity.this,EnterPost.class));
//                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"權限不足，請洽資訊人員",Toast.LENGTH_SHORT).show();
                }
            }else if(v.getId()==R.id.btn_checkin){
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }else if(v.getId()==R.id.btn_checkInData){
                startActivity(new Intent(MainActivity.this,CheckInDataActivity.class));
//                finish();
            }else if(v.getId()==R.id.bt_PrePage){
                if(page==1){
                    Toast.makeText(MainActivity.this,"已經在第一頁了!!",Toast.LENGTH_LONG).show();
                }else{
                    --page;
//                Log.d("11111111111","page -= "+page);
                    loadData(page);
                }
            }else if(v.getId()==R.id.bt_NextPage){
                if(page== totalPageData){
                    Toast.makeText(MainActivity.this,"已經在最後一頁了!!",Toast.LENGTH_LONG).show();
                }else{
                    ++page;
//                Log.d("11111111111","page += "+page);
                    loadData(page);
                }
            }else if(v.getId()==R.id.bt_jump){
                if(Integer.parseInt(jumpPage.getText().toString())>totalPageData || Integer.parseInt(jumpPage.getText().toString())<1){
                    Toast.makeText(MainActivity.this,"頁數錯誤",Toast.LENGTH_LONG).show();
                }else{
                    page=Integer.parseInt(jumpPage.getText().toString());
//                Log.d("11111111111","jumppage = "+page);
                    loadData(page);
                }
            }
        }
    }

    //TODO loadData(final int page)
    private void loadData(final int page){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                grade=dataSnapshot.child("account").child(account).child("grade").getValue().toString();
//                Log.d("11111111",grade);
                ArrayList<HashMap<String, String>> arrayList=new ArrayList();
//                HashMap<String, String> hashMap=(HashMap)dataSnapshot.getValue();
//                Log.e("bbbb2",hashMap+"");
//                arrayList.add(hashMap);
                HashMap<String, String> hashMap = null;
                for(int i=0;i<5;i++) {
                    hashMap = (HashMap) dataSnapshot.child("post"+((int)dataSnapshot.getChildrenCount()-1-i-(page-1)*5)).getValue();
                    arrayList.add(hashMap);
//                    Log.d("1111111","post"+i);
                }
                SimpleAdapter adapter = new SimpleAdapter(
                                    MainActivity.this,
                                    arrayList,
                                    R.layout.post_style,
                                    new String[]{"title", "time"},
                                    new int[]{R.id.postTitle, R.id.postDate}
                            );
                listPostData.setAdapter(adapter);
                totalPageData=(int)(dataSnapshot.getChildrenCount()-1)/5+1;

                totalPage.setText("/"+totalPageData);
                jumpPage.setText(String.valueOf(page));
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
