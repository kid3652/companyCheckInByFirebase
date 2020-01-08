package com.example.companycheckinbyfirebase;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private LocationManager mLocationManager;
    private int REQUEST_EXTERNEL_PERMISSION = 789;
    Location last;
    LatLng latLng;
    private GoogleMap mMap;
    private Marker markerMe;
    //You should put your Google Geocoding API KEY AT Here
    String Geocoding_API_KEY="Your API KEY";
    String provider,longitude, latitude, date_Time;
    SupportMapFragment mapFragment;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

    }

    @Override
    protected void onStart() {
        super.onStart();
                /*
                    //優化與位置提供來源的關係
                    criteria.setAccuracy(Criteria.ACCURACY_FINE); //定位精度: 最高
                    criteria.setBearingRequired(false); //方位資訊: 不需要
                    criteria.setCostAllowed(true);  //是否允許付費
                    String provider = mLocationManager.getBestProvider(criteria, true); //獲取GPS資訊
                    */
        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false); //海拔資訊：不需要
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //定位精度: 最高
        criteria.setPowerRequirement(Criteria.POWER_LOW); //耗電量: 低功耗
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapFragment.getMapAsync(MapsActivity.this);
        //如果是使用LocationManager.GPS_PROVIDER，則在室內手機無法啟動GPS功能時，會導致無法跑onLocationChanged
        provider=mLocationManager.getBestProvider(criteria,true);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MapsActivity.this, "請開啟定位功能後重新執行", Toast.LENGTH_SHORT).show();
            Intent enableGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(enableGPS);
            finish();
        } else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //第一次執行此程式，尚未確認權限
                String[] ppp = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                this.requestPermissions(ppp, REQUEST_EXTERNEL_PERMISSION);
                return;
            } else {
                //第二次進入後不在詢問權限，直接執行以下程式
                mLocationManager.requestLocationUpdates(provider /*provider*/, 500, 0, MapsActivity.this);
                last = mLocationManager.getLastKnownLocation(provider);

                if (last == null) {
                    Log.d("FirstTime", "首次打卡");
                    latLng = new LatLng(25.038630, 121.564816);
                    mLocationManager.removeUpdates(MapsActivity.this);
                    mLocationManager.requestLocationUpdates(provider /*provider*/, 500, 0, MapsActivity.this);
                } else {
                    mLocationManager.removeUpdates(MapsActivity.this);
                    mLocationManager.requestLocationUpdates(provider /*provider*/, 10000, 1000, MapsActivity.this);
//                    Log.d("1111111", "3" + last);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(MapsActivity.this);
    }

    //TODO onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNEL_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MapsActivity.this, "必須同意使用定位功能,才可以打卡", Toast.LENGTH_LONG).show();
                finish();
            } else {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER /*provider*/, 500, 0, MapsActivity.this);
                    last = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    mLocationManager.removeUpdates(this);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER /*provider*/, 10000, 1000, MapsActivity.this);
                    mapFragment.getMapAsync(MapsActivity.this);
                }

            }

        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //TODO OnMapReady
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //假設markMe沒跟著位置跑，有可是因為已存在markMe的關係，可以試著加入以下程式
//        if (markerMe != null) {
//            markerMe.remove();
//        }

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //google API內建的UiSettings功能
            UiSettings mapUiSettings = mMap.getUiSettings();
            //縮放控制Bttton
            mapUiSettings.setZoomControlsEnabled(true);
            //手指縮放功能
            mapUiSettings.setZoomGesturesEnabled(true);

            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //按鈕後獲取現在位置的經緯度
                mLocationManager.removeUpdates(MapsActivity.this);
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return false;
                }
                mLocationManager.requestLocationUpdates(provider /*provider*/, 500, 0, MapsActivity.this);
                CameraPosition camPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(14)
                        .build();
                Log.d("11111","here");
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
                mLocationManager.removeUpdates(MapsActivity.this);
                mLocationManager.requestLocationUpdates(provider /*provider*/, 900000, 1000, MapsActivity.this);
                return false;
            }
        });
    }

    //TODO onLocationChanged
    @Override
    public void onLocationChanged(Location location) {
        Log.d("11111","here2");
        latLng=new LatLng(location.getLatitude(),location.getLongitude());
        //地圖設定(Marker和Camera)
        if (markerMe != null) {
            markerMe.remove();
        }

        MarkerOptions markerOptions=new MarkerOptions();
        if(latLng==null){
            latLng = new LatLng(25.038630, 121.564816);
        }

        markerOptions.position(latLng).title("請按我打卡");

        markerMe=mMap.addMarker(markerOptions);

        CameraPosition camPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));

        try {
            Thread.sleep(1000);
            markerMe.showInfoWindow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //要傳入資料庫的變數設定
        final User user = SharedPrefManager.getInstance(MapsActivity.this).getUser();
        longitude = Double.toString(latLng.longitude);
        latitude = Double.toString(latLng.latitude);
        Log.d("經緯度1","經度 = " + longitude + "  緯度 = " + latitude);
        try {
            String geoencode_url="https://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longitude+"&language=zh-TW&key="+Geocoding_API_KEY;
            byte[] data = MapsActivity.this.down_data(geoencode_url,null,null);
            String json_string = new String(data);
            JSONObject json_object = null;
            json_object = new JSONObject(json_string);
            String status = (String)json_object.get("status");
            Log.d("status",status);
            if(status.equals("OK")) {
                JSONArray arr = (JSONArray) json_object.get("results");
                JSONObject arr1 = (JSONObject) arr.get(0);
                address = (String)arr1.get("formatted_address");
            }else{
                Toast.makeText(MapsActivity.this,"Geocoding Fail請聯繫資訊人員",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        date_Time = sDateFormat.format(new java.util.Date());

        //設定監聽
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(marker.getTitle())
                        .setMessage("要在這個位置打卡?\n時間:" + date_Time +"\n地點:"+address)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String account=SharedPrefManager.getInstance(MapsActivity.this).getUser().getAccount();
                                FirebaseDatabase database=FirebaseDatabase.getInstance();
                                final DatabaseReference myRef=database.getReference("account/"+account);
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        myRef.child("checkIn"+dataSnapshot.getChildrenCount()).child("time").setValue(date_Time);
                                        myRef.child("checkIn"+dataSnapshot.getChildrenCount()).child("address").setValue(address);
                                        Toast.makeText(MapsActivity.this, "打卡時間"+date_Time+"\n"+"打卡地點"+address, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(MapsActivity.this,"資料庫連線失敗",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                startActivity(new Intent(MapsActivity.this, MainActivity.class));
                                MapsActivity.this.finish();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return false;
            }
        });

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private byte[] down_data(final String resource, final ProgressBar busy /* you have to pass in a Progress, at least null*/, final ProgressBar progress /* you have to pass in a Progress, at least null*/){

        final byte[][] result = new byte[1][1];
        if(busy != null){
            busy.setVisibility(View.VISIBLE);
        }
        Thread t;
        t = new Thread(){

            @Override
            public void run() {
                super.run();
                try {

                    URL url;
                    url = new URL(resource);
                    HttpURLConnection connect;
                    connect = (HttpURLConnection) url.openConnection();
                    connect.setRequestProperty("Accept-Encoding", "identity");
                    connect.connect();
                    int total_amount = connect.getContentLength();

                    InputStream is = connect.getInputStream();


                    byte[] singl_time = new byte[1024 * 4];
                    ArrayList<Byte> toatl = new ArrayList<Byte>();

                    int how_many_byte_this_time;
                    how_many_byte_this_time = is.read(singl_time);
                    int accumulation = 0;

                    while(how_many_byte_this_time >= 0){

                        for(int i=0;i<how_many_byte_this_time;i++){
                            toatl.add(singl_time[i]);
                        }


                        if(progress != null) {
                            accumulation = accumulation + how_many_byte_this_time;

                            final double percent = (double)accumulation / total_amount;
                            Log.i("PERCENT", "" + percent + "分母total_amount=" + total_amount + "分子accumulation=" + accumulation);
                            MapsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    progress.setProgress((int) (100 * percent));
                                }

                            });
                        }
                        how_many_byte_this_time = is.read(singl_time);
                    }

                    //final Bitmap image;
                    byte[] array_i_need = new byte[toatl.size()];
                    for(int i=0;i<toatl.size();i++){
                        array_i_need[i] = toatl.get(i);
                    }
                    //image = BitmapFactory.decodeByteArray(array_i_need, 0, array_i_need.length);
                    if(busy != null){
                        MapsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //head.setImageBitmap(image);
                                busy.setVisibility(View.INVISIBLE);
                            }
                        });
                    }

                    result[0] = array_i_need;

                }catch(Exception e){
                    Log.e("ERROR", e.toString());
                }

            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  result[0];
    }

    //轉UTF-8
//    public static String toUtf8(String str) {
//        String result = null;
//        try {
//            result = new String(str.getBytes("UTF-8"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return result;
//    }

}