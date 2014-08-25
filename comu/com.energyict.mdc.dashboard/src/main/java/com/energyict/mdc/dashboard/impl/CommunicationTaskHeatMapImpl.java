package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionTaskDeviceTypeHeatMap} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:20)
 */
public class CommunicationTaskHeatMapImpl implements CommunicationTaskHeatMap {

    private final List<CommunicationTaskHeatMapRow> rows = new ArrayList<>();

    @Override
    public Iterator<CommunicationTaskHeatMapRow> iterator() {
        return Collections.unmodifiableList(this.rows).iterator();
    }

    public void add(CommunicationTaskHeatMapRow row) {
        this.rows.add(row);
    }

}