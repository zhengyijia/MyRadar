package com.oneplus.myradar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oneplus.model.ContactsItem;
import com.oneplus.util.FriendAdapter;
import com.oneplus.widget.MyTextView2;
import com.oneplus.widget.MyTextView3;

import java.util.ArrayList;
import java.util.List;

public class FriendPageActivity extends AppCompatActivity {

    private List<ContactsItem> FriendList = null;

    private ListView FriendListView = null;

    private String name = null;
    private String phoneNumber = null;
    private boolean hasInputName = false;

    private boolean editMode = false;

    private FriendAdapter friendAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_friend_page);

        final MyApplication application = (MyApplication) this.getApplicationContext();
        FriendList = application.getFriendsList();

        FriendListView = (ListView) findViewById(R.id.friend_list);
        friendAdapter = new FriendAdapter(FriendPageActivity.this, FriendList);
        FriendListView.setAdapter(friendAdapter);

        FriendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FriendPageActivity.this, FriendDetailActivity.class);
                intent.putExtra("phoneNumber", FriendList.get(position).getPhoneNumber());
                startActivity(intent);
                application.addActivity(FriendPageActivity.this);
            }
        });

        RelativeLayout AddButton = (RelativeLayout) findViewById(R.id.friend_add_button);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasInputName = false;
                final Dialog dialog = new Dialog(FriendPageActivity.this, R.style.MyDialog);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.show();
                dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                lp.width = dm.widthPixels * 5 / 6;
                lp.height = dm.widthPixels * 35 / 66;
                dialog.getWindow().setAttributes(lp);
                LayoutInflater lf = (LayoutInflater) FriendPageActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup vg = (ViewGroup) lf.inflate(R.layout.add_friend_dialog, null);
                final EditText editText = (EditText) vg.findViewById(R.id.friend_add_input_box);
                final MyTextView3 myTextView3 = (MyTextView3) vg.findViewById(R.id.friend_add_input_what);
                dialog.getWindow().setContentView(vg);
                dialog.setCancelable(false);
                dialog.getWindow().findViewById(R.id.friend_add_ok_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!hasInputName) {
                            name = editText.getText().toString();
                            hasInputName = true;
                            myTextView3.setText(R.string.number);
                            editText.setText("");
                            editText.setInputType(InputType.TYPE_CLASS_PHONE);
                        } else {
                            phoneNumber = editText.getText().toString();
                            if (phoneNumber.equals("")) {
                                Toast.makeText(getApplicationContext(), R.string.empty_number_error,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                MyApplication application = (MyApplication) FriendPageActivity.this.getApplicationContext();
                                if (!application.addFriend(name, phoneNumber))
                                    Toast.makeText(getApplicationContext(), R.string.add_friend_error,
                                            Toast.LENGTH_SHORT).show();
                                else {
                                    hasInputName = false;
                                    friendAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            }
                        }
                    }
                });
                dialog.getWindow().findViewById(R.id.friend_add_cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(onKeyListener);
            }
        });

        MyTextView2 radarButton = (MyTextView2) findViewById(R.id.friend_radar_button);
        radarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MyTextView2 enemyButton = (MyTextView2) findViewById(R.id.friend_enemy_button);
        enemyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendPageActivity.this, EnemyPageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView editButton = (TextView) findViewById(R.id.friend_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editMode) {
                    for (int i = 0; i < FriendListView.getChildCount(); i++) {
                        FriendListView.getChildAt(i).findViewById(R.id.friend_delete_button).setVisibility(View.INVISIBLE);
                    }
                    FriendListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            for (int i = 0; i < FriendListView.getChildCount(); i++) {
                                FriendListView.getChildAt(i).findViewById(R.id.friend_delete_button).setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    editMode = false;
                } else {
                    for (int i = 0; i < FriendListView.getChildCount(); i++) {
                        FriendListView.getChildAt(i).findViewById(R.id.friend_delete_button).setVisibility(View.VISIBLE);
                    }
                    FriendListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            for (int i = 0; i < FriendListView.getChildCount(); i++) {
                                FriendListView.getChildAt(i).findViewById(R.id.friend_delete_button).setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    editMode = true;
                }
            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        friendAdapter.notifyDataSetChanged();
    }

}
