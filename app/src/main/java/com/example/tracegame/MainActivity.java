package com.example.tracegame;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.tracegame.database.GameDao;
import com.example.tracegame.models.GameModel;
import com.example.tracegame.models.SmsInfo;
import com.example.tracegame.utils.Constants;
import com.example.tracegame.utils.SmsObserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/12/25.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    public static final int REQUEST_LOCATION_TYPE = 100;
    public static final int RETURN_LOCATION_TYPE = 101;

    private Button toFriend;
    private Button toEnemy;
    private Button locate;
    private Button refresh;
    private MapView mapView;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    private List<GameModel> friendModelList;
    private List<GameModel> enemyModelList;
    private List<GameModel> allPersonList = new ArrayList<>();
    private SmsObserver mObserver;
    private double latestLat = 0;
    private double latestLng = 0;
    private boolean isFirst = true;
    private BitmapDescriptor friendBitmap;
    private BitmapDescriptor enemyBitmap;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    SmsInfo info = (SmsInfo)msg.obj;
                    String content = info.smsBody;
                    if(content.contains("hello")){
                        Set<String> stringList = new HashSet<>();
                        stringList.add(info.smsAddress);
                        sendSms(stringList, RETURN_LOCATION_TYPE);
                    }else if(content.contains("/")){
                        GameModel gameModel = getPersonByNumber(info.smsAddress);
                        if(gameModel != null) {
                            List<Double> doubleList = produceMatchPattern(content);
                            if(doubleList != null && doubleList.size() > 0){
                                gameModel.setLatitude(doubleList.get(0));
                                gameModel.setLongitude(doubleList.get(1));
                                GameDao.getInstance(MainActivity.this).update(gameModel);
                                refreshMapOverlay();
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        checkUserPermission();
        toFriend = (Button)findViewById(R.id.main_to_friend);
        toFriend.setOnClickListener(this);
        toEnemy = (Button)findViewById(R.id.main_to_enemy);
        toEnemy.setOnClickListener(this);
        locate = (Button)findViewById(R.id.main_location);
        locate.setOnClickListener(this);
        refresh = (Button)findViewById(R.id.main_refresh);
        refresh.setOnClickListener(this);
        mapView = (MapView)findViewById(R.id.map_view);
        loadPerson();
        initMap();
        registerObserver();
    }

    private void loadPerson(){
        friendModelList = GameDao.getInstance(this).getPerson(Constants.FRIENDS);
        enemyModelList = GameDao.getInstance(this).getPerson(Constants.ENEMIES);
        allPersonList.clear();
        allPersonList.addAll(friendModelList);
        allPersonList.addAll(enemyModelList);
    }

    private void initMap(){
        friendBitmap =  BitmapDescriptorFactory.fromResource(R.mipmap.friend_marker);
        enemyBitmap =  BitmapDescriptorFactory.fromResource(R.mipmap.enemy_marker);
        baiduMap = mapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(new LocationReceiver());
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setScanSpan(5000);
        option.setOpenGps(true);
        locationClient.setLocOption(option);
        locationClient.start();
    }

    private void registerObserver(){
        ContentResolver resolver = getContentResolver();
        mObserver = new SmsObserver(resolver, handler);
        resolver.registerContentObserver(Uri.parse("content://sms"), true, mObserver);
    }

    private List<Double> produceMatchPattern(String content){
        List<Double> doubleList = new ArrayList<>();
        int j = 0;
        for(int i=0;i<content.length();i++){
            if(content.charAt(i) == '/'){
                String subString = content.substring(j,i);
                doubleList.add(Double.parseDouble(subString));
                j = i+1;
            }
        }
        doubleList.add(Double.parseDouble(content.substring(j)));
        return doubleList;
    }

    private GameModel getPersonByNumber(String number){

        for (GameModel gameModel : allPersonList){
            if(gameModel.getPhoneNum().equals(number))
                return gameModel;
        }
        return null;
    }

    private void sendSms(Set<String> list, int type){

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list){
            stringBuilder.append(string);
            stringBuilder.append(";");
        }

        if(type == REQUEST_LOCATION_TYPE){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("address", stringBuilder.toString());
            intent.putExtra("sms_body", "hello");
            intent.setType("vnd.android-dir/mms-sms");
            startActivity(intent);
        }else if(type == RETURN_LOCATION_TYPE){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("address", stringBuilder.toString());
            intent.putExtra("sms_body", latestLat+"/"+latestLng);
            intent.setType("vnd.android-dir/mms-sms");
            startActivity(intent);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_to_friend:
                Intent intent = new Intent(this, FriendActivity.class);
                startActivity(intent);
                break;

            case R.id.main_to_enemy:
                Intent intent1 = new Intent(this, EnemyActivity.class);
                startActivity(intent1);
                break;

            case R.id.main_location:
                if(latestLat >0 && latestLng > 0) {
                    LatLng loc = new LatLng(latestLat, latestLng);
                    MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(loc);
                    baiduMap.animateMapStatus(msu);
                }
                break;

            case R.id.main_refresh:
                loadPerson();
                Set<String> stringSet = new HashSet<>();
                for (GameModel gameModel : allPersonList){
                    stringSet.add(gameModel.getPhoneNum());
                }
                sendSms(stringSet, REQUEST_LOCATION_TYPE);
                break;
        }
    }

    class LocationReceiver implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation == null)
                return;
            latestLat = bdLocation.getLatitude();
            latestLng = bdLocation.getLongitude();

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .direction(100)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            baiduMap.setMyLocationData(locData);

            if (isFirst) {
                isFirst = false;
                LatLng loc = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(loc);
                baiduMap.animateMapStatus(msu);
            }
            refreshMapOverlay();
        }
    }

    private void refreshMapOverlay(){
        loadPerson();
        baiduMap.clear();
        for (GameModel gameModel : allPersonList){
            if(gameModel.getLatitude() > 0 || gameModel.getLongitude() > 0){
                LatLng point = new LatLng(gameModel.getLatitude(), gameModel.getLongitude());
                if(gameModel.getType() == Constants.FRIENDS){
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(friendBitmap);
                    baiduMap.addOverlay(option);

                    List<LatLng> points = new ArrayList<>();
                    points.add(new LatLng(latestLat,latestLng));
                    points.add(new LatLng(gameModel.getLatitude(), gameModel.getLongitude()));
                    List<Integer> colors = new ArrayList<>();
                    colors.add(Color.GREEN);
                    OverlayOptions ooPolyline = new PolylineOptions().width(5)
                            .colorsValues(colors).points(points);
                    baiduMap.addOverlay(ooPolyline);
                }else if(gameModel.getType() == Constants.ENEMIES){
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(enemyBitmap);
                    baiduMap.addOverlay(option);

                    List<LatLng> points = new ArrayList<>();
                    points.add(new LatLng(latestLat,latestLng));
                    points.add(new LatLng(gameModel.getLatitude(), gameModel.getLongitude()));
                    List<Integer> colors = new ArrayList<>();
                    colors.add(Color.RED);
                    OverlayOptions ooPolyline = new PolylineOptions().width(5)
                            .colorsValues(colors).points(points);
                    baiduMap.addOverlay(ooPolyline);
                }
            }
        }
    }

    private void checkUserPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ,Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_SMS},
                    99);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 99){
//            if(grantResults[0] != PackageManager.PERMISSION_GRANTED ||
//                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
//                    grantResults[2] != PackageManager.PERMISSION_GRANTED){
//                finish();
//            }
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.stop();
        this.getContentResolver().unregisterContentObserver(mObserver);
    }

}
