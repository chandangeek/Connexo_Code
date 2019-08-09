/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.masterdata.LogBookType;

import java.util.Comparator;

public class LogBookTypeComparator implements Comparator<LogBookType> {

    @Override
    public int compare(LogBookType o1, LogBookType o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}