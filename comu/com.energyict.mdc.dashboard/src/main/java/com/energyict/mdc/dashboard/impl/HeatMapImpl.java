package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.HeatMap;
import com.energyict.mdc.dashboard.HeatMapRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link HeatMap} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:17)
 */
public abstract class HeatMapImpl<T> implements HeatMap<T> {

    private final List<HeatMapRow<T>> rows = new ArrayList<>();

    @Override
    public Iterator<HeatMapRow<T>> iterator() {
        return Collections.unmodifiableList(this.rows).iterator();
    }

    public void add(HeatMapRow<T> row) {
        this.rows.add(row);
    }

}