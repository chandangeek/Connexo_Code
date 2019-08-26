/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.tasks.ComTask;

import java.util.Comparator;

public class ComTaskComparator implements Comparator<ComTask> {

    @Override
    public int compare(ComTask o1, ComTask o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}