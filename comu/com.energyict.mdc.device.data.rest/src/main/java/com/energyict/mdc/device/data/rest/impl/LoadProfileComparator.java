/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.LoadProfile;

import java.util.Comparator;

/**
 * Created by bvn on 8/7/14.
 */
public class LoadProfileComparator implements Comparator<LoadProfile> {

    @Override
    public int compare(LoadProfile o1, LoadProfile o2) {
        return o1.getLoadProfileSpec().getLoadProfileType().getName().compareToIgnoreCase(o2.getLoadProfileSpec().getLoadProfileType().getName());
    }
}
