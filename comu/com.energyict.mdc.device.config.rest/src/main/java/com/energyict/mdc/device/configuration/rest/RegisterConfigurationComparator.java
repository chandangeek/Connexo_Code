/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.device.config.RegisterSpec;

import java.util.Comparator;

public class RegisterConfigurationComparator implements Comparator<RegisterSpec> {

    @Override
    public int compare(RegisterSpec o1, RegisterSpec o2) {
        return o1.getRegisterType().getReadingType().getAliasName().compareToIgnoreCase(o2.getRegisterType().getReadingType().getAliasName());
    }
}