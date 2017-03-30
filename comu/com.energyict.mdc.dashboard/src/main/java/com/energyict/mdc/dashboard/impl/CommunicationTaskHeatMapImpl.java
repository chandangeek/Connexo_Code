/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ConnectionTaskDeviceTypeHeatMap} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:20)
 */
class CommunicationTaskHeatMapImpl implements CommunicationTaskHeatMap {

    private final List<CommunicationTaskHeatMapRow> rows = new ArrayList<>();

    CommunicationTaskHeatMapImpl() {
        super();
    }

    CommunicationTaskHeatMapImpl(Map<DeviceType, List<Long>> rawData) {
        this();
        for (DeviceType deviceType : rawData.keySet()) {
            List<Long> counters = rawData.get(deviceType);
            CommunicationTaskHeatMapRowImpl heatMapRow = new CommunicationTaskHeatMapRowImpl(deviceType);
            heatMapRow.add(this.newComCommandCompletionCodeOverview(counters));
            this.add(heatMapRow);
        }
    }

    private ComCommandCompletionCodeOverview newComCommandCompletionCodeOverview(List<Long> counters) {
        Iterator<Long> completionCodeValues = counters.iterator();
        ComCommandCompletionCodeOverviewImpl overview = new ComCommandCompletionCodeOverviewImpl();
        for (CompletionCode completionCode : CompletionCode.values()) {
            overview.add(new CounterImpl<>(completionCode, completionCodeValues.next()));
        }
        return overview;
    }

    @Override
    public Iterator<CommunicationTaskHeatMapRow> iterator() {
        return Collections.unmodifiableList(this.rows).iterator();
    }

    public void add(CommunicationTaskHeatMapRow row) {
        this.rows.add(row);
    }

    public ComCommandCompletionCodeOverviewImpl getOverview() {
        Map<CompletionCode, Long> completionCodeCount = this.completionCodeMapWithAllZeros();
        for (CommunicationTaskHeatMapRow row : this.rows) {
            this.prepareOverview(completionCodeCount, row);
        }
        ComCommandCompletionCodeOverviewImpl result = new ComCommandCompletionCodeOverviewImpl();
        Stream
                .of(CompletionCode.values())
                .map(completionCode -> new CounterImpl<>(completionCode, completionCodeCount.get(completionCode)))
                .forEach(result::add);
        return result;
    }

    private Map<CompletionCode, Long> completionCodeMapWithAllZeros() {
        return Stream
                .of(CompletionCode.values())
                .collect(Collectors.toMap(
                        Function.identity(),
                        completionCode -> 0L));
    }

    private void prepareOverview(Map<CompletionCode, Long> completionCodeCount, CommunicationTaskHeatMapRow row) {
        for (ComCommandCompletionCodeOverview counters : row) {
            this.prepareOverview(completionCodeCount, counters);
        }
    }

    private void prepareOverview(Map<CompletionCode, Long> completionCodeCount, ComCommandCompletionCodeOverview counters) {
        for (Counter<CompletionCode> counter : counters) {
            completionCodeCount.merge(
                    counter.getCountTarget(),
                    counter.getCount(),
                    (l1, l2) -> l1 + l2);
        }
    }

}