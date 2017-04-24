/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class SensorInAlarmEvent extends AbstractField<SensorInAlarmEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int SENSOR_BYTE_LENGTH = 1;

    private int sensorCode;
    private PaddingData paddingData;

    public SensorInAlarmEvent() {
        this.sensorCode = 0;
        this.paddingData = new PaddingData(15);
    }

    public SensorInAlarmEvent(int sensorCode) {
        this.sensorCode = sensorCode;
        this.paddingData = new PaddingData(15);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getBytesFromInt(sensorCode, SENSOR_BYTE_LENGTH),
                paddingData.getBytes()
        );
    }

    @Override
    public SensorInAlarmEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.sensorCode = getIntFromBytes(rawData, ptr, SENSOR_BYTE_LENGTH);
        ptr += SENSOR_BYTE_LENGTH;

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
        return "Sensor " + sensorCode + " triggered an alarm";
    }

    public int getSensorCode() {
        return sensorCode;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}