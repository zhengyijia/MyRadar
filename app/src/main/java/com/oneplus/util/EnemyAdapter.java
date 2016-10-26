package com.oneplus.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;
import com.oneplus.model.ContactsItem;
import com.oneplus.model.MarkerItem;
import com.oneplus.myradar.MyApplication;
import com.oneplus.myradar.R;
import com.oneplus.widget.MyTextView4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Oneplus on 2016/10/15.
 */
public class EnemyAdapter extends BaseAdapter{

    private List<ContactsItem> myList = new ArrayList<>();
    Context context;

    public EnemyAdapter(Context context, List<ContactsItem> myList){
        this.myList = myList;
        this.context = context;
    }

    @Override
    public int getCount(){
        return (myList==null)?0:myList.size();
    }

    @Override
    public Object getItem(int position){
        return myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        MyTextView4 EnemyName;
        RelativeLayout EnemyDeleteButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final ContactsItem contactsItem = (ContactsItem)getItem(position);
        ViewHolder viewHolder = null;
        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.enemy_item,null);
            viewHolder = new ViewHolder();
            viewHolder.EnemyName = (MyTextView4)convertView.findViewById(R.id.enemy_name);
            viewHolder.EnemyDeleteButton = (RelativeLayout)convertView.findViewById(R.id.enemy_delete_button);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.EnemyName.setText(contactsItem.getName());
        viewHolder.EnemyDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(EnemyAdapter.this.context,R.style.MyDialog);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                ((Activity)EnemyAdapter.this.context).getWindowManager().getDefaultDisplay().getMetrics(dm);
                lp.width = dm.widthPixels*5/6;
                lp.height = dm.widthPixels*35/66;
                dialog.getWindow().setAttributes(lp);
                LayoutInflater lf = (LayoutInflater) EnemyAdapter.this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup vg = (ViewGroup) lf.inflate(R.layout.delete_enemy_dialog, null);
                dialog.getWindow().setContentView(vg);
                dialog.setCancelable(false);
                ((MyTextView4)vg.findViewById(R.id.enemy_delete_phonenumber)).setText(myList.get(position).getPhoneNumber());
                vg.findViewById(R.id.enemy_delete_ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = myList.get(position).getPhoneNumber();
                        MyApplication application = (MyApplication) EnemyAdapter.this.context.getApplicationContext();
                        if(myList.get(position).getGetLoc()){
                            Map<String,MarkerItem> enemyMarker = application.getEnemyMap();
                            Marker marker = enemyMarker.get(phoneNumber).getMarker();
                            Polyline line1 = enemyMarker.get(phoneNumber).getLine1();
                            Polyline line2 = enemyMarker.get(phoneNumber).getLine2();
                            Marker distanceMarker = enemyMarker.get(phoneNumber).getMarker();
                            if(null != marker){
                                marker.remove();
                                line1.remove();
                                line2.remove();
                                distanceMarker.remove();
                                enemyMarker.remove(phoneNumber);
                            }
                        }
                        application.deleteEnemy(phoneNumber);
                        notifyDataSetChanged();
                        dialog.dismiss();
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
        viewHolder.EnemyDeleteButton.setVisibility(View.INVISIBLE);
        return convertView;
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

}
