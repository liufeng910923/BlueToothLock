package com.lncosie.ilandroidos.utils;

import com.lncosie.ilandroidos.R;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.UserWithTime;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * User: liufeng(549454273@qq.com)
 * Date: 2015-12-15
 * Time: 16:38
 * 设置用户参数的工具类
 */
public class UserTools {

    static UserTools instance;
    UserWithTime user;

    /**
     * @param user
     */
    private UserTools(UserWithTime user) {
        this.user = user;
    }

    public synchronized static UserTools getInstance(UserWithTime user) {
        if (instance == null)
            return new UserTools(user);
        else
            return instance;

    }

    /**
     * @param circleImageView
     */
    public void setUserIcon(CircleImageView circleImageView) {
        /**
         *
         * 显示用户自定义的头像
         */
        String userIconPath = user.image;
        if (userIconPath != null) {
            BitmapUtil.getInstance().setLocalImg(circleImageView, userIconPath);
        } else {
            //显示默认的头像
            circleImageView.setImageResource(R.drawable.stack_of_photos);
        }

    }


    /**
     * @param userIconFlag
     * @param circleImageView
     */
//
//    void setUserIconByFlag(int userIconFlag, CircleImageView circleImageView) {
//        switch (userIconFlag) {
//            case 0:
//                circleImageView.setImageResource(R.drawable.default_user);
//                break;
//            case 1:
//                circleImageView.setImageResource(R.drawable.grandpa);
//                break;
//            case 2:
//                circleImageView.setImageResource(R.drawable.grandma);
//                break;
//            case 3:
//                circleImageView.setImageResource(R.drawable.father);
//                break;
//            case 4:
//                circleImageView.setImageResource(R.drawable.mother);
//                break;
//            case 5:
//                circleImageView.setImageResource(R.drawable.son);
//                break;
//            case 6:
//                circleImageView.setImageResource(R.drawable.daughter);
//                break;
//            case 7:
//                circleImageView.setImageResource(R.drawable.children);
//                break;
//            default:
//                circleImageView.setImageResource(R.drawable.default_user);
//                break;
//        }
//    }

}  