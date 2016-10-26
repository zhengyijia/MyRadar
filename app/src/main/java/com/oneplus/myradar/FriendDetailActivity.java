package com.oneplus.myradar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;
import com.oneplus.widget.MyEditText;
import com.oneplus.widget.MyTextView2;
import com.oneplus.widget.MyTextView3;
import com.oneplus.widget.MyTextView4;
import com.oneplus.widget.StrokeTextView3;

import java.util.Date;
import java.util.Map;

public class FriendDetailActivity extends AppCompatActivity {

    private ContactsItem friendItem;

    private boolean editMode = false;

    GeoCoder mSearch = null; // 搜索模块

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_friend_detail);

        Intent intent = getIntent();
        final MyApplication application = (MyApplication) this.getApplicationContext();
        friendItem = application.getFriend(intent.getStringExtra("phoneNumber"));

        final MyTextView4 FriendName = (MyTextView4)findViewById(R.id.friend_detail_name);
        FriendName.setText(friendItem.getName());
        final MyEditText FriendNameEdit = (MyEditText)findViewById(R.id.friend_detail_edit_name);
        FriendNameEdit.setText(friendItem.getName());
        MyTextView4 FriendNumber = (MyTextView4)findViewById(R.id.friend_number_content);
        FriendNumber.setText(friendItem.getPhoneNumber());
        MyTextView4 FriendLatLon = (MyTextView4)findViewById(R.id.friend_lat_lon_content);
        FriendLatLon.setText(getString(R.string.lat_lon_value,friendItem.getLatitude(),friendItem.getLongitude()));
        Date curDate = new Date(System.currentTimeMillis());
        Date lastDate = friendItem.getDate();
        long diff = (curDate.getTime() - lastDate.getTime())/1000;
        MyTextView4 FriendSecondsSince = (MyTextView4)findViewById(R.id.friend_seconds_since_content);
        String time = Long.toString(diff);
        String formatTime = "";
        if(time.length() <= 3)
            formatTime = time;
        else {
            int remainder = time.length() % 3;
            int result = time.length() / 3;
            if (0 == remainder) {
                remainder = 3;
                result -= 1;
            }
            formatTime += time.substring(0,remainder);
            time = time.substring(remainder);
            for(int i = 0; i < result; i++){
                formatTime += "''" + time.substring(i*3,i*3+3);
            }
        }
        FriendSecondsSince.setText(formatTime);

        final MyTextView4 FriendNearestAddress = (MyTextView4)findViewById(R.id.friend_nearest_address_content);
        FriendNearestAddress.setText(R.string.loading);
        FriendNearestAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FriendDetailActivity.this, FriendNearestAddress.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            public void onGetGeoCodeResult(GeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                }
                //获取地理编码结果
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有找到检索结果
                    MyTextView4 FriendNearestAddress = (MyTextView4)findViewById(R.id.friend_nearest_address_content);
                    FriendNearestAddress.setText(R.string.none_result);
                }
                //获取反向地理编码结果
                MyTextView4 FriendNearestAddress = (MyTextView4)findViewById(R.id.friend_nearest_address_content);
                FriendNearestAddress.setText(result.getAddress());
            }
        };
        mSearch.setOnGetGeoCodeResultListener(listener);
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
            .location(new LatLng(friendItem.getLatitude(),friendItem.getLongitude())));

        MyTextView2 radarButton = (MyTextView2)findViewById(R.id.friend_detail_radar_button);
        radarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.destroyActivity();
                finish();
            }
        });

        MyTextView2 enemyButton = (MyTextView2)findViewById(R.id.friend_detail_enemy_button);
        enemyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendDetailActivity.this, EnemyPageActivity.class);
                startActivity(intent);
                application.destroyActivity();
                finish();
            }
        });

        TextView listButton = (TextView)findViewById(R.id.friend_detail_list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.removeActivity();
                finish();
            }
        });

        findViewById(R.id.friend_detail_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(FriendDetailActivity.this,R.style.MyDialog);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                FriendDetailActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                lp.width = dm.widthPixels*5/6;
                lp.height = dm.widthPixels*35/66;
                dialog.getWindow().setAttributes(lp);
                LayoutInflater lf = (LayoutInflater) FriendDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup vg = (ViewGroup) lf.inflate(R.layout.delete_friend_dialog, null);
                dialog.getWindow().setContentView(vg);
                dialog.setCancelable(false);
                ((MyTextView4)vg.findViewById(R.id.friend_delete_phonenumber)).setText(friendItem.getPhoneNumber());
                vg.findViewById(R.id.friend_delete_ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = friendItem.getPhoneNumber();
                        MyApplication application = (MyApplication) FriendDetailActivity.this.getApplicationContext();
                        if(friendItem.getGetLoc()){
                            Map<String,MarkerItem> friendMarker = application.getFriendMap();
                            Marker marker = friendMarker.get(phoneNumber).getMarker();
                            Polyline line1 = friendMarker.get(phoneNumber).getLine1();
                            Polyline line2 = friendMarker.get(phoneNumber).getLine2();
                            Marker distanceMarker = friendMarker.get(phoneNumber).getDistanceMarker();
                            if(null != marker){
                                marker.remove();
                                line1.remove();
                                line2.remove();
                                distanceMarker.remove();
                                friendMarker.remove(phoneNumber);
                            }
                        }
                        application.deleteFriend(phoneNumber);
                        dialog.dismiss();
                        finish();
                    }
                });
                vg.findViewById(R.id.friend_delete_cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(onKeyListener);
            }
        });

        findViewById(R.id.friend_detail_delete_button).setVisibility(View.INVISIBLE);
        FriendNameEdit.setVisibility(View.INVISIBLE);

        TextView editButton = (TextView)findViewById(R.id.friend_detail_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editMode){
                    findViewById(R.id.friend_detail_delete_button).setVisibility(View.VISIBLE);
                    FriendNameEdit.setVisibility(View.VISIBLE);
                    FriendName.setVisibility(View.INVISIBLE);
                    editMode = true;
                }else{
                    findViewById(R.id.friend_detail_delete_button).setVisibility(View.INVISIBLE);
                    FriendName.setText(FriendNameEdit.getText().toString());
                    friendItem.setName(FriendNameEdit.getText().toString());
                    application.saveFriendsList();
                    if(friendItem.getGetLoc()){
                        Map<String,MarkerItem> friendMarker = application.getFriendMap();
                        Marker marker = friendMarker.get(friendItem.getPhoneNumber()).getMarker();
                        if(null != marker){
                            LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View markerView = inflater.inflate(R.layout.map_friend_marker, null);
                            ((StrokeTextView3) markerView.findViewById(R.id.friend_marker_name)).setText(friendItem.getName());
                            ((MyTextView3) markerView.findViewById(R.id.friend_marker_number)).setText(friendItem.getPhoneNumber());
                            //构建Marker图标
                            Bitmap viewBitmap = getViewBitmap(markerView);
                            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);;
                            marker.setIcon(bitmap);
                        }
                    }
                    FriendNameEdit.setVisibility(View.INVISIBLE);
                    FriendName.setVisibility(View.VISIBLE);
                    editMode = false;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        MyApplication application = (MyApplication)this.getApplicationContext();
        application.removeActivity();
        finish();
    }

    //按下Back键隐藏Dialog
    private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode,
                             KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                dialog.dismiss();
            }
            return false;
        }
    };

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

}
