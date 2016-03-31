package com.lncosie.ilandroidos.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name = "Users")
public class Users extends Model implements Parcelable {
    @Column(name = "MAC")
    public String mac;
    @Column(name = "NAME")
    public String name;
    @Column(name = "IMAGE")
    public String image;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mac);
        dest.writeString(this.name);
        dest.writeString(this.image);
    }

    public Users() {
    }

    protected Users(Parcel in) {
        this.mac = in.readString();
        this.name = in.readString();
        this.image = in.readString();
    }

    public static final Parcelable.Creator<Users> CREATOR = new Parcelable.Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel source) {
            return new Users(source);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };
}
