package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.LoadProfileType;

import java.util.Comparator;

public class LoadProfileTypeComparator implements Comparator<LoadProfileType> {

    @Override
    public int compare(LoadProfileType o1, LoadProfileType o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}