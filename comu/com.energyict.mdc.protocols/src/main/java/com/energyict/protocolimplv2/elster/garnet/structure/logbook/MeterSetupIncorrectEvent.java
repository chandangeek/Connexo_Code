/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class MeterSetupIncorrectEvent extends AbstractField<MeterSetupIncorrectEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;

    private MeterSerialNumber meterSerialNumber;
    private PaddingData paddingData;

    public MeterSetupIncorrectEvent() {
        this.meterSerialNumber = new MeterSerialNumber();
        this.paddingData = new PaddingData(8);
    }

    public MeterSetupIncorrectEvent(MeterSerialNumber meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
        this.paddingData = new PaddingData(8);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                meterSerialNumber.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public MeterSetupIncorrectEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.meterSerialNumber.parse(rawData, ptr);
        ptr += meterSerialNumber.getLength();

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return "Incorrect meter setup for meter with serial " + meterSerialNumber.getSerialNumber();
    }

    public MeterSerialNumber getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}