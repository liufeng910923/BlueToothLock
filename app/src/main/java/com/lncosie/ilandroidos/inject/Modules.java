package com.lncosie.ilandroidos.inject;

import android.support.v4.app.Fragment;

import com.lncosie.ilandroidos.model.Applyable;
import com.lncosie.ilandroidos.view.n.SetupDescriptionFragment;

import org.codejargon.feather.Provides;

import javax.inject.Named;


public class Modules {
    @Named("desc")
    @Provides
    Fragment desc() {
        return new SetupDescriptionFragment();
    }

    @Named("desc")
    @Provides
    Applyable desc_app(@Named("desc") Applyable applyable) {
        return applyable;
    }


}
