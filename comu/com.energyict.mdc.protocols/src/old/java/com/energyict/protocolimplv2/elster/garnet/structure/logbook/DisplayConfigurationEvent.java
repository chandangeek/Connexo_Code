/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class DisplayConfigurationEvent extends AbstractField<DisplayConfigurationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;

    private MeterSerialNumber displayId;
    private MeterSerialNumber meterSerialNumber;

    public DisplayConfigurationEvent() {
        this.displayId = new MeterSerialNumber();
        this.meterSerialNumber = new MeterSerialNumber();
    }

    public DisplayConfigurationEvent(MeterSerialNumber meterSerialNumber, MeterSerialNumber displayId) {
        this.meterSerialNumber = meterSerialNumber;
        this.displayId = displayId;
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                displayId.getBytes(),
                meterSerialNumber.getBytes()
        );
    }

    @Override
    public DisplayConfigurationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.displayId.parse(rawData, ptr);
        ptr += displayId.getLength();

        this.meterSerialNumber.parse(rawData, ptr);
        ptr += meterSerialNumber.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return "Configuration of display with ID " + displayId.getSerialNumber() +
                " - Display linked to meter " + meterSerialNumber.getSerialNumber();
    }

    public MeterSerialNumber getDisplayId() {
        return displayId;
    }

    public MeterSerialNumber getMeterSerialNumber() {
        return meterSerialNumber;
    }
}