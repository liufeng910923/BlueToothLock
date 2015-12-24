package com.lncosie.ilandroidos.model;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.lncosie.ilandroidos.bus.Bus;
import com.lncosie.ilandroidos.bus.ErrorPassword;
import com.lncosie.ilandroidos.db.ConnectedLocks;
import com.lncosie.ilandroidos.db.LockUsers;
import com.lncosie.ilandroidos.db.Settings;
import com.lncosie.ilandroidos.db.TimeWithUser;
import com.lncosie.ilandroidos.db.UserDetail;
import com.lncosie.ilandroidos.db.UserWithTime;
import com.lncosie.ilandroidos.db.Users;

import java.util.List;

/**
 * Created by Administrator on 2015/11/11.singles day
 */
public class DbHelper {
    static final String dropUserWithTime = "DROP Table IF EXISTS UserWithTime ;\n";
    static final String dropTimeWithUser = "DROP Table IF EXISTS TimeWithUser;\n";
    static final String dropLockUser = "DROP Table IF EXISTS LockUsers;\n";

    static final String createLockUser = "CREATE VIEW LockUsers AS SELECT * FROM UserDetail WHERE gid IN\n" +
            "( SELECT id FROM users WHERE mac= ( SELECT mac FROM Settings ))";
    static final String createTimeWithUser =
            "CREATE VIEW if not EXISTS TimeWithUser AS SELECT History.*,Users.NAME,Users.IMAGE FROM History LEFT JOIN " +
                    "Users ON History.gid = Users.id WHERE users.MAC=(SELECT mac FROM Settings LIMIT 1) ORDER BY History.time DESC";

    static final String createUserWithTime = "CREATE VIEW if not EXISTS UserWithTime  AS SELECT * FROM " +
            "(SELECT * from users where users.mac==(SELECT mac from settings LIMIT 1)) AS LU" +
            " LEFT JOIN \n" +
            "(SELECT gid,\n" +
            "sum(CASE WHEN type=0 OR type=3 THEN 1 ELSE 0 END )AS Pc,\n" +
            "sum(CASE WHEN type=1 OR type=4 THEN 1 ELSE 0 END )AS Fc,\n" +
            "sum(CASE WHEN type=2 THEN 1 ELSE 0 END )AS Rc\n" +
            "FROM userdetail GROUP BY gid\n" +
            ")AS DT ON LU.id=DT.GID";
    static final String eraseCurDetail = "delete from userdetail\n" +
            "where gid in\n" +
            "(select gid from users where \n" +
            "mac=(select mac from settings))";
    static final String eraseCurUser = "delete from users\n" +
            "where mac=(select mac from settings)\n";
    static final String eraseCurHistory = "delete from history\n" +
            "where gid in\n" +
            "(select id from users where \n" +
            "mac=(select mac from settings))";
    static final String updateCurHistory = "UPDATE History SET GID = ( SELECT UserDetail.GID FROM UserDetail, Users WHERE UserDetail.UID = History.UID AND UserDetail.TYPE = History.TYPE AND Users.id = UserDetail.GID AND Users.mac = (SELECT mac FROM settings LIMIT 1)) WHERE gid=0";
    static final String optimized = "CREATE INDEX IF NOT EXISTS idxUserMac ON Users (MAC ASC)";

    public static void DbInit() {

        execSQLNoThrow(dropTimeWithUser);
        execSQLNoThrow(dropUserWithTime);
        execSQLNoThrow(dropLockUser);
        execSQLNoThrow(createUserWithTime);
        execSQLNoThrow(createTimeWithUser);
        execSQLNoThrow(createLockUser);
        execSQLNoThrow(optimized);
    }

    public static void syncDbHistory() {
        execSQLNoThrow(updateCurHistory);
    }

    public static void eraseUsers() {
        execSQLNoThrow(eraseCurDetail);
        execSQLNoThrow(eraseCurUser);
    }

    public static void eraseHistory() {
        execSQLNoThrow(eraseCurHistory);
    }

    public static String getCurMac() {
        Settings setting = new Select().from(Settings.class).executeSingle();
        return setting==null?null:setting.mac;
    }

    public static void setCurMac(String mac) {
        Settings setting = new Select().from(Settings.class).executeSingle();
        if (setting == null) {
            setting = new Settings();
        }
        setting.mac = mac;
        setting.save();
    }

    public static List<ConnectedLocks> getLocks() {
        return new Select().from(ConnectedLocks.class).execute();
    }

    public static boolean insertLock(ConnectedLocks lock) {
        lock.save();
        return true;
    }

    public static boolean checkPassword(String pwd) {
        String mac=getCurMac();
        if(mac==null)
            return false;
        ConnectedLocks lock = new Select().from(ConnectedLocks.class).where("mac=?",mac).executeSingle();
        if (lock != null) {
            if (lock.password.equals(pwd))
                return true;
        }
        return false;
    }

    public static byte[] getPassword(String mac) {
        ConnectedLocks lock = new Select().from(ConnectedLocks.class).where("mac=?", mac).executeSingle();
        return StringTools.getPwdBytes(lock != null ? lock.password : null);
    }

    public static void setPassword(String mac, String password) {
        ConnectedLocks lock = new Select().from(ConnectedLocks.class).where("mac=?", mac).executeSingle();
        if (lock != null) {
            lock.password = password;
            lock.save();
        } else {
            lock = new ConnectedLocks();
            lock.password = password;
            lock.mac = mac;
            lock.name = mac;
            lock.save();

        }
    }

    public static Users getUser(long gid) {
        return Users.load(Users.class, gid);
    }

    public static List<UserWithTime> getUsers() {
        return new Select().from(UserWithTime.class).execute();
    }
    public static List<Users> getRawUsers() {
        return new Select().from(Users.class).where("mac=?",getCurMac()).execute();
    }
    public static List<UserDetail> getUserDetails(long id) {
        return new Select().from(UserDetail.class).where("gid=?", id).execute();
    }

    public static List<TimeWithUser> getHistory() {
        return new Select().from(TimeWithUser.class).execute();
    }

    public static List<LockUsers> getCurLockUsers(int type) {
        return new Select().from(LockUsers.class).where("type=?", type).execute();
    }

    public static void deleteUser(long id) {
        Users user = Users.load(Users.class, id);
        if(user!=null)
            user.delete();
    }

    public static void deleteAuth(LockUsers id) {
        new Delete().from(UserDetail.class).where(
                "gid=? and uid=? and type=?", id.gid, id.uid, id.type
        ).execute();
    }

    public static String getLanguage() {
        Settings setting = new Select().from(Settings.class).executeSingle();
        return setting.language;
    }

    public static void setLanguage(String language) {
        Settings setting = new Select().from(Settings.class).executeSingle();
        if (setting == null) {
            setting = new Settings();
        }
        setting.language = language;
        setting.save();
    }

    protected static void execSQLNoThrow(String sql) {
        try {
            ActiveAndroid.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
