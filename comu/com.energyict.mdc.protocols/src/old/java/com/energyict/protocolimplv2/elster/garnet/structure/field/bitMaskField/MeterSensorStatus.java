/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterSensorStatus extends AbstractBitMaskField<MeterSensorStatus> {

    public static final int LENGTH = 1; // The length expressed in nr of bits

    private BitSet sensorStatusBitMask;
    private int sensorStatusCode;
    private SensorStatus sensorStatus;

    public MeterSensorStatus() {
        this.sensorStatusBitMask = new BitSet(LENGTH);
        this.sensorStatus = SensorStatus.UNKNOWN;
    }

    public MeterSensorStatus(SensorStatus sensorStatus) {
        this.sensorStatus = sensorStatus;
    }

    public BitSet getBitMask() {
        return sensorStatusBitMask;
    }

    @Override
    public MeterSensorStatus parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        sensorStatusBitMask = bitSet.get(startPos, startPos + LENGTH);
        sensorStatusCode = convertBitSetToInt(sensorStatusBitMask);
        sensorStatus = SensorStatus.fromStatusCode(sensorStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getSensorStatusCode() {
        return sensorStatusCode;
    }

    public String getSensorStatusInfo() {
        if (!this.sensorStatus.equals(SensorStatus.UNKNOWN)) {
            return sensorStatus.getStatusInfo();
        } else {
            return (sensorStatus.getStatusInfo() + " " + sensorStatus);
        }
    }

    private enum SensorStatus {
        NO_VOLTAGE(0, "No voltage"),
        WITH_VOLTAGE(1, "With voltage"),
        UNKNOWN(-1, "Unknown sensor status");

        private final int statusCode;
        private final String statusInfo;

        private SensorStatus(int statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getStatusInfo() {
            return statusInfo;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static SensorStatus fromStatusCode(int statusCode) {
            for (SensorStatus version : SensorStatus.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return SensorStatus.UNKNOWN;
        }
    }
}