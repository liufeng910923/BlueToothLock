package com.lncosie.ilandroidos.view;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.TipOperation;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SetupPasswordActivity extends EventableActivity {

    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.password_re)
    EditText passwordRe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_password);
        ButterKnife.bind(this);

    }

    public void onClick(View v) {
        if (v.getId() == R.id.save) {
            if(password.length()<6){
                password.requestFocus();
                Bus.post(new TipOperation(-1, R.string.password_shoter));
                return;
            }else if(passwordRe.length()<6){
                passwordRe.requestFocus();
                Bus.post(new TipOperation(-1, R.string.password_shoter));
                return;
            }else if(!password.getText().toString().equals(passwordRe.getText().toString())){
                Bus.post(new TipOperation(-1,R.string.password_no_equal));
                return;
            }
            Intent intent=new Intent();
            intent.putExtra("password",password.getText().toString());
            setResult(RESULT_FIRST_USER,intent);
            finish();
        }
    }

}
