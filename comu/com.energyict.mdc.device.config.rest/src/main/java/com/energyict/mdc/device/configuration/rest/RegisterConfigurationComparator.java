package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.device.config.RegisterSpec;

import java.util.Comparator;

public class RegisterConfigurationComparator implements Comparator<RegisterSpec> {

    @Override
    public int compare(RegisterSpec o1, RegisterSpec o2) {
        return o1.getRegisterType().getReadingType().getAliasName().compareToIgnoreCase(o2.getRegisterType().getReadingType().getAliasName());
    }
}