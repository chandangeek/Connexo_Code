package com.energyict.protocolimpl.coronis.wavelog.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    private static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.5.2.255");
    private static final ObisCode OBISCODE_APPLICATION_OPERATION_MODE = ObisCode.fromString("0.0.96.5.1.255");
    private static final ObisCode OBISCODE_INPUT1 = ObisCode.fromString("0.1.96.3.1.255");
    private static final ObisCode OBISCODE_INPUT2 = ObisCode.fromString("0.2.96.3.1.255");
    private static final ObisCode OBISCODE_INPUT3 = ObisCode.fromString("0.3.96.3.1.255");
    private static final ObisCode OBISCODE_INPUT4 = ObisCode.fromString("0.4.96.3.1.255");
    private static final ObisCode OBISCODE_OUTPUT1 = ObisCode.fromString("0.1.96.3.2.255");
    private static final ObisCode OBISCODE_OUTPUT2 = ObisCode.fromString("0.2.96.3.2.255");
    private static final ObisCode OBISCODE_OUTPUT3 = ObisCode.fromString("0.3.96.3.2.255");
    private static final ObisCode OBISCODE_OUTPUT4 = ObisCode.fromString("0.4.96.3.2.255");
    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_RSSI = ObisCode.fromString("0.0.96.0.63.255");

    static {
        registerMaps.put(OBISCODE_APPLICATION_STATUS, "Application status");
        registerMaps.put(OBISCODE_APPLICATION_OPERATION_MODE, "Operation mode");
        registerMaps.put(OBISCODE_INPUT1, "Input level 1");
        registerMaps.put(OBISCODE_INPUT2, "Input level 2");
        registerMaps.put(OBISCODE_INPUT3, "Input level 3");
        registerMaps.put(OBISCODE_INPUT4, "Input level 4");
        registerMaps.put(OBISCODE_OUTPUT1, "Output level 1");
        registerMaps.put(OBISCODE_OUTPUT2, "Output level 2");
        registerMaps.put(OBISCODE_OUTPUT3, "Output level 3");
        registerMaps.put(OBISCODE_OUTPUT4, "Output level 4");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");
        registerMaps.put(OBISCODE_RSSI, "RSSI Level");
    }

    private WaveLog waveLog;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveLog the protocol
     */
    public ObisCodeMapper(final WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    public final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveLog.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        return new RegisterInfo(info);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
            int status = waveLog.getParameterFactory().readApplicationStatus().getStatus();
            return new RegisterValue(obisCode, new Quantity(status, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_APPLICATION_OPERATION_MODE)) {
            int mode = waveLog.getParameterFactory().readOperatingMode().getOperationMode();
            return new RegisterValue(obisCode, new Quantity(mode, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
            return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, waveLog.getFirmwareVersion());
        } else if (obisCode.equals(OBISCODE_RSSI)) {
            double value = waveLog.getRadioCommandFactory().readRSSI();
            return new RegisterValue(obisCode, new Quantity(value > 100 ? 100 : value, Unit.get("")), new Date());
        } else if (isInputStateObisCode(obisCode)) {
            int state = waveLog.getRadioCommandFactory().readCurrentInputState(obisCode.getB() - 1);
            return new RegisterValue(obisCode, new Quantity(state, Unit.get("")), new Date());
        } else if (isOutputStateObisCode(obisCode)) {
            int state = waveLog.getRadioCommandFactory().readCurrentOutputState(obisCode.getB() - 1);
            return new RegisterValue(obisCode, new Quantity(state, Unit.get("")), new Date());
        } else {
            throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] is not supported");
        }
    }

    private boolean isInputStateObisCode(ObisCode obisCode) {
        return (OBISCODE_INPUT1.equals(obisCode) || OBISCODE_INPUT2.equals(obisCode) || OBISCODE_INPUT3.equals(obisCode) || OBISCODE_INPUT4.equals(obisCode));
    }

    private boolean isOutputStateObisCode(ObisCode obisCode) {
        return (OBISCODE_OUTPUT1.equals(obisCode) || OBISCODE_OUTPUT2.equals(obisCode) || OBISCODE_OUTPUT3.equals(obisCode) || OBISCODE_OUTPUT4.equals(obisCode));
    }
}