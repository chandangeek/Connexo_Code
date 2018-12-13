/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ConnectionTaskHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionTaskHeatMap} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:17)
 */
abstract class ConnectionTaskHeatMapImpl<T> implements ConnectionTaskHeatMap<T> {

    private final List<ConnectionTaskHeatMapRow<T>> rows = new ArrayList<>();

    @Override
    public Iterator<ConnectionTaskHeatMapRow<T>> iterator() {
        return Collections.unmodifiableList(this.rows).iterator();
    }

    public void add(ConnectionTaskHeatMapRow<T> row) {
        this.rows.add(row);
    }

}