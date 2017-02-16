/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.tasks.ConnectionTask;

public class ConnectionTaskComparator implements java.util.Comparator<ConnectionTask<?,?>> {

    @Override
    public int compare(ConnectionTask<?, ?> o1, ConnectionTask<?, ?> o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}
