package com.lncosie.ilandroidos.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lncosie.ilandroidos.bluenet.Net;
import com.lncosie.ilandroidos.bluenet.OnNetStateChange;
import com.lncosie.ilandroidos.bus.Bus;

/**
 * Created by Administrator on 2015/11/17.
 */
public class ActiveAbleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bus.register(this);
        return null;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Bus.unregister(this);
    }

    protected void onActive(Object arg) {

    }
    protected boolean checkSendable() {
        return Net.get().isSendable();
    }
    protected void autoConnet(){
        Net net=Net.get();
        if(net.getState()== OnNetStateChange.NetState.Disconnected){
            if(net.getDevice()==null){
                DeviceSelectorFragment fragment = new DeviceSelectorFragment();
                fragment.show(getActivity().getSupportFragmentManager(), "Search");
            }else{
                net.connect();
            }
        }
    }
}
