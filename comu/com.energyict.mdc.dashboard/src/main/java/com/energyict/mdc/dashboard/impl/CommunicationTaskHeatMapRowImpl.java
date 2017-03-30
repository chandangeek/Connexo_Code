/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMapRow;
import com.energyict.mdc.device.config.DeviceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link CommunicationTaskHeatMapRow} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (15:09)
 */
public class CommunicationTaskHeatMapRowImpl implements CommunicationTaskHeatMapRow {

    private final DeviceType target;
    private final List<ComCommandCompletionCodeOverview> overviews = new ArrayList<>();

    CommunicationTaskHeatMapRowImpl(DeviceType target) {
        super();
        this.target = target;
    }

    public CommunicationTaskHeatMapRowImpl(DeviceType target, ComCommandCompletionCodeOverview... overviews) {
        this(target);
        this.overviews.addAll(Arrays.asList(overviews));
    }

    public void add(ComCommandCompletionCodeOverview overview) {
        this.overviews.add(overview);
    }

    @Override
    public Iterator<ComCommandCompletionCodeOverview> iterator() {
        return Collections.unmodifiableList(this.overviews).iterator();
    }

    @Override
    public DeviceType getTarget() {
        return this.target;
    }

}