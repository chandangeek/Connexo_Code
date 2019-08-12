/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.device.config.LoadProfileSpec;

import java.util.Comparator;

public class LoadProfileSpecComparator implements Comparator<LoadProfileSpec> {

    @Override
    public int compare(LoadProfileSpec o1, LoadProfileSpec o2) {
        return o1.getLoadProfileType().getName().compareToIgnoreCase(o2.getLoadProfileType().getName());
    }
}