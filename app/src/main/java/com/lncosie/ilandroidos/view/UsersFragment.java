package com.lncosie.ilandroidos.view;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.LoginSuccess;
import com.lncosie.ilandroidos.bus.DeviceDisconnected;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.UserSet;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.bus.ViewUserLog;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.InterlockOperation;
import com.lncosie.ilandroidos.model.Sync;
import com.lncosie.ilandroidos.utils.UserTools;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

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

    long userId = 0;
    public final Handler handler = new Handler();

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        ButterKnife.bind(this, view);
        adapter = new DeviceAdapter(getContext());
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
    public void OnConnected(LoginSuccess state) {
        net_tip.setVisibility(View.GONE);
    }

    @Subscribe
    public void OnConnected(DeviceDisconnected state) {
        net_tip.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.net_tip)
    public void connect() {
        super.autoConnet();
    }

    @Subscribe
    public void langChanged(LanguageChanged languageChanged) {
        //title.setText(R.string.users);
        adapter.notifyDataSetChanged();
    }

    void setupSwiper() {
        swiper.setEnabled(false);
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
                if (!checkSendable()) {
                    Bus.post(new NetworkError());
                    return;
                }
                int action = (int) arg0;
                userId = (long) arg1;
                switch (action) {
                    case 0:
                        //编辑用户
                        Intent intent = new Intent(getContext(), UserViewDetailActivity.class);
                        intent.putExtra("uid", userId);
                        Bus.post(new UserSet());
                        startActivity(intent);
                        break;
                    case 1:
                        Bus.post(new ViewUserLog(userId));
                        break;
                    case 2:
                        deleteUser(userId);
                        break;
                }
            }
        };
        MenuUserFragment fragment = MenuUserFragment.newInstance(adapter.users.get(position).getId(), applyable);
        fragment.show(getFragmentManager().beginTransaction(), "");
    }


    private void deleteUser(final long gid) {
        if (!checkSendable()) {
            Bus.post(new NetworkError());
            return;
        }
        Applyable delete = new Applyable() {
            public void apply(Object arg0, Object arg1) {
                boolean deleteGid = true;
                List<UserDetail> details = DbHelper.getUserDetails(gid);
                if (details.size() == 0) {
                    DbHelper.deleteUser(gid);
                    adapter.reInit();
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
    }

    /**
     * 添加新用户
     *
     * @param v
     */
    @OnClick(R.id.user_add)
    public void userAdd(View v) {
        if (!checkSendable()) {
            Bus.post(new NetworkError());
            return;
        }
        Intent intent = new Intent(getContext(), UserAddActivity.class);
        intent.putExtra("uid", (long) -1);
        intent.putExtra("edit", true);
        Bus.post(new UserSet());
        startActivity(intent);
    }

    @Subscribe
    public void onUserChanged(UsersChanged changed) {
        adapter.reInit();
    }

    static class ViewHolder {
        @Bind(R.id.user_image)
        CircleImageView userImage;
        @Bind(R.id.user_name)
        TextView userName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(Context context, UserWithTime user) {
            Log.d("User", user.toString());
            userName.setText(user.name);
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    UserTools.getInstance().setIcon(context, userImage, user.image);
                }
            });
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
        private Context context;

        DeviceAdapter(Context context) {
            super();
            this.context = context;
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
            holder.bind(context, users.get(position));
            return convertView;
        }
    }


}
