/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 14:03
 */
public class Data<T extends Data> extends AbstractField<T> {

    private final int length;
    private TimeZone timeZone;
    private byte[] frameBytes;

    public Data(int length, TimeZone timeZone) {
        this.length = length;
        this.frameBytes = new byte[length];
        this.timeZone = timeZone;
    }

    @Override
    public byte[] getBytes() {
        return frameBytes;
    }

    @Override
    public T parse(byte[] rawData, int offset) throws ParsingException {
        this.frameBytes = ProtocolTools.getSubArray(rawData, offset, offset + getLength());
        return (T) this;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}