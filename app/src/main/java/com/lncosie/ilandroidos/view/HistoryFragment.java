package com.lncosie.ilandroidos.view;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.HistoryChanged;
import com.lncosie.ilandroidos.bus.LanguageChanged;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.model.BitmapTool;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.Sync;
import com.lncosie.ilandroidos.model.TimeTools;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends ActiveAbleFragment implements TextWatcher {

    @Bind(R.id.search)
    EditText search;
    @Bind(R.id.history)
    ListView history;

    HistoryAdapter adapter;
    @Bind(R.id.swiper)
    SwipeRefreshLayout swiper;
    @Bind(R.id.title)
    TextView title;


    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        Bus.register(this);
        adapter = new HistoryAdapter(this.getActivity());
        history.setAdapter(adapter);
        setupSwiper();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        Bus.unregister(this);
    }

    boolean changed = false;

    @Subscribe
    public void langChanged(LanguageChanged languageChanged) {
        //title.setText(R.string.history);
        //adapter.notifyDataSetChanged();
    }
    @Subscribe
    public void onUserChanged(UsersChanged changed) {
        adapter.reInit();
        adapter.filterInit(null);
        adapter.notifyDataSetChanged();
    }
    @Subscribe
    public void onHistoryChanged(HistoryChanged c) {
        changed = true;
    }

    @Override
    protected void onActive(Object arg) {
        super.onActive(arg);
        if (arg != null) {
            adapter.reInit();
            Users user = DbHelper.getUser((long) arg);
            adapter.filterInit(user.name);
            adapter.notifyDataSetChanged();
        } else {
            if (changed == true) {
                changed = false;
                syncHistory();
                return;
            }
            adapter.filterInit(null);
            adapter.notifyDataSetChanged();
        }
    }

    void setupSwiper() {
        search.addTextChangedListener(this);
        swiper.setEnabled(true);
        history.setTextFilterEnabled(false);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncHistory();
            }
        });
    }

    private void syncHistory() {
        swiper.setRefreshing(true);
        Sync.syncHistory(new Applyable() {
            @Override
            public void apply(Object arg0, Object arg1) {
                swiper.setRefreshing(false);
                adapter.reInit();
                adapter.filterInit(null);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void cancel() {
                swiper.setRefreshing(false);
            }
        });
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String query = s.toString();
        adapter.getFilter().filter(query);
    }

    interface ViewBase {
        void bind(Context context, TimeWithUser history);
    }

    static class NodeView implements ViewBase {
        @Bind(R.id.open_user_img)
        ImageView openUserImg;
        @Bind(R.id.open_user_name)
        TextView openUserName;
        @Bind(R.id.open_type)
        TextView openType;
        @Bind(R.id.open_time)
        TextView openTime;

        NodeView(View view) {
            ButterKnife.bind(this, view);
        }

        public void bind(Context context, TimeWithUser history) {
            openUserImg.setImageBitmap(BitmapTool.decodeBitmap(context, history.image));
            openUserName.setText(history.name);
            openType.setText(history.type==0?R.string.password:R.string.finger);
            openTime.setText(TimeTools.toTimeString(history.time));
        }
    }

    static class HeaderView implements ViewBase {
        @Bind(R.id.open_date)
        TextView openDate;

        HeaderView(View view) {
            ButterKnife.bind(this, view);
        }

        public void bind(Context context, TimeWithUser history) {
            openDate.setText(TimeTools.toDayString(history.time));
        }
    }

    class HistoryAdapter extends BaseAdapter implements Filterable {
        List<TimeWithUser> history = new ArrayList<>();
        List<TimeWithUser> orgs = new ArrayList<>();

        final TimeWithUser nullValue = new TimeWithUser();
        UserFitler fitler = new UserFitler();

        public HistoryAdapter(Context context) {
            nullValue.name = "";
            reInit();
        }
        boolean index(TimeWithUser h,CharSequence filter){
            return h.name.contains(filter)?true:TimeTools.rawString(h.time).contains(filter);
        }
        void filterInit(CharSequence filter) {
            history.clear();
            if (orgs.size() == 0) {
                notifyDataSetChanged();
                return;
            }
            TimeWithUser pre = orgs.get(0);
            boolean first = false;
            for (TimeWithUser h : orgs) {
                if (filter == null ? true : index(h,filter)) {
                    if (first == false) {
                        history.add(nullValue);
                        first = true;
                    }
                    if (h.time / 1000000 != pre.time / 1000000) {
                        if (history.size() != 1)
                            history.add(nullValue);
                    }
                    history.add(h);
                    pre = h;
                }
            }

        }

        void reInit() {
            orgs = DbHelper.getHistory();

        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int position) {
            return getItem(position) == nullValue ? 1 : 0;
        }

        @Override
        public int getCount() {
            return history.size();
        }

        @Override
        public Object getItem(int position) {
            return history.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewBase holder = null;
            if (convertView == null) {
                View view;
                if (getItemViewType(position) == 0) {
                    view = getLayoutInflater(null).inflate(R.layout.item_history_node, null);
                    holder = new NodeView(view);
                } else {
                    view = getLayoutInflater(null).inflate(R.layout.item_history_header, null);
                    holder = new HeaderView(view);
                }
                view.setTag(holder);
                convertView = view;
            } else {
                holder = (ViewBase) convertView.getTag();
            }
            TimeWithUser bind = history.get(position);
            if (bind == nullValue) {
                bind = history.get(position + 1);
            }
            holder.bind(getContext(), bind);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return fitler;
        }

        class UserFitler extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                HistoryAdapter.this.filterInit(constraint);
                FilterResults results = new FilterResults();
                results.values = HistoryAdapter.this.history;
                results.count = HistoryAdapter.this.history.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetChanged();
                }
            }
        }
    }

}
