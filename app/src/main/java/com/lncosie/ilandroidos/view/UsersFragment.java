package com.lncosie.ilandroidos.view;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.DeviceConnedted;
import com.lncosie.ilandroidos.bus.DeviceDisconnected;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.bus.ViewUserLog;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.BitmapTool;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.InterlockOperation;
import com.lncosie.ilandroidos.model.Sync;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends ActiveAbleFragment implements AdapterView.OnItemClickListener {


    @Bind(R.id.users)
    ListView users;
    DeviceAdapter adapter;
    @Bind(R.id.swiper)
    SwipeRefreshLayout swiper;

    @Bind(R.id.title)
    TextView title;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        ButterKnife.bind(this, view);
        adapter = new DeviceAdapter();
        users.setAdapter(adapter);
        setupSwiper();
        users.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onActive(Object arg) {
        super.onActive(arg);
    }
    @Bind(R.id.net_tip)
    TextView net_tip;
    @Subscribe
    public void OnConnected(DeviceConnedted state){
        net_tip.setVisibility(View.GONE);
    }
    @Subscribe
    public void OnConnected(DeviceDisconnected state){
        net_tip.setVisibility(View.VISIBLE);
    }
    @OnClick(R.id.net_tip)
    public void connect(){
        super.autoConnet();
    }
    @Subscribe
    public void langChanged(LanguageChanged languageChanged) {
        //title.setText(R.string.users);
        adapter.notifyDataSetChanged();
    }
    void setupSwiper() {
        swiper.setEnabled(true);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Sync.syncIds(new Applyable() {
                    @Override
                    public void apply(Object arg0, Object arg1) {
                        swiper.setRefreshing(false);
                        adapter.reInit();
                    }
                    @Override
                    public void cancel() {
                        swiper.setRefreshing(false);
                    }
                });

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Applyable applyable = new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                int action = (int) arg0;
                long gid = (long) arg1;
                if (action == 0) {
                    Intent intent = new Intent(getContext(), UserViewDetailActivity.class);
                    intent.putExtra("uid", gid);
                    startActivity(intent);
                } else if (action == 1) {
                    Bus.post(new ViewUserLog(gid));
                } else if (action == 2) {
                    if (checkSendable() == false) {
                        return;
                    }
                    deleteUser(gid);
                }

            }
        };
        MenuUserFragment fragment = MenuUserFragment.newInstance(adapter.users.get(position).getId(), applyable);
        fragment.show(getFragmentManager().beginTransaction(), "");

    }

    private void deleteUser(final long gid) {
        Applyable delete = new Applyable() {
            public void apply(Object arg0, Object arg1) {
                boolean deleteGid = true;
                List<UserDetail> details = DbHelper.getUserDetails(gid);
                if (details.size() == 0) {
                    DbHelper.deleteUser(gid);
                    adapter.reInit();
                    return;
                }
                if (!Net.get().isSendable()) {
                    Bus.post(new NetworkError());
                    return;
                }
                for (UserDetail detail : details) {
                    if (detail.type == 0 && detail.uid == 0) {
                        deleteGid = false;
                        break;
                    }
                }
                for (int i = 0; i < details.size(); i++) {
                    UserDetail detail = details.get(i);
                    Del del = new Del(detail, gid, (i == details.size() - 1) && deleteGid);
                    InterlockOperation.deleteIdSlient(detail.type, detail.uid, del);
                }
            }
        };
        final Users user = DbHelper.getUser(gid);
        if (user == null)
            return;
        String message = getString(R.string.delete_conform_user);
        MenuYesnoFragment yesnoFragment = MenuYesnoFragment.newInstance(R.string.delete_conform_title, message, delete);
        yesnoFragment.show(getFragmentManager(), "");
        return;
//
//        String message=String.format(getString(R.string.delete_conform),user.name);
//        new AlertDialog.Builder(getActivity())
//                .setTitle(R.string.delete_conform_title)
//                .setMessage(message)
//                .setPositiveButton(android.R.string.yes,delete)
//                .setNegativeButton(android.R.string.no, null)
//                .show().getWindow().setTitleColor(getResources().getColor(R.color.colorPrimary));
    }

    @OnClick(R.id.user_add)
    public void userAdd(View v) {
        Intent intent = new Intent(getContext(), UserAddActivity.class);
        intent.putExtra("uid", -1);
        intent.putExtra("edit", true);
        startActivity(intent);
    }

    @Subscribe
    public void onUserChanged(UsersChanged changed) {
        adapter.reInit();
    }

    static class ViewHolder {
        @Bind(R.id.user_image)
        ImageView userImage;
        @Bind(R.id.user_name)
        TextView userName;
//        @Bind(R.id.password_cnt)
//        TextView passwordCnt;
//        @Bind(R.id.finger_cnt)
//        TextView fingerCnt;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(UserWithTime user) {
            userName.setText(user.name);
            userImage.setImageBitmap(BitmapTool.decodeBitmap(userImage.getContext(), user.image));
//            passwordCnt.setText(String.valueOf(user.pAccounts));
//            fingerCnt.setText(String.valueOf(user.fAccounts));
        }
    }

    class Del extends Applyable {
        UserDetail detail;
        boolean del;
        long gid;

        Del(UserDetail detail, long gid, boolean del) {
            this.detail = detail;
            this.gid = gid;
            this.del = del;
        }

        public void cancel() {
        }

        public void apply(Object arg0, Object arg1) {
            detail.delete();
            if (del) {
                DbHelper.deleteUser(gid);
                adapter.reInit();
            }
        }
    }

    class DeviceAdapter extends BaseAdapter {
        List<UserWithTime> users = null;

        DeviceAdapter() {
            super();
            init();
        }

        void init() {
            users = DbHelper.getUsers();

        }

        void reInit() {
            init();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                View item = getLayoutInflater(null).inflate(R.layout.item_users, null);
                holder = new ViewHolder(item);
                item.setTag(holder);
                convertView = item;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.bind(users.get(position));
            return convertView;
        }
    }


}
