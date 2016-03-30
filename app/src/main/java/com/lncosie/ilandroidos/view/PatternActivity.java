package com.lncosie.ilandroidos.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.TryLogin;
import com.lncosie.ilandroidos.model.DbHelper;
import com.takwolf.android.lock9.Lock9View;

public class PatternActivity extends EventableActivity {
    enum State {
        Check, Set, ReSet
    }

    State state = State.Check;
    String pwd;
    boolean isSetup = false;
    TextView todo;
    TextView clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        Lock9View lock9View = (Lock9View) findViewById(R.id.lock_9_view);
        todo = (TextView) findViewById(R.id.todo);
        clear = (TextView) findViewById(R.id.clear);
        TextView title = (TextView) findViewById(R.id.title);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear.setVisibility(View.INVISIBLE);
                state = State.Set;
                todo.setTextColor(getResources().getColor(R.color.black));
                todo.setText(R.string.pattern_set);
            }
        });
        Lock9View.CallBack CALL_BACK = new Lock9View.CallBack() {
            @Override
            public void onFinish(String password) {
                if (!isSetup) {
                    if (password.equals(DbHelper.getPatternPwd())) {
                        Bus.post(new TryLogin());
                        finish();
                    } else {
                        todo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        todo.setText(R.string.pattern_check_error);
                    }
                    return;
                }
                if (state == State.Check) {
                    if (password.equals(DbHelper.getPatternPwd())) {
                        state = State.Set;
                        todo.setTextColor(getResources().getColor(R.color.black));
                        todo.setText(R.string.pattern_set);
                    } else {
                        todo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        todo.setText(R.string.pattern_error);

                    }
                } else if (state == State.Set) {
                    pwd = password;
                    state = State.ReSet;
                    todo.setText(R.string.pattern_reset);
                } else if (state == State.ReSet) {
                    if (password.equals(pwd)) {
                        DbHelper.setPatternPwd(password);
                        todo.setTextColor(getResources().getColor(R.color.black));
                        todo.setText(R.string.pattern_set_ok);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        clear.setVisibility(View.VISIBLE);
                        todo.setTextColor(getResources().getColor(R.color.colorPrimary));
                        todo.setText(R.string.pattern_error);
                    }
                }
            }
        };
        lock9View.setCallBack(CALL_BACK);
        isSetup = getIntent().getBooleanExtra("setup", false);
        if (isSetup) {
            title.setText(R.string.pattern_set_title);
            String dbPwd = DbHelper.getPatternPwd();
            if (dbPwd == null || dbPwd.length() == 0) {
                state = State.Set;
                todo.setTextColor(getResources().getColor(R.color.black));
                todo.setText(R.string.pattern_set);
            } else {
                todo.setTextColor(getResources().getColor(R.color.black));
                todo.setText(R.string.pattern_check);
            }
        } else {
            todo.setTextColor(getResources().getColor(R.color.black));
            todo.setText(R.string.pattern_check);
        }
    }

    public void backward(View v) {
        finish();
    }

}
