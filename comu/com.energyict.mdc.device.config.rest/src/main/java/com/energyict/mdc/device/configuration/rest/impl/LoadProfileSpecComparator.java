package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.LoadProfileSpec;

import java.util.Comparator;

public class LoadProfileSpecComparator implements Comparator<LoadProfileSpec> {

    @Override
    public int compare(LoadProfileSpec o1, LoadProfileSpec o2) {
        return o1.getLoadProfileType().getName().compareToIgnoreCase(o2.getLoadProfileType().getName());
    }
}