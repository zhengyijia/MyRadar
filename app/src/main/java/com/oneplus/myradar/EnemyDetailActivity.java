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
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;
import com.oneplus.widget.MyEditText;
import com.oneplus.widget.MyTextView2;
import com.oneplus.widget.MyTextView3;
import com.oneplus.widget.MyTextView4;
import com.oneplus.widget.StrokeTextView3;

import java.util.Date;
import java.util.Map;

public class EnemyDetailActivity extends AppCompatActivity {

    private ContactsItem enemyItem = null;

    private boolean editMode = false;

    GeoCoder mSearch = null; // 搜索模块

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_enemy_detail);

        Intent intent = getIntent();
        final MyApplication application = (MyApplication) this.getApplicationContext();
        enemyItem = application.getEnemy(intent.getStringExtra("phoneNumber"));

        final MyTextView4 EnemyName = (MyTextView4)findViewById(R.id.enemy_detail_name);
        EnemyName.setText(enemyItem.getName());
        final MyEditText EnemyNameEdit = (MyEditText)findViewById(R.id.enemy_detail_edit_name);
        EnemyNameEdit.setText(enemyItem.getName());
        MyTextView4 EnemyNumber = (MyTextView4)findViewById(R.id.enemy_number_content);
        EnemyNumber.setText(enemyItem.getPhoneNumber());
        if(enemyItem.getGetLoc()) {
            MyTextView4 EnemyLatLon = (MyTextView4) findViewById(R.id.enemy_lat_lon_content);
            EnemyLatLon.setText(getString(R.string.lat_lon_value, enemyItem.getLatitude(), enemyItem.getLongitude()));
            Date curDate = new Date(System.currentTimeMillis());
            Date lastDate = enemyItem.getDate();
            long diff = (curDate.getTime() - lastDate.getTime()) / 1000;
            MyTextView4 EnemySecondsSince = (MyTextView4) findViewById(R.id.enemy_seconds_since_content);
            String time = Long.toString(diff);
            String formatTime = "";
            if (time.length() <= 3)
                formatTime = time;
            else {
                int remainder = time.length() % 3;
                int result = time.length() / 3;
                if (0 == remainder) {
                    remainder = 3;
                    result -= 1;
                }
                formatTime += time.substring(0, remainder);
                time = time.substring(remainder);
                for (int i = 0; i < result; i++) {
                    formatTime += "''" + time.substring(i * 3, i * 3 + 3);
                }
            }
            EnemySecondsSince.setText(formatTime);

            final MyTextView4 EnemyNearestAddress = (MyTextView4) findViewById(R.id.enemy_nearest_address_content);
            EnemyNearestAddress.setText(R.string.loading);
            EnemyNearestAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(EnemyDetailActivity.this, EnemyNearestAddress.getText().toString(), Toast.LENGTH_LONG).show();
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
                        MyTextView4 EnemyNearestAddress = (MyTextView4) findViewById(R.id.enemy_nearest_address_content);
                        EnemyNearestAddress.setText(R.string.none_result);
                    }
                    //获取反向地理编码结果
                    MyTextView4 EnemyNearestAddress = (MyTextView4) findViewById(R.id.enemy_nearest_address_content);
                    EnemyNearestAddress.setText(result.getAddress());
                }
            };
            mSearch.setOnGetGeoCodeResultListener(listener);
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(new LatLng(enemyItem.getLatitude(), enemyItem.getLongitude())));
        }

        MyTextView2 radarButton = (MyTextView2)findViewById(R.id.enemy_detail_radar_button);
        radarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.destroyActivity();
                finish();
            }
        });

        MyTextView2 friendButton = (MyTextView2)findViewById(R.id.enemy_detail_friend_button);
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EnemyDetailActivity.this, FriendPageActivity.class);
                startActivity(intent);
                application.destroyActivity();
                finish();
            }
        });

        TextView listButton = (TextView)findViewById(R.id.enemy_detail_list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                application.removeActivity();
                finish();
            }
        });

        findViewById(R.id.enemy_detail_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(EnemyDetailActivity.this,R.style.MyDialog);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                EnemyDetailActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
                lp.width = dm.widthPixels*5/6;
                lp.height = dm.widthPixels*35/66;
                dialog.getWindow().setAttributes(lp);
                LayoutInflater lf = (LayoutInflater) EnemyDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup vg = (ViewGroup) lf.inflate(R.layout.delete_enemy_dialog, null);
                dialog.getWindow().setContentView(vg);
                dialog.setCancelable(false);
                ((MyTextView4)vg.findViewById(R.id.enemy_delete_phonenumber)).setText(enemyItem.getPhoneNumber());
                vg.findViewById(R.id.enemy_delete_ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = enemyItem.getPhoneNumber();
                        MyApplication application = (MyApplication) EnemyDetailActivity.this.getApplicationContext();
                        if(enemyItem.getGetLoc()){
                            Map<String,MarkerItem> enemyMarker = application.getEnemyMap();
                            Marker marker = enemyMarker.get(phoneNumber).getMarker();
                            Polyline line1 = enemyMarker.get(phoneNumber).getLine1();
                            Polyline line2 = enemyMarker.get(phoneNumber).getLine2();
                            Marker distanceMarker = enemyMarker.get(phoneNumber).getDistanceMarker();
                            if(null != marker){
                                marker.remove();
                                line1.remove();
                                line2.remove();
                                distanceMarker.remove();
                                enemyMarker.remove(phoneNumber);
                            }
                        }
                        application.deleteEnemy(phoneNumber);
                        dialog.dismiss();
                        finish();
                    }
                });
                vg.findViewById(R.id.enemy_delete_cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(onKeyListener);
            }
        });

        findViewById(R.id.enemy_detail_delete_button).setVisibility(View.INVISIBLE);
        EnemyNameEdit.setVisibility(View.INVISIBLE);

        TextView editButton = (TextView)findViewById(R.id.enemy_detail_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editMode){
                    findViewById(R.id.enemy_detail_delete_button).setVisibility(View.VISIBLE);
                    EnemyNameEdit.setVisibility(View.VISIBLE);
                    EnemyName.setVisibility(View.INVISIBLE);
                    editMode = true;
                }else{
                    findViewById(R.id.enemy_detail_delete_button).setVisibility(View.INVISIBLE);
                    EnemyName.setText(EnemyNameEdit.getText().toString());
                    enemyItem.setName(EnemyNameEdit.getText().toString());
                    application.saveEnemiesList();
                    if(enemyItem.getGetLoc()){
                        Map<String,MarkerItem> enemyMarker = application.getEnemyMap();
                        Marker marker = enemyMarker.get(enemyItem.getPhoneNumber()).getMarker();
                        if(null != marker){
                            LayoutInflater inflater=(LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View markerView = inflater.inflate(R.layout.map_enemy_marker, null);
                            ((StrokeTextView3) markerView.findViewById(R.id.enemy_marker_name)).setText(enemyItem.getName());
                            ((MyTextView3) markerView.findViewById(R.id.enemy_marker_number)).setText(enemyItem.getPhoneNumber());
                            //构建Marker图标
                            Bitmap viewBitmap = getViewBitmap(markerView);
                            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(viewBitmap);;
                            marker.setIcon(bitmap);
                        }
                    }
                    EnemyNameEdit.setVisibility(View.INVISIBLE);
                    EnemyName.setVisibility(View.VISIBLE);
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
