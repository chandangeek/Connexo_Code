package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.RegisterMapping;
import java.util.Comparator;

public class RegisterTypeComparator implements Comparator<RegisterMapping> {

    @Override
    public int compare(RegisterMapping o1, RegisterMapping o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}