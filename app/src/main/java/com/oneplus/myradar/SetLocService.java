package com.oneplus.myradar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;
import com.oneplus.widget.MyTextView3;
import com.oneplus.widget.StrokeTextView3;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by Oneplus on 2016/10/16.
 */
public class SetLocService extends Service{

    private String phoneNumber = null;
    private String body = null;
    private Date date = null;

    private double lat;
    private double lon;

    private List<ContactsItem> FriendsList = null;
    private List<ContactsItem> EnemiesList = null;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        phoneNumber = intent.getStringExtra("phoneNumber");
        body = intent.getStringExtra("body");
        date = (Date)intent.getSerializableExtra("date");
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean find = false;
                MyApplication application = (MyApplication)getApplicationContext();
                FriendsList = application.getFriendsList();
                EnemiesList = application.getEnemiesList();
                StringTokenizer st = new StringTokenizer(body, "/") ;
                lat = Double.parseDouble(st.nextToken());
                lon = Double.parseDouble(st.nextToken());
                BaiduMap baiduMap = application.getMap();
                for(ContactsItem i : FriendsList){
                    if(i.getPhoneNumber().equals(phoneNumber)||("+86"+i.getPhoneNumber()).equals(phoneNumber)){
                        i.setLatitude(lat);
                        i.setLongitude(lon);
                        i.setDate(date);
                        i.setGetLoc(true);
                        application.saveFriendsList();
                        Map<String,MarkerItem> friendMarker = application.getFriendMap();
                        MarkerItem friendMarkerItem = null;
                        Marker marker = null;
                        Polyline line1 = null;
                        Polyline line2 =null;
                        Marker distanceMarker = null;
                        List<LatLng> points = new ArrayList<LatLng>();
                        LatLng myLocPoint = new LatLng(application.getLat(),application.getLon());
                        points.add(myLocPoint);
                        LatLng point = new LatLng(lat, lon);
                        points.add(point);
                        Double distance = DistanceUtil.getDistance(myLocPoint,point)/1000;
                        BigDecimal shortDistance = new BigDecimal(distance).setScale(2,BigDecimal.ROUND_HALF_UP);
                        Double centerLat = (application.getLat() + i.getLatitude())/2;
                        Double centerLon = (application.getLon() + i.getLongitude())/2;
                        LatLng centerPoint = new LatLng(centerLat, centerLon);
                        LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View distanceMarkerView = inflater.inflate(R.layout.map_friend_distance_marker, null);
                        ((StrokeTextView3) distanceMarkerView.findViewById(R.id.friend_marker_distance)).setText(shortDistance.toString()+"km");
                        Bitmap distanceViewBitmap = getViewBitmap(distanceMarkerView);
                        BitmapDescriptor distanceBitmap = BitmapDescriptorFactory.fromBitmap(distanceViewBitmap);
                        if(null == (friendMarkerItem = friendMarker.get(phoneNumber)))
                            friendMarkerItem = friendMarker.get(phoneNumber.substring(3));
                        if(null == friendMarkerItem){
                            View markerView = inflater.inflate(R.layout.map_friend_marker, null);
                            ((StrokeTextView3) markerView.findViewById(R.id.friend_marker_name)).setText(i.getName());
                            ((MyTextView3) markerView.findViewById(R.id.friend_marker_number)).setText(i.getPhoneNumber());
                            //构建Marker图标
                            Bitmap viewBitmap = getViewBitmap(markerView);
                            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);;
                            Bundle extraMsg = new Bundle();
                            extraMsg.putString("phoneNumber",i.getPhoneNumber());
                            extraMsg.putBoolean("ForE",true);
                            OverlayOptions option = new MarkerOptions()
                                    .position(point)
                                    .icon(bitmap)
                                    .draggable(false)  //设置手势拖拽;
                                    .extraInfo(extraMsg);
                            //在地图上添加Marker，并显示
                            marker = (Marker)baiduMap.addOverlay(option);
                            //构造对象
                            OverlayOptions ooPolyline = new PolylineOptions()
                                    .width(8)
                                    .color(ContextCompat.getColor(getBaseContext(),R.color.white))
                                    .points(points);
                            //添加到地图
                            line1 = (Polyline) baiduMap.addOverlay(ooPolyline);
                            OverlayOptions ooPolyline2 = new PolylineOptions()
                                    .width(6)
                                    .color(ContextCompat.getColor(getBaseContext(),R.color.buttonGreen))
                                    .points(points);
                            //添加到地图
                            line2 = (Polyline) baiduMap.addOverlay(ooPolyline2);

                            option = new MarkerOptions()
                                    .position(centerPoint)
                                    .icon(distanceBitmap)
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
                        }else{
                            marker = friendMarkerItem.getMarker();
                            marker.setPosition(point);
                            line1 = friendMarkerItem.getLine1();
                            line1.setPoints(points);
                            line2 = friendMarkerItem.getLine2();
                            line2.setPoints(points);
                            distanceMarker = friendMarkerItem.getDistanceMarker();
                            distanceMarker.setPosition(centerPoint);
                            distanceMarker.setIcon(distanceBitmap);
                        }
                        find = true;
                        break;
                    }
                }
                if(!find){
                    for(ContactsItem i : EnemiesList){
                        if(i.getPhoneNumber().equals(phoneNumber)||("+86"+i.getPhoneNumber()).equals(phoneNumber)){
                            i.setLatitude(lat);
                            i.setLongitude(lon);
                            i.setDate(date);
                            i.setGetLoc(true);
                            application.saveEnemiesList();
                            Map<String,MarkerItem> enemyMarker = application.getEnemyMap();
                            MarkerItem enemyMarkerItem = null;
                            Marker marker = null;
                            Polyline line1 = null;
                            Polyline line2 =null;
                            Marker distanceMarker = null;
                            List<LatLng> points = new ArrayList<LatLng>();
                            LatLng myLocPoint = new LatLng(application.getLat(),application.getLon());
                            points.add(myLocPoint);
                            LatLng point = new LatLng(lat, lon);
                            points.add(point);
                            Double distance = DistanceUtil.getDistance(myLocPoint,point)/1000;
                            BigDecimal shortDistance = new BigDecimal(distance).setScale(2,BigDecimal.ROUND_HALF_UP);
                            Double centerLat = (application.getLat() + i.getLatitude())/2;
                            Double centerLon = (application.getLon() + i.getLongitude())/2;
                            LatLng centerPoint = new LatLng(centerLat, centerLon);
                            LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View distanceMarkerView = inflater.inflate(R.layout.map_enemy_distance_marker, null);
                            ((StrokeTextView3) distanceMarkerView.findViewById(R.id.enemy_marker_distance)).setText(shortDistance.toString()+"km");
                            Bitmap distanceViewBitmap = getViewBitmap(distanceMarkerView);
                            BitmapDescriptor distanceBitmap = BitmapDescriptorFactory.fromBitmap(distanceViewBitmap);
                            if(null == (enemyMarkerItem = enemyMarker.get(phoneNumber)))
                                enemyMarkerItem = enemyMarker.get(phoneNumber.substring(3));
                            if(null == enemyMarkerItem){
                                View markerView = inflater.inflate(R.layout.map_enemy_marker, null);
                                ((StrokeTextView3) markerView.findViewById(R.id.enemy_marker_name)).setText(i.getName());
                                ((MyTextView3) markerView.findViewById(R.id.enemy_marker_number)).setText(i.getPhoneNumber());
                                //构建Marker图标
                                Bitmap viewBitmap = getViewBitmap(markerView);
                                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);;
                                Bundle extraMsg = new Bundle();
                                extraMsg.putString("phoneNumber",i.getPhoneNumber());
                                extraMsg.putBoolean("ForE",false);
                                OverlayOptions option = new MarkerOptions()
                                        .position(point)
                                        .icon(bitmap)
                                        .draggable(false)  //设置手势拖拽;
                                        .extraInfo(extraMsg);
                                //在地图上添加Marker，并显示
                                marker = (Marker)baiduMap.addOverlay(option);
                                //构造对象
                                OverlayOptions ooPolyline = new PolylineOptions()
                                        .width(8)
                                        .color(ContextCompat.getColor(getBaseContext(),R.color.white))
                                        .points(points);
                                //添加到地图
                                line1 = (Polyline) baiduMap.addOverlay(ooPolyline);
                                OverlayOptions ooPolyline2 = new PolylineOptions()
                                        .width(6)
                                        .color(ContextCompat.getColor(getBaseContext(),R.color.buttonRed))
                                        .points(points);
                                //添加到地图
                                line2 = (Polyline) baiduMap.addOverlay(ooPolyline2);

                                option = new MarkerOptions()
                                        .position(centerPoint)
                                        .icon(distanceBitmap)
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
                            }else{
                                marker = enemyMarkerItem.getMarker();
                                marker.setPosition(point);
                                line1 = enemyMarkerItem.getLine1();
                                line1.setPoints(points);
                                line2 = enemyMarkerItem.getLine2();
                                line2.setPoints(points);
                                distanceMarker = enemyMarkerItem.getDistanceMarker();
                                distanceMarker.setPosition(centerPoint);
                                distanceMarker.setIcon(distanceBitmap);
                            }
                            break;
                        }
                    }
                }
                stopSelf();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
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

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
