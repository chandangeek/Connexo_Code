package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.Comparator;

public class RegisterTypeComparator implements Comparator<RegisterType> {

    @Override
    public int compare(RegisterType o1, RegisterType o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}