/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

import java.util.Arrays;

/**
 * A NotSupported field contains only characters 0xFF, which indicate the field is not supported by the device
 *
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class NotSupportedData extends AbstractField<NotSupportedData> {

    private static final byte NOT_SUPPORTED_BYTE_VALUE = (byte) 0xFF;

    private final int length;

    public NotSupportedData(int length) {
        this.length = length;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, NOT_SUPPORTED_BYTE_VALUE);
        return bytes;
    }

    @Override
    public NotSupportedData parse(byte[] rawData, int offset) throws ParsingException {
        return this;
    }

    @Override
    public int getLength() {
        return length;
    }
}