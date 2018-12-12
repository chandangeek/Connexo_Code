/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link StateTimeline} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (08:59)
 */
public class StateTimelineImpl implements StateTimeline {

    private final List<StateTimeSlice> slices;

    public static StateTimelineImpl from(List<StateTimeSlice> slices) {
        return new StateTimelineImpl(slices);
    }

    private StateTimelineImpl (List<StateTimeSlice> slices) {
        super();
        this.slices = new ArrayList<>(slices);
    }

    @Override
    public List<StateTimeSlice> getSlices() {
        return Collections.unmodifiableList(this.slices);
    }

}