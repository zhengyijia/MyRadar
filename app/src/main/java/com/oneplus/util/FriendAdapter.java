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
 * Created by Oneplus on 2016/10/11.
 */
public class FriendAdapter extends BaseAdapter{

    private List<ContactsItem> myList = new ArrayList<>();
    Context context;

    public FriendAdapter(Context context, List<ContactsItem> myList){
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
        MyTextView4 FriendName;
        RelativeLayout FriendDeleteButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final ContactsItem contactsItem = (ContactsItem)getItem(position);
        ViewHolder viewHolder = null;
        if(convertView==null){
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.friend_item,null);
            viewHolder = new ViewHolder();
            viewHolder.FriendName = (MyTextView4)convertView.findViewById(R.id.friend_name);
            viewHolder.FriendDeleteButton = (RelativeLayout)convertView.findViewById(R.id.friend_delete_button);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.FriendName.setText(contactsItem.getName());
        viewHolder.FriendDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(FriendAdapter.this.context,R.style.MyDialog);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                ((Activity)FriendAdapter.this.context).getWindowManager().getDefaultDisplay().getMetrics(dm);
                lp.width = dm.widthPixels*5/6;
                lp.height = dm.widthPixels*35/66;
                dialog.getWindow().setAttributes(lp);
                LayoutInflater lf = (LayoutInflater) FriendAdapter.this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup vg = (ViewGroup) lf.inflate(R.layout.delete_friend_dialog, null);
                dialog.getWindow().setContentView(vg);
                dialog.setCancelable(false);
                ((MyTextView4)vg.findViewById(R.id.friend_delete_phonenumber)).setText(myList.get(position).getPhoneNumber());
                vg.findViewById(R.id.friend_delete_ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = myList.get(position).getPhoneNumber();
                        MyApplication application = (MyApplication) FriendAdapter.this.context.getApplicationContext();
                        if(myList.get(position).getGetLoc()){
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
                        notifyDataSetChanged();
                        dialog.dismiss();
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
        viewHolder.FriendDeleteButton.setVisibility(View.INVISIBLE);
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
