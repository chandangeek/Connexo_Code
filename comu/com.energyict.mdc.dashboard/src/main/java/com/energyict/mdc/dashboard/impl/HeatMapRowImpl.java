package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.HeatMapRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link HeatMapRow} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:11)
 */
public class HeatMapRowImpl<T> implements HeatMapRow<T> {

    private final T target;
    private final List<ComSessionSuccessIndicatorOverview> overviews = new ArrayList<>();

    public HeatMapRowImpl(T target) {
        super();
        this.target = target;
    }

    public HeatMapRowImpl(T target, ComSessionSuccessIndicatorOverview... overviews) {
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

}