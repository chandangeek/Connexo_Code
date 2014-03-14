package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import java.util.Comparator;

public class RegisterConfigurationComparator implements Comparator<RegisterSpec> {

    @Override
    public int compare(RegisterSpec o1, RegisterSpec o2) {
        return o1.getRegisterMapping().getName().compareToIgnoreCase(o2.getRegisterMapping().getName());
    }
}