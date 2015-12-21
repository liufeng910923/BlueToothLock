package com.lncosie.ilandroidos.inject;


import com.squareup.otto.Produce;

import org.codejargon.feather.Feather;

import java.util.LinkedHashMap;
import java.util.Map;

public class Inj{
    private static Feather feather=Feather.with();
    public static Feather get(){
       return feather;
    }

}
