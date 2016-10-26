package com.oneplus.myradar;

import android.app.Activity;
import android.app.Application;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Oneplus on 2016/10/14.
 */
public class MyApplication extends Application{

    private List<ContactsItem> FriendsList = null;
    private List<ContactsItem> EnemiesList = null;

    private double lat;
    private double lon;

    private List<Activity> activityList = new LinkedList();

    private Map<String,MarkerItem> friendMarker = new HashMap<>();
    private Map<String,MarkerItem> enemyMarker = new HashMap<>();

    private BaiduMap baiduMap = null;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        FriendsList = (List<ContactsItem>) getObject("RadarFriends.dat");
        if (null == FriendsList)
            FriendsList = new ArrayList<>();
        EnemiesList = (List<ContactsItem>)getObject("RadarEnemies.dat");
        if(null == EnemiesList)
            EnemiesList = new ArrayList<>();
    }

    public List<ContactsItem> getFriendsList(){
        return FriendsList;
    }
    public List<ContactsItem> getEnemiesList(){
        return EnemiesList;
    }
    public boolean addFriend(String name, String phoneNumber){
        for(ContactsItem i:FriendsList){
            if(i.getPhoneNumber().equals(phoneNumber)){
                return false;
            }
        }
        for(ContactsItem i:EnemiesList){
            if(i.getPhoneNumber().equals(phoneNumber)){
                return false;
            }
        }
        ContactsItem tempItem = new ContactsItem();
        tempItem.setName(name);
        tempItem.setPhoneNumber(phoneNumber);
        FriendsList.add(tempItem);
        saveObject(FriendsList, "RadarFriends.dat");
        return true;
    }
    public boolean addEnemy(String name, String phoneNumber){
        for(ContactsItem i:EnemiesList){
            if(i.getPhoneNumber().equals(phoneNumber))
                return false;
        }
        for(ContactsItem i:FriendsList){
            if(i.getPhoneNumber().equals(phoneNumber))
                return false;
        }
        ContactsItem tempItem = new ContactsItem();
        tempItem.setName(name);
        tempItem.setPhoneNumber(phoneNumber);
        EnemiesList.add(tempItem);
        saveObject(EnemiesList, "RadarEnemies.dat");
        return true;
    }
    public void deleteFriend(String phoneNumber){
        for(ContactsItem i:FriendsList){
            if(i.getPhoneNumber().equals(phoneNumber)) {
                FriendsList.remove(i);
                saveObject(FriendsList, "RadarFriends.dat");
                break;
            }
        }
    }
    public void deleteEnemy(String phoneNumber){
        for(ContactsItem i:EnemiesList){
            if(i.getPhoneNumber().equals(phoneNumber)){
                EnemiesList.remove(i);
                saveObject(EnemiesList, "RadarEnemies.dat");
                break;
            }
        }
    }
    public ContactsItem getFriend(String phoneNumber){
        for (ContactsItem i:FriendsList){
            if(i.getPhoneNumber().equals(phoneNumber)){
                return i;
            }
        }
        return null;
    }
    public ContactsItem getEnemy(String phoneNumber){
        for(ContactsItem i:EnemiesList){
            if(i.getPhoneNumber().equals(phoneNumber)){
                return i;
            }
        }
        return null;
    }
    public void saveFriendsList(){
        saveObject(FriendsList, "RadarFriends.dat");
    }
    public void saveEnemiesList(){
        saveObject(EnemiesList, "RadarEnemies.dat");
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void destroyActivity() {
        for(Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
    }

    public void removeActivity(){
        activityList.clear();
    }

    public void setLat(double lat){this.lat = lat;}
    public void setLon(double lon){this.lon = lon;}
    public double getLat(){return lat;}
    public double getLon(){return lon;}

    public void addFriendMarker(String phoneNumber, MarkerItem friendMarkerItem){
        friendMarker.put(phoneNumber, friendMarkerItem);
    }

    public void addEnemyMarker(String phoneNumber, MarkerItem enemyMarkerItem){
        enemyMarker.put(phoneNumber, enemyMarkerItem);
    }

    public Map<String,MarkerItem> getFriendMap(){
        return friendMarker;
    }

    public Map<String,MarkerItem> getEnemyMap(){
        return enemyMarker;
    }

    public void setMap(BaiduMap baiduMap){
        this.baiduMap = baiduMap;
    }

    public BaiduMap getMap(){
        return baiduMap;
    }

    private void saveObject(Object objectList, String name) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = this.openFileOutput(name, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(objectList);
        } catch (Exception e) {
            e.printStackTrace();
            //这里是保存文件产生异常
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    //fos流关闭异常
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    //oos流关闭异常
                    e.printStackTrace();
                }
            }
        }
    }

    private Object getObject(String name) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = this.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            //这里是读取文件产生异常
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //fis流关闭异常
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    //ois流关闭异常
                    e.printStackTrace();
                }
            }
        }
        //读取产生异常，返回null
        return null;
    }

}
