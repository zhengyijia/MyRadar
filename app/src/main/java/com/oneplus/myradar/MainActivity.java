package com.oneplus.myradar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;
import com.oneplus.util.AESEncryptor;
import com.oneplus.widget.MyTextView2;
import com.oneplus.widget.MyTextView3;
import com.oneplus.widget.StrokeTextView3;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;

    private boolean isFirstLocation = true;

    private BaiduMap baiduMap = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private ImageView RefreshButton = null;

    private List<ContactsItem> FriendList = null;
    private List<ContactsItem> EnemyList = null;

    private MyApplication application = null;

    private Marker myLocMarker = null;

    private View markerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        ImageView scanLine = (ImageView) findViewById(R.id.scan_line);
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
        scanLine.startAnimation(animation);

        application = (MyApplication) this.getApplicationContext();
        FriendList = application.getFriendsList();
        EnemyList = application.getEnemiesList();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);//隐藏缩放控件
        mMapView.showScaleControl(false);//隐藏比例尺控件
        baiduMap = mMapView.getMap();

        application.setMap(baiduMap);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        Double lat = Double.parseDouble(pref.getString("Latitude","22.258974"));
        Double lon = Double.parseDouble(pref.getString("Longitude","113.542137"));
        application.setLat(lat);
        application.setLon(lon);
        setUserMapCenter();
        setMarker();

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();

        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setOverlookingGesturesEnabled(false);
        //uiSettings.setScrollGesturesEnabled(false);

        MyTextView2 friendButton = (MyTextView2) findViewById(R.id.friend_button);
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FriendPageActivity.class);
                startActivity(intent);
            }
        });

        MyTextView2 enemyButton = (MyTextView2) findViewById(R.id.enemy_button);
        enemyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EnemyPageActivity.class);
                startActivity(intent);
            }
        });

        //定义多边形的四个顶点
        LatLng pt1 = new LatLng(90, -180);
        LatLng pt2 = new LatLng(90, 180);
        LatLng pt3 = new LatLng(-90, 180);
        LatLng pt4 = new LatLng(-90, -180);
        List<LatLng> pts = new ArrayList<LatLng>();
        pts.add(pt1);
        pts.add(pt2);
        pts.add(pt3);
        pts.add(pt4);
        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(pts)
                .stroke(new Stroke(5, 0x0000FF00))
                .fillColor(0xaa049909);
        //在地图上添加多边形Option，用于显示
        baiduMap.addOverlay(polygonOption);

        ImageView locateButton = (ImageView) findViewById(R.id.locate);
        ImageView helpButton = (ImageView) findViewById(R.id.help);
        RefreshButton = (ImageView)findViewById(R.id.refresh);

        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserMapCenter();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        RefreshButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                RefreshButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.refresh_button_on));
                ImageView scanLine = (ImageView) findViewById(R.id.scan_line);
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
                scanLine.startAnimation(animation);
                MyTimer timer = new MyTimer();
                timer.start();
                String smsText = getString(R.string.sms_text);
                try {
                    smsText = AESEncryptor.encrypt(smsText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SmsManager manager = SmsManager.getDefault();
                ArrayList<String> list = manager.divideMessage(smsText);  //因为一条短信有字数限制，因此要将长短信拆分
                String phoneNumber = null;
                for(ContactsItem i : FriendList) {
                    phoneNumber = i.getPhoneNumber();
                    for (String text : list) {
                        manager.sendTextMessage(phoneNumber, null, text, null, null);
                    }
                }
                for(ContactsItem i: EnemyList){
                    phoneNumber = i.getPhoneNumber();
                    for(String text : list){
                        manager.sendTextMessage(phoneNumber, null, text, null, null);
                    }
                }
            }
        });

        //setFriendMarker();
        //setEnemyMarker();

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String phoneNumber = null;
                if(null != (phoneNumber = marker.getExtraInfo().getString("phoneNumber"))){
                    if(marker.getExtraInfo().getBoolean("ForE")){
                        Intent intent = new Intent(MainActivity.this, FriendDetailActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(MainActivity.this, EnemyDetailActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });

    }

    private void setFriendMarker(){
        LatLng point = null;
        OverlayOptions option = null;
        LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //构建Marker图标
        BitmapDescriptor bitmap = null;
        Bitmap viewBitmap = null;
        Marker marker = null;
        Polyline line1 = null;
        Polyline line2 = null;
        Marker distanceMarker = null;
        Bundle extraMsg = null;
        List<LatLng> points = new ArrayList<LatLng>();
        LatLng myLocPoint = new LatLng(application.getLat(),application.getLon());
        points.add(myLocPoint);
        MarkerItem friendMarkerItem = null;
        double distance = 0;
        double centerLat = 0;
        double centerLon = 0;
        LatLng centerPoint = null;
        for(ContactsItem i : FriendList){
            if(i.getGetLoc()) {
                markerView = inflater.inflate(R.layout.map_friend_marker, null);
                ((StrokeTextView3) markerView.findViewById(R.id.friend_marker_name)).setText(i.getName());
                ((MyTextView3) markerView.findViewById(R.id.friend_marker_number)).setText(i.getPhoneNumber());
                viewBitmap = getViewBitmap(markerView);
                bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);
                point = new LatLng(i.getLatitude(), i.getLongitude());
                extraMsg = new Bundle();
                extraMsg.putString("phoneNumber", i.getPhoneNumber());
                extraMsg.putBoolean("ForE", true);
                option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .draggable(false)  //设置手势拖拽;
                        .animateType(MarkerOptions.MarkerAnimateType.grow)  //生长动画
                        .extraInfo(extraMsg);
                //在地图上添加Marker，并显示
                marker = (Marker) baiduMap.addOverlay(option);
                points.add(point);
                //构造对象
                OverlayOptions ooPolyline = new PolylineOptions()
                        .width(8)
                        .color(ContextCompat.getColor(getBaseContext(),R.color.white))
                        .points(points);
                //添加到地图
                line1 = (Polyline)baiduMap.addOverlay(ooPolyline);
                OverlayOptions ooPolyline2 = new PolylineOptions()
                        .width(6)
                        .color(ContextCompat.getColor(getBaseContext(),R.color.buttonGreen))
                        .points(points);
                //添加到地图
                line2 = (Polyline) baiduMap.addOverlay(ooPolyline2);

                distance = DistanceUtil.getDistance(myLocPoint,point)/1000;
                BigDecimal shortDistance = new BigDecimal(distance).setScale(2,BigDecimal.ROUND_HALF_UP);
                centerLat = (application.getLat() + i.getLatitude())/2;
                centerLon = (application.getLon() + i.getLongitude())/2;
                centerPoint = new LatLng(centerLat, centerLon);
                markerView = inflater.inflate(R.layout.map_friend_distance_marker, null);
                ((StrokeTextView3) markerView.findViewById(R.id.friend_marker_distance)).setText(shortDistance.toString()+"km");
                viewBitmap = getViewBitmap(markerView);
                bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);
                extraMsg = new Bundle();
                extraMsg.putString("phoneNumber", null);
                option = new MarkerOptions()
                        .position(centerPoint)
                        .icon(bitmap)
                        .draggable(false)  //设置手势拖拽;
                        .extraInfo(extraMsg)
                        .anchor(0.5f,0.5f);
                //在地图上添加Marker，并显示
                distanceMarker = (Marker) baiduMap.addOverlay(option);

                friendMarkerItem = new MarkerItem();
                friendMarkerItem.setMarker(marker);
                friendMarkerItem.setLine1(line1);
                friendMarkerItem.setLine2(line2);
                friendMarkerItem.setDistanceMarker(distanceMarker);
                application.addFriendMarker(i.getPhoneNumber(), friendMarkerItem);
                points.remove(point);
            }
        }
    }

    public void setEnemyMarker(){
        LatLng point = null;
        OverlayOptions option = null;
        LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //构建Marker图标
        BitmapDescriptor bitmap = null;
        Bitmap viewBitmap = null;
        Marker marker = null;
        Polyline line1 = null;
        Polyline line2 = null;
        Marker distanceMarker = null;
        Bundle extraMsg = null;
        List<LatLng> points = new ArrayList<LatLng>();
        LatLng myLocPoint = new LatLng(application.getLat(),application.getLon());
        points.add(myLocPoint);
        MarkerItem enemyMarkerItem = null;
        double distance = 0;
        double centerLat = 0;
        double centerLon = 0;
        LatLng centerPoint = null;
        for(ContactsItem i : EnemyList){
            if(i.getGetLoc()) {
                markerView = inflater.inflate(R.layout.map_enemy_marker, null);
                ((StrokeTextView3) markerView.findViewById(R.id.enemy_marker_name)).setText(i.getName());
                ((MyTextView3) markerView.findViewById(R.id.enemy_marker_number)).setText(i.getPhoneNumber());
                viewBitmap = getViewBitmap(markerView);
                bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);
                point = new LatLng(i.getLatitude(), i.getLongitude());
                extraMsg = new Bundle();
                extraMsg.putString("phoneNumber", i.getPhoneNumber());
                extraMsg.putBoolean("ForE", false);
                option = new MarkerOptions()
                        .position(point)
                        .icon(bitmap)
                        .draggable(false)  //设置手势拖拽;
                        .animateType(MarkerOptions.MarkerAnimateType.grow)  //生长动画
                        .extraInfo(extraMsg);
                //在地图上添加Marker，并显示
                marker = (Marker) baiduMap.addOverlay(option);
                points.add(point);
                //构造对象
                OverlayOptions ooPolyline = new PolylineOptions()
                        .width(8)
                        .color(ContextCompat.getColor(getBaseContext(),R.color.white))
                        .points(points);
                //添加到地图
                line1 = (Polyline)baiduMap.addOverlay(ooPolyline);
                OverlayOptions ooPolyline2 = new PolylineOptions()
                        .width(6)
                        .color(ContextCompat.getColor(getBaseContext(),R.color.buttonRed))
                        .points(points);
                //添加到地图
                line2 = (Polyline) baiduMap.addOverlay(ooPolyline2);

                distance = DistanceUtil.getDistance(myLocPoint,point)/1000;
                BigDecimal shortDistance = new BigDecimal(distance).setScale(2,BigDecimal.ROUND_HALF_UP);
                centerLat = (application.getLat() + i.getLatitude())/2;
                centerLon = (application.getLon() + i.getLongitude())/2;
                centerPoint = new LatLng(centerLat, centerLon);
                markerView = inflater.inflate(R.layout.map_enemy_distance_marker, null);
                ((StrokeTextView3) markerView.findViewById(R.id.enemy_marker_distance)).setText(shortDistance.toString()+"km");
                viewBitmap = getViewBitmap(markerView);
                bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);
                extraMsg = new Bundle();
                extraMsg.putString("phoneNumber", null);
                option = new MarkerOptions()
                        .position(centerPoint)
                        .icon(bitmap)
                        .draggable(false)  //设置手势拖拽;
                        .extraInfo(extraMsg)
                        .anchor(0.5f,0.5f);
                //在地图上添加Marker，并显示
                distanceMarker = (Marker) baiduMap.addOverlay(option);

                enemyMarkerItem = new MarkerItem();
                enemyMarkerItem.setMarker(marker);
                enemyMarkerItem.setLine1(line1);
                enemyMarkerItem.setLine2(line2);
                enemyMarkerItem.setDistanceMarker(distanceMarker);
                application.addEnemyMarker(i.getPhoneNumber(), enemyMarkerItem);
                points.remove(point);
            }
        }
    }

    /**
     * 添加marker
     */
    private void setMarker() {
        Log.v("pcw","setMarker : lat : "+ application.getLat()+" lon : " + application.getLon());
        //定义Marker坐标点
        LatLng point = new LatLng(application.getLat(), application.getLon());
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker);
        Bundle extraMsg = new Bundle();
        extraMsg.putString("phoneNumber",null);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .anchor(0.5f,0.5f)
                .extraInfo(extraMsg);
        //在地图上添加Marker，并显示
        myLocMarker = (Marker)baiduMap.addOverlay(option);
    }

    /**
     * 设置中心点
     */
    private void setUserMapCenter() {
        Log.v("pcw","setUserMapCenter : lat : "+ application.getLat()+" lon : " + application.getLon());
        LatLng cenpt = new LatLng(application.getLat(),application.getLon());
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(cenpt)
                .zoom(17)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        baiduMap.setMapStatus(mMapStatusUpdate);

    }

    /**
     * 配置定位参数
     */
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现定位监听 位置一旦有所改变就会调用这个方法
     * 可以在这个方法里面获取到定位之后获取到的一系列数据
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            boolean success = false;
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());

            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
                success = true;

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
                success = true;
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
                success = true;
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            /*if(success) {
                application.setLat(location.getLatitude());
                application.setLon(location.getLongitude());
            }*/

            //这个判断是为了防止每次定位都重新设置中心点和marker
            if(isFirstLocation){
                if(success) {
                    isFirstLocation = false;
                    //setMarker();
                    application.setLat(location.getLatitude());
                    application.setLon(location.getLongitude());
                    LatLng point = new LatLng(application.getLat(), application.getLon());
                    myLocMarker.setPosition(point);
                    myLocMarker.setAnchor(0.5f,0.5f);
                    setUserMapCenter();
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("Latitude", Double.toString(application.getLat()));
                    editor.putString("Longitude", Double.toString(application.getLon()));
                    editor.apply();
                    setFriendMarker();
                    setEnemyMarker();
                }
            }
            Log.v("pcw","lat : " + application.getLat()+" lon : " + application.getLon());
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

    private Bitmap getViewBitmap(View addViewContent) {

        addViewContent.setDrawingCacheEnabled(true);

        addViewContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        addViewContent.layout(0, 0,
                addViewContent.getMeasuredWidth(),
                addViewContent.getMeasuredHeight());

        addViewContent.buildDrawingCache();
        Bitmap cacheBitmap = addViewContent.getDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        return bitmap;
    }

    Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    RefreshButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.refresh_button_selector));
                    break;
                default:
                    break;
            }
        }
    };

    public class MyTimer extends Thread {
        public MyTimer() {
            // TODO Auto-generated constructor stub
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(3000);
                mHandler.sendEmptyMessage(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("Latitude",Double.toString(application.getLat()));
        editor.putString("Longitude",Double.toString(application.getLon()));
        editor.apply();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
