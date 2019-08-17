/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.masterdata.LoadProfileType;

import java.util.Comparator;

public class LoadProfileTypeComparator implements Comparator<LoadProfileType> {

    @Override
    public int compare(LoadProfileType o1, LoadProfileType o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}