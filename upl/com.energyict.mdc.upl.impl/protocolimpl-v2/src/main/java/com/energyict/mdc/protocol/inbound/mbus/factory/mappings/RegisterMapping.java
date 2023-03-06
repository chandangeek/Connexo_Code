/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.factory.mappings;

import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.VIFUnitMultiplierMasks;
import com.energyict.obis.ObisCode;

import java.util.Arrays;
import java.util.Optional;

public enum RegisterMapping {
    INSTANTANEOUS_VOLUME(   0x04, 0x13, -1,
                            TelegramFunctionType.INSTANTANEOUS_VALUE,
                            TelegramEncoding.ENCODING_INTEGER,
                            VIFUnitMultiplierMasks.VOLUME,
                    "8.0.1.0.0.255"), //Bulk Billing water volume (m³)

    MIN_FLOW(0x22, 0x3b, -1,
            TelegramFunctionType.MINIMUM_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            VIFUnitMultiplierMasks.VOLUME_FLOW,
            "8.0.2.0.0.255"),   // Water flow (km³/h)

    MAX_FLOW(0x12, 0x3b, -1,
            TelegramFunctionType.MAXIMUM_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            VIFUnitMultiplierMasks.VOLUME_FLOW,
            "8.0.3.0.0.255"),

    BACK_FLOW(0x04, 0x93, 0x3c,
            TelegramFunctionType.INSTANTANEOUS_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            VIFUnitMultiplierMasks.VOLUME,
            "8.0.4.0.0.255"),

    BACK_FLOW_WEEKLY(0x04, 0x93, 0xc3,
              TelegramFunctionType.INSTANTANEOUS_VALUE,
              TelegramEncoding.ENCODING_INTEGER,
              VIFUnitMultiplierMasks.VOLUME,
            "8.0.5.0.0.255"),

    BATTERY_LIFETIME(0x02, 0xfd, 0,
            TelegramFunctionType.INSTANTANEOUS_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            null,
            "0.0.96.6.6.255");


    private final TelegramFunctionType functionType;
    private final TelegramEncoding encoding;
    private final VIFUnitMultiplierMasks unit;
    private final ObisCode obisCode;
    private final byte dif;
    private final byte vif;
    private final byte vife;

    RegisterMapping(int dif, int vif, int vife, TelegramFunctionType functionType, TelegramEncoding encoding, VIFUnitMultiplierMasks unit, String obisCode) {
        this.dif = (byte)dif;
        this.vif = (byte)vif;
        this.vife = (byte)vife;
        this.functionType = functionType;
        this.encoding = encoding;
        this.unit = unit;
        this.obisCode = ObisCode.fromString(obisCode);
    }

    public byte getDif() {
        return dif;
    }

    public byte getVif() {
        return vif;
    }

    public byte getVife() {
        return vife;
    }

    public TelegramFunctionType getFunctionType() {
        return functionType;
    }

    public TelegramEncoding getEncoding() {
        return encoding;
    }

    public VIFUnitMultiplierMasks getUnit() {
        return unit;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public static Optional<RegisterMapping> getFor(TelegramVariableDataRecord record) {
        if (record.getDif() == null || record.getVif() == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(v -> v.getEncoding().equals(record.getDif().getDataFieldEncoding()))
                .filter(v -> v.getFunctionType().equals(record.getDif().getFunctionType()))
                .filter(v -> {
                                if (v.getUnit() == null) {
                                    return true; // not needed
                                }
                                if (record.getVif().getType() != null) {
                                    return v.getUnit().equals(record.getVif().getType());
                                } else {
                                    return true;
                                }
                            })
                .filter(v -> v.getDif() == record.getDif().getFieldAsByteArray()[0])
                .filter(v -> v.getVif() == record.getVif().getFieldAsByteArray()[0])
                .filter(v -> {
                    // only test VIF-Extensions is requested
                    if (v.getVife() > 0) {
                        if (record.getVifes() != null && record.getVifes().size() > 0) {
                            return v.getVife() == record.getVifes().get(0).getFieldAsByteArray()[0];
                        } else {
                            return false; // we need VIFex but not available in record
                        }
                    } else {
                        return true;
                    }
                })
                .findFirst();
    }
}
