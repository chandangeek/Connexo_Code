/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTaskHeatMapRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ConnectionTaskHeatMapRow} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:11)
 */
public class ConnectionTaskHeatMapRowImpl<T> implements ConnectionTaskHeatMapRow<T> {

    private final T target;
    private final List<ComSessionSuccessIndicatorOverview> overviews = new ArrayList<>();

    ConnectionTaskHeatMapRowImpl(T target) {
        super();
        this.target = target;
    }

    public ConnectionTaskHeatMapRowImpl(T target, ComSessionSuccessIndicatorOverview... overviews) {
        this(target);
        this.overviews.addAll(Arrays.asList(overviews));
    }

    public void add(ComSessionSuccessIndicatorOverview overview) {
        this.overviews.add(overview);
    }

    @Override
    public Iterator<ComSessionSuccessIndicatorOverview> iterator() {
        return Collections.unmodifiableList(this.overviews).iterator();
    }

    @Override
    public T getTarget() {
        return this.target;
    }

    @Override
    public long getTotalCount() {
        return Stream.of(this).
                map(ConnectionTaskHeatMapRow::getTotalCount).
                reduce((runningSum, value) -> runningSum + value).orElse(0L);
    }

}