package com.energyict.mdc.protocol.inbound.mbus.factory.mappings;

import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.VIF_Unit_Multiplier_Masks;
import com.energyict.obis.ObisCode;

import java.util.Arrays;
import java.util.Optional;

public enum RegisterMapping {
    INSTANTANEOUS_VOLUME(TelegramFunctionType.INSTANTANEOUS_VALUE,
                            TelegramEncoding.ENCODING_INTEGER,
                            VIF_Unit_Multiplier_Masks.VOLUME,
                    "8.0.1.0.0.255"), //Bulk Billing water volume (m³)

    MIN_FLOW(TelegramFunctionType.MINIMUM_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            VIF_Unit_Multiplier_Masks.VOLUME_FLOW,
            "8.0.2.0.0.255"),   // Water flow (km³/h)

    MAX_FLOW(TelegramFunctionType.MAXIMUM_VALUE,
            TelegramEncoding.ENCODING_INTEGER,
            VIF_Unit_Multiplier_Masks.VOLUME_FLOW,
            "8.0.3.0.0.255");

    private final TelegramFunctionType functionType;
    private final TelegramEncoding encoding;
    private final VIF_Unit_Multiplier_Masks unit;
    private final ObisCode obisCode;

    RegisterMapping(TelegramFunctionType functionType, TelegramEncoding encoding, VIF_Unit_Multiplier_Masks unit, String obisCode) {
        this.functionType = functionType;
        this.encoding = encoding;
        this.unit = unit;
        this.obisCode = ObisCode.fromString(obisCode);
    }

    public TelegramFunctionType getFunctionType() {
        return functionType;
    }

    public TelegramEncoding getEncoding() {
        return encoding;
    }

    public VIF_Unit_Multiplier_Masks getUnit() {
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
                .filter(v -> v.getUnit().equals(record.getVif().getType()))
                .findFirst();
    }
}
