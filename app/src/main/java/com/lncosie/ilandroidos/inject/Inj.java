package com.lncosie.ilandroidos.inject;


import org.codejargon.feather.Feather;

public class Inj {
    private static Feather feather = Feather.with();

    public static Feather get() {
        return feather;
    }

}
