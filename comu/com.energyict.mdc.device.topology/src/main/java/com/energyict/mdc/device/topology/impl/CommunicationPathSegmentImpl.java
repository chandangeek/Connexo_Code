/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.CommunicationPathSegment;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Map;

/**
 * Serves as the root for all components that intend
 * to implement the {@link CommunicationPathSegment} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:36)
 */
public abstract class CommunicationPathSegmentImpl implements CommunicationPathSegment {

    protected static final String G3_DISCRIMINATOR = "0";
    protected static final String SIMPLE_DISCRIMINATOR = "1";

    public enum Field implements ImplField {
        CREATION_TIME("interval.start"),
        SOURCE("source"),
        TARGET("target"),
        NEXT_HOP("nextHop"),
        INTERVAL("interval"),
        TIME_TO_LIVE("timeToLive"),
        COST("cost"),
        ;

        private final String javaFieldName;

        private Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }
    }

    public static final Map<String, Class<? extends CommunicationPathSegment>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends CommunicationPathSegment>>of(
                    G3_DISCRIMINATOR, G3CommunicationPathSegmentImpl.class,
                    SIMPLE_DISCRIMINATOR, SimpleCommunicationPathSegmentImpl.class);

    @IsPresent
    private Reference<Device> source = ValueReference.absent();
    @IsPresent
    private Reference<Device> target = ValueReference.absent();
    private Interval interval;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    protected void init(Device source, Device target, Interval interval) {
        this.source.set(source);
        this.target.set(target);
        this.interval = interval;
    }

    @Override
    public Device getSource() {
        return this.source.get();
    }

    @Override
    public Device getTarget() {
        return this.target.get();
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

}