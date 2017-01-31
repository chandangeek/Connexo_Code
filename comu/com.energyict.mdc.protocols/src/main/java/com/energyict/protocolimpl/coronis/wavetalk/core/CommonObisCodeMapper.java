/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommonObisCodeMapper extends AbstractCommonObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.6.3.255");
    private static final ObisCode OBISCODE_RSSI_LEVEL = ObisCode.fromString("0.0.96.0.63.255");
    private static final ObisCode OBISCODE_BATTERY = ObisCode.fromString("0.0.96.6.0.255");

    static {
        registerMaps.put(OBISCODE_BATTERY, "Available battery power in %");
        registerMaps.put(OBISCODE_APPLICATION_STATUS, "Application status");
        registerMaps.put(OBISCODE_FIRMWARE, "Firmware version");
        registerMaps.put(OBISCODE_RSSI_LEVEL, "RSSI level");
    }

    private AbstractWaveTalk waveTalk;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public CommonObisCodeMapper(final AbstractWaveTalk waveTalk) {
        this.waveTalk = waveTalk;
    }

    final public String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();

        for (Entry<ObisCode, String> o : registerMaps.entrySet()) {
            waveTalk.getLogger().info(o.getKey().toString() + ", " + o.getValue());
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

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException, IOException {
        if (obisCode.equals(OBISCODE_BATTERY)) {
            int battery = waveTalk.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife();
            return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(battery), Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
            int status = waveTalk.getParameterFactory().readApplicationStatus();
            return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(status), Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_RSSI_LEVEL)) {
            double rssiLevel = waveTalk.getRadioCommandFactory().readRSSI();
            return new RegisterValue(obisCode, new Quantity(rssiLevel, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
            String firmwareVerison = waveTalk.readFirmwareVersion();
            return new RegisterValue(obisCode, firmwareVerison);
        }

        throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist!");
    }
}