/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_APPLICATIONSTATUS = ObisCode.fromString("0.0.96.5.2.255");
    private static final ObisCode OBISCODE_CURRENT_VALUE = ObisCode.fromString("0.0.67.0.0.255");
    private static final ObisCode OBISCODE_RSSI = ObisCode.fromString("0.0.96.0.63.255");

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    static {
        registerMaps.put(OBISCODE_APPLICATIONSTATUS, "Application status");
        registerMaps.put(OBISCODE_CURRENT_VALUE, "Current sensor value");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");
        registerMaps.put(OBISCODE_RSSI, "RSSI Level");
    }

    private WaveSense waveSense;

    public ObisCodeMapper(final WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    public String getRegisterExtendedLogging() {

        StringBuilder strBuilder = new StringBuilder();

        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveSense.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }

        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        if (info != null) {
            return new RegisterInfo(info);
        } else {
            throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist!");
        }
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        try {
            if (obisCode.equals(OBISCODE_APPLICATIONSTATUS)) {
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(waveSense.getParameterFactory().readApplicationStatus().getStatus()), Unit.get("")), new Date());

                //Firmware version
            } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, waveSense.getFirmwareVersion());

                //Current value
            } else if (obisCode.equals(OBISCODE_CURRENT_VALUE)) {
                double value = waveSense.getRadioCommandFactory().readCurrentValue();
                Unit unit = waveSense.getRadioCommandFactory().readModuleType().getUnit();
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(value), unit));
            } else if (obisCode.equals(OBISCODE_RSSI)) {
                double value = waveSense.getRadioCommandFactory().readRSSI();
                return new RegisterValue(obisCode, new Quantity(value > 100 ? 100 : value, Unit.get("")), new Date());
            }

            throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist!");
        } catch (IOException e) {
            throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] has an error [" + e.getMessage() + "]!");
        }
    }
}