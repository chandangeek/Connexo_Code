/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.TopologyService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link TopologyService.G3CommunicationPathSegmentBuilder} interface
 * that closely works with the {@link TopologyServiceImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-18 (11:37)
 */
public final class G3CommunicationPathSegmentBuilderImpl implements TopologyService.G3CommunicationPathSegmentBuilder {

    private final TopologyServiceImpl topologyService;
    private final Device source;
    private final Clock clock;
    private final List<G3PathSegmentSpecification> segmentSpecifications = new ArrayList<>();

    public G3CommunicationPathSegmentBuilderImpl(TopologyServiceImpl topologyService, Clock clock, Device source) {
        super();
        this.topologyService = topologyService;
        this.source = source;
        this.clock = clock;
    }

    @Override
    public TopologyService.G3CommunicationPathSegmentBuilder add(Device target, Device intermediateHop, Duration timeToLive, int cost) {
        this.segmentSpecifications.add(new G3PathSegmentSpecification(target, intermediateHop, timeToLive, cost));
        return this;
    }

    @Override
    public List<G3CommunicationPathSegment> complete() {
        Instant now = this.clock.instant();
        return this.segmentSpecifications
                .stream()
                .map(s -> this.createFromSpecification(now, s))
                .collect(Collectors.toList());
    }

    private G3CommunicationPathSegment createFromSpecification(Instant now, G3PathSegmentSpecification specification) {
        return this.topologyService.addCommunicationSegment(now, this.source, specification.target, specification.intermediateHop, specification.timeToLive, specification.cost);
    }

    private class G3PathSegmentSpecification {
        final Device target;
        final Device intermediateHop;
        final Duration timeToLive;
        final int cost;

        private G3PathSegmentSpecification(Device target, Device intermediateHop, Duration timeToLive, int cost) {
            super();
            this.target = target;
            this.intermediateHop = intermediateHop;
            this.timeToLive = timeToLive;
            this.cost = cost;
        }
    }
}