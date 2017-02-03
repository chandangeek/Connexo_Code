package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class SchedulingInterval extends AbstractField<SchedulingInterval> {

    public static final int LENGTH = 4;

    private int schedulingInterval;

    public SchedulingInterval() {
        this.schedulingInterval = 0;
    }

    public SchedulingInterval(int schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(schedulingInterval, LENGTH);
    }

    @Override
    public SchedulingInterval parse(byte[] rawData, int offset) throws ParsingException {
        schedulingInterval = getIntFromBytes(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getSchedulingInterval() {
        return schedulingInterval;
    }

    @Override
    public String toString() {
        return String.valueOf(schedulingInterval);
    }
}