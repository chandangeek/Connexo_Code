/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPath;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CommunicationPath} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (15:21)
 */
public class G3CommunicationPathImpl extends CommunicationPathImpl implements G3CommunicationPath {

    private final List<G3CommunicationPathSegment> segments;

    G3CommunicationPathImpl(Device source, Device target) {
        this(source, target, Collections.emptyList());
    }

    G3CommunicationPathImpl(Device source, Device target, List<G3CommunicationPathSegment> segments) {
        super(source, target);
        this.segments = new ArrayList<>(segments);
    }

    void addSegment(G3CommunicationPathSegment segment) {
        this.segments.add(segment);
    }

    @Override
    public int getNumberOfHops() {
        return Math.max(0, this.segments.size() - 1);
    }

    @Override
    public List<Device> getIntermediateDevices() {
        return this.segments
                .stream()
                .map(G3CommunicationPathSegment::getNextHopDevice)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

}