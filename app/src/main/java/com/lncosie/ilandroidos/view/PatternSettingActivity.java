package com.lncosie.ilandroidos.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.TryLogin;
import com.lncosie.ilandroidos.model.DbHelper;
import com.takwolf.android.lock9.Lock9View;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class PatternSettingActivity extends EventableActivity {

    @Bind(R.id.frame_change_pattern)
    LinearLayout frame_change_pattern;
    @Bind(R.id.switch_motion_pwd)
    Switch switch_motion_pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_setting);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    public void backward(View v){
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            updateUi();
        }
    }

    private void updateUi() {
        boolean has=hasPattern();
        frame_change_pattern.setVisibility(has? View.VISIBLE:View.INVISIBLE);
        switch_motion_pwd.setChecked(has);

    }

    @OnClick(R.id.switch_motion_pwd)
    void frame_motion_pwd(View v){
        if(!hasPattern()){
            Intent intent=new Intent(this,PatternActivity.class);
            intent.putExtra("setup",true);
            startActivityForResult(intent,1);
        }else{
            DbHelper.setPatternPwd(null);
            frame_change_pattern.setVisibility(View.INVISIBLE);
        }
    }
    @OnClick(R.id.frame_change_pattern)
    void frame_change_pattern(View v){
        Intent intent=new Intent(this,PatternActivity.class);
        intent.putExtra("setup",true);
        startActivity(intent);
    }
    boolean hasPattern(){
        String pattern=DbHelper.getPatternPwd();
        if(pattern==null||pattern.length()==0)
            return false;
        return true;
    }
}
