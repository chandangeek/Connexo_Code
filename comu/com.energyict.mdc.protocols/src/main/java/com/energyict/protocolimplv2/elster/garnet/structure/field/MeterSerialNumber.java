/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.SerialNumberCompacter;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 4/06/2014 - 15:04
 */
public class MeterSerialNumber extends AbstractField<MeterSerialNumber> {

    public static final int LENGTH = 8;

    private String serialNumber;

    public MeterSerialNumber() {
        this.serialNumber = "";
    }

    public MeterSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public byte[] getBytes() {
        return packSerialNumber(getSerialNumber());
    }

    @Override
    public MeterSerialNumber parse(byte[] rawData, int offset) throws ParsingException {
        byte[] encodedSerialNumber = ProtocolTools.getSubArray(rawData, offset, offset + LENGTH);
        serialNumber = SerialNumberCompacter.unPackSerialNumber(encodedSerialNumber);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    private byte[] packSerialNumber(String serialNumber) {
        return SerialNumberCompacter.packSerialNumber(serialNumber);
    }
}