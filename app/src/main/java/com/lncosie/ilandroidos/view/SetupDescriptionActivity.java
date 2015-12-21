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

public class SetupDescriptionActivity extends EventableActivity {

    @Bind(R.id.description)
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_description);
        ButterKnife.bind(this);
        String name=getIntent().getStringExtra("description");
        description.setText(name);

    }

    public void onClick(View v) {
        if (v.getId() == R.id.save) {
            String desc = description.getText().toString();
            if(desc.length()==0){
                Bus.post(new TipOperation(-1,R.string.name_must_set));
                description.requestFocus();
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("description", desc);
            setResult(RESULT_FIRST_USER, intent);
            finish();
        }
    }
}
