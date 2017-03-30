/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;

import java.util.Comparator;

public class ComTaskEnablementComparator implements Comparator<ComTaskEnablement> {

    @Override
    public int compare(ComTaskEnablement o1, ComTaskEnablement o2) {
        return o1.getComTask().getName().compareToIgnoreCase(o2.getComTask().getName());
    }
}