package com.lncosie.ilandroidos.view;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.bluenet.ByteableTask;
import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.Task;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.NetworkError;
import com.lncosie.ilandroidos.bus.OperatorMessages;
import com.lncosie.ilandroidos.bus.TipOperation;
import com.lncosie.ilandroidos.bus.UsersChanged;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.Users;
import com.lncosie.ilandroidos.model.BitmapTool;
import com.lncosie.ilandroidos.model.DbHelper;
import com.lncosie.ilandroidos.model.InterlockOperation;
import com.lncosie.ilandroidos.model.StringTools;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserAddActivity extends EventableActivity {

    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.auth_add_pwd_page)
    LinearLayout authAddPwdPage;
    @Bind(R.id.auth_add_finger_page)
    LinearLayout authAddFingerPage;
    @Bind(R.id.animate_frame)
    FrameLayout animate_frame;
    @Bind(R.id.password_re)
    EditText passwordRe;
    @Bind(R.id.user_add_finger_animate)
    View userAddFingerAnimate;

    Integer authAddSelect = 0;

    @Bind(R.id.id_of_pwd)
    TextView idOfPwd;
    boolean isAdd = false;


    @Bind(R.id.user_add_ok)
    TextView userAddOk;
    @Bind(R.id.user_image)
    de.hdodenhof.circleimageview.CircleImageView userImage;
    Users user = null;
    OperatorMessages.OpAddAuth idLocked = null;
    @Bind(R.id.user_name_view_frame)
    LinearLayout user_name_view_frame;
    @Bind(R.id.user_name_edit_frame)
    LinearLayout user_name_edit_frame;
    @Bind(R.id.default_add_pwd)
    RadioButton add_radio;

    @Bind(R.id.user_name_edit)
    EditText user_name_edit;
    @Bind(R.id.user_name_view)
    TextView user_name_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);
        ButterKnife.bind(this);
        Bus.register(this);
        init();
    }

    @Override
    public void onDestroy() {
        Bus.unregister(this);
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
    }

    @OnClick(R.id.user_image)
    void user_pick_image(View v) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0)
            return;
        if (checkAddUser() == false)
            return;
        if (requestCode == 3) {
            String img[] = new String[1];
            userImage.setImageBitmap(BitmapTool.cropBitmap(this, data.getData(), img));
            user.image = img[0];
            user.save();
        }
    }

    void init() {
        authAddFingerPage.setVisibility(View.GONE);
        long uid = getIntent().getLongExtra("uid", -1);
        boolean edit = getIntent().getBooleanExtra("edit", false);
        if (edit) {
            user_name_edit_frame.setVisibility(View.VISIBLE);
            user_name_view_frame.setVisibility(View.GONE);
        } else {
            user_name_edit_frame.setVisibility(View.GONE);
            user_name_view_frame.setVisibility(View.VISIBLE);
        }

        if (uid != -1) {
            user = DbHelper.getUser(uid);
            user_name_edit.setText(user.name);
            user_name_view.setText(user.name);
            userImage.setImageBitmap(BitmapTool.decodeBitmap(this, user.image));
        }

        clickAddAuth(add_radio);
        add_radio.setChecked(true);
        HideWhenTouchOutside.setupUI(this);
        //userAddOk.requestFocus();
    }

    @Subscribe
    public void addFromLock(OperatorMessages.OpAddAuth result) {
        if (result.error != 0) {
            idOfPwd.setText(R.string.user_full);
            idLocked = null;

            return;
        }
        idLocked = result;
        switch (result.cmd) {
            case ByteableTask.CMD_ADD_ADMIN_PWD:
            case ByteableTask.CMD_ADD_USER_PWD:
                idOfPwd.setText(String.format("%02d", idLocked.uid));
                break;
            case ByteableTask.CMD_ADD_ADMIN_FINGER:
            case ByteableTask.CMD_ADD_USER_FINGER:
                break;
        }
        if (isAdd) {
            user.save();
            UserDetail detail = new UserDetail();
            detail.gid = user.getId();
            detail.type = idLocked.type;
            detail.uid = idLocked.uid;
            detail.save();
            gotoViewPage();
        }

    }

    public void backward(View v) {
        Bus.post(new UsersChanged());
        super.backward(v);
    }

    @Override
    public void onBackPressed() {
        Bus.post(new UsersChanged());
        super.onBackPressed();
    }

    void gotoViewPage() {
        finish();
        Intent intent = new Intent(this, UserViewDetailActivity.class);
        intent.putExtra("uid", user != null ? user.getId() : -1);
        startActivity(intent);
    }

    boolean checkAddUser() {
        if (user == null) {
            String name = user_name_edit.getText().toString();
            if (name.length() == 0) {
                Bus.post(new TipOperation(-1, R.string.name_must_set));
                user_name_edit.requestFocus();
                return false;
            }
            user = new Users();
            user.name = name;
            user.mac = DbHelper.getCurMac();
            return true;
        }
        return true;
    }

    public void clickAddAuth(View v) {

        isAdd = false;
        authAddSelect = Integer.valueOf((String) v.getTag());
        add_radio.setChecked(false);
        add_radio = (RadioButton) v;
        add_radio.setChecked(true);
        switch (authAddSelect) {
            case 0:
                authAddPwdPage.setVisibility(View.VISIBLE);
                authAddFingerPage.setVisibility(View.GONE);
                Net.get().sendChecked(new InterlockOperation.TaskGetPwdID(Net.get(), false));
                break;
            case 1:
                authAddPwdPage.setVisibility(View.GONE);
                authAddFingerPage.setVisibility(View.VISIBLE);
                idOfPwd.setText(null);
                break;
            case 2:
                authAddPwdPage.setVisibility(View.VISIBLE);
                authAddFingerPage.setVisibility(View.GONE);
                Net.get().sendChecked(new InterlockOperation.TaskGetPwdID(Net.get(), true));
                break;
            case 3:
                authAddPwdPage.setVisibility(View.GONE);
                authAddFingerPage.setVisibility(View.VISIBLE);
                idOfPwd.setText(null);
                break;
        }
        if (checkSendable() == false) {
            return;
        }
    }

    @OnClick(R.id.user_add_ok)
    void userAddOk(View v) {
        if (checkSendable() == false) {
            return;
        }
        if (checkAddUser() == false) {
            user_name_edit.requestFocus();
            return;
        }
        if (authAddSelect == 1 || authAddSelect == 3) {
            startAnimate();
        }
        byte[] password = checkPassword();
        if (password == null) {
            return;
        }

        isAdd = true;
        Task task = null;
        Net net = Net.get();
        switch (authAddSelect) {
            case 0:
                task = new InterlockOperation.TaskAddPwd(net, false, password);
                break;
            case 1:
                task = new InterlockOperation.TaskAddFinger(net, false);
                break;
            case 2:
                task = new InterlockOperation.TaskAddPwd(net, true, password);
                break;
            case 3:
                task = new InterlockOperation.TaskAddFinger(net, true);
                break;
        }
        net.sendChecked(task);
    }

    byte[] checkPassword() {
        if (authAddSelect == 1 || authAddSelect == 3)
            return new byte[]{0};
        if (idLocked == null || Net.get().isSendable() == false) {
            Bus.post(new NetworkError());
            return null;
        }
        String p = password.getText().toString();
        String pRe = passwordRe.getText().toString();
        if (p.length() == 0) {
            password.requestFocus();
            Bus.post(new TipOperation(-1, R.string.password_input));
            return null;
        } else if (p.length() < 6) {
            password.requestFocus();
            Bus.post(new TipOperation(-1, R.string.password_error));
            return null;
        } else if (pRe.length() == 0) {
            passwordRe.requestFocus();
            Bus.post(new TipOperation(-1, R.string.password_input));
            return null;
        } else if (pRe.length() < 6) {
            passwordRe.requestFocus();
            Bus.post(new TipOperation(-1, R.string.password_error));
            return null;
        } else if (!p.equals(pRe)) {
            Bus.post(new TipOperation(-1, R.string.password_no_equal));
            return null;
        }
        return StringTools.getPwdBytes(idLocked.uid, p, pRe);
    }

    void startAnimate() {
        int h = animate_frame.getHeight();
        int w = animate_frame.getHeight();
        userAddFingerAnimate.setScaleX(1);
        userAddFingerAnimate.setX(-1);
        userAddFingerAnimate.setVisibility(View.VISIBLE);

        userAddFingerAnimate.animate()//.setInterpolator(new AccelerateDecelerateInterpolator())
                .scaleX(w).setDuration(6000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                userAddFingerAnimate.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }
}
