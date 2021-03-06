package com.lncosie.ilandroidos.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.BluetoothConneted;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ImageUrlChange;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.OperatorMessages;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.InterlockOperation;
import com.lncosie.ilandroidos.model.StringTools;
import com.lncosie.ilandroidos.utils.UserTools;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserViewDetailActivity extends EventableActivity
        implements AdapterView.OnItemClickListener {
    @Bind(R.id.user_name)
    TextView userName;
    @Bind(R.id.auth_list)
    ListView authList;

    Users user;
    AuthAdapter adapter;
    UserDetail detailSelected;
    @Bind(R.id.user_image)
    de.hdodenhof.circleimageview.CircleImageView userImage;
    @Bind(R.id.user_name_frame)
    LinearLayout userNameFrame;
    UserDetail active_auth;
    private long userId = -1;
    Context context;
    private Handler handler;
    private String ImageChangedUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        handler = new Handler();
        setContentView(R.layout.activity_user_view_detail);
        ButterKnife.bind(this);
        Bus.register(this);

    }

    @Override
    protected void onPause() {
        pauseDetect = true;
        super.onPause();


    }

    @Override
    protected void onResume() {
        pauseDetect = true;
        super.onResume();
        init();
    }

    @Override
    public void onDestroy() {
        Bus.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Subscribe
    public void bluetoothConneted(BluetoothConneted state) {
        showLoginPassword(state.needPassword);
    }

//    @Override
//    public void onBackPressed() {
////        Bus.post(new UsersChanged());
//        super.onBackPressed();
//    }


    public void backward(View v) {
//        Bus.post(new UsersChanged());
        super.backward(v);
        this.finish();
    }

    /**
     * 修改头像的方法：
     *
     * @param v
     */
    @OnClick(R.id.user_image)
    void user_pick_image(View v) {
        Intent intent = new Intent(UserViewDetailActivity.this,
                ActivityIconSeleted.class);
        intent.putExtra("uid", userId);
        startActivityForResult(intent, 3);

    }

    @OnClick(R.id.user_name_frame)
    public void user_name_frame(View v) {
        Intent intent = new Intent(this, SetupDescriptionActivity.class);
        intent.putExtra("description", user.name);
        startActivityForResult(intent, 1);
    }

    /**
     * 添加新用户
     */
    @OnClick(R.id.auth_add)
    public void authAdd(View v) {
        if (!checkSendable()) {
            Bus.post(new NetworkError());
            return;
        }
        Intent intent = new Intent(UserViewDetailActivity.this, UserAddActivity.class);
        intent.putExtra("uid", userId == -1 ? -1 : userId);
        intent.putExtra("edit", true);
        startActivityForResult(intent, 3);
        finish();
    }

    /**
     * @param imageUrlChange
     */
    @Subscribe
    public void getImageUrl(ImageUrlChange imageUrlChange) {
        ImageChangedUrl = imageUrlChange.getImagePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:

                break;
            case 1:
                String username = data.getStringExtra("description");
                user.name = username;
                user.save();
                userName.setText(username);
                Bus.post(new UsersChanged());
                break;
            case 2:
//                pauseDetect = false;
                String password = data.getStringExtra("password");
                InterlockOperation.modifyPwd(detailSelected.type,
                        detailSelected.uid, StringTools.getPwdBytes(detailSelected.uid, password));
                break;
            case 3:
                user.image = ImageChangedUrl;
                user.save();
                Bus.post(new UsersChanged());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        UserTools.getInstance().setIcon(context, userImage, user.image);
                    }
                });
                Bus.post(new UsersChanged());
                break;

        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                if (checkSendable() == false) {
                    return;
                }
                detailSelected = adapter.details.get(position);
                if ((int) arg0 == 0) {
                    Applyable del = new Applyable() {
                        @Override
                        public void apply(Object arg0, Object arg1) {
                            InterlockOperation.deleteId(detailSelected.type, detailSelected.uid);
                            if (adapter.details.size() == 1) {
                                user.delete();
                                Bus.post(new UsersChanged());
                                finish();
                            }
                        }
                    };
                    String message = getString(R.string.delete_conform_id);
                    MenuYesnoFragment fragment = MenuYesnoFragment.newInstance(R.string.delete_conform_title,
                            message, del);
                    fragment.show(getSupportFragmentManager(), "");
                    return;
                }
                Intent intent = new Intent(UserViewDetailActivity.this, SetupPasswordActivity.class);
                startActivityForResult(intent, 2);
            }
        };
        active_auth = adapter.details.get(position);
        showMenu(active_auth.type, active_auth.uid, applyable);
    }

    void showMenu(long arg0, long arg1, Applyable applyable) {
        DialogFragment dialogFragment = MenuAuthFragment.newInstance(arg0, arg1, applyable);
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
    }

    void init() {
        userId = getIntent().getLongExtra("uid", -1);
        if (userId != -1) {

            user = DbHelper.getUser(userId);
            userName.setText(user.name);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    UserTools.getInstance().setIcon(context, userImage, user.image);
                }
            });
//            UserTools.getInstance().setIcon(context,userImage,user.image);
            adapter = new AuthAdapter();
            authList.setAdapter(adapter);
        }

        authList.setOnItemClickListener(this);
    }


    @Subscribe
    public void delFromLock(OperatorMessages.OpDelAuth result) {
        if (result.error != 0 || active_auth == null) {
            return;
        }
        adapter.details.remove(active_auth);
        active_auth.delete();
        active_auth = null;
        adapter.notifyDataSetChanged();
    }

    static class ViewHolder {
        @Bind(R.id.auth_type)
        TextView authType;
        @Bind(R.id.auth_id)
        TextView authId;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(UserDetail detail) {
            if (detail.type == 1) {
                authType.setText(R.string.finger);
                authId.setText(String.format("%02d", detail.uid));
            } else {
                authType.setText(R.string.password);
                authId.setText(String.format("%02d", detail.uid) + "******");
            }

        }
    }

    class AuthAdapter extends BaseAdapter {
        List<UserDetail> details = null;

        AuthAdapter() {
            init();
        }

        void init() {
            details = DbHelper.getUserDetails(user.getId());
        }

        void reInit() {
            init();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return details.size();
        }

        @Override
        public Object getItem(int position) {
            //return null;
            return details.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                View item = getLayoutInflater().inflate(R.layout.item_auth_id, null);
                holder = new ViewHolder(item);
                item.setTag(holder);
                convertView = item;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bind(details.get(position));
            return convertView;
        }
    }

}
