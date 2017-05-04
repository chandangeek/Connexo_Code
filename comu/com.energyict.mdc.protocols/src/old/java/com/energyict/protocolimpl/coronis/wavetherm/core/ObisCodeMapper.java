package com.energyict.protocolimpl.coronis.wavetherm.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.CurrentReading;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    private static final ObisCode OBISCODE_CURRENTVALUE_SENSOR1 = ObisCode.fromString("1.1.82.8.0.255");
    private static final ObisCode OBISCODE_CURRENTVALUE_SENSOR2 = ObisCode.fromString("1.2.82.8.0.255");
    private static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.5.2.255");
    private static final ObisCode OBISCODE_OPERATION_MODE = ObisCode.fromString("0.0.96.5.1.255");
    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_RSSI = ObisCode.fromString("0.0.96.0.63.255");

    private static final ObisCode OBISCODE_HIGH_THRESHOLD_SENSOR1 = ObisCode.fromString("0.1.96.0.64.255");
    private static final ObisCode OBISCODE_HIGH_THRESHOLD_SENSOR2 = ObisCode.fromString("0.2.96.0.64.255");
    private static final ObisCode OBISCODE_LOW_THRESHOLD_SENSOR1 = ObisCode.fromString("0.1.96.0.65.255");
    private static final ObisCode OBISCODE_LOW_THRESHOLD_SENSOR2 = ObisCode.fromString("0.2.96.0.65.255");
    private static final ObisCode OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR1 = ObisCode.fromString("0.1.96.0.66.255");
    private static final ObisCode OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR2 = ObisCode.fromString("0.2.96.0.66.255");
    private static final ObisCode OBISCODE_LOW_THRESHOLD_DURATION_SENSOR1 = ObisCode.fromString("0.1.96.0.67.255");
    private static final ObisCode OBISCODE_LOW_THRESHOLD_DURATION_SENSOR2 = ObisCode.fromString("0.2.96.0.67.255");

    private static final ObisCode OBISCODE_DETECTION_MEASUREMENT_PERIOD = ObisCode.fromString("0.0.96.0.68.255");

    static {
        registerMaps.put(OBISCODE_CURRENTVALUE_SENSOR1, "Sensor 1 current value");
        registerMaps.put(OBISCODE_CURRENTVALUE_SENSOR2, "Sensor 2 current value");
        registerMaps.put(OBISCODE_APPLICATION_STATUS, "Application status");
        registerMaps.put(OBISCODE_OPERATION_MODE, "Operation mode");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");
        registerMaps.put(OBISCODE_RSSI, "RSSI Level");

        registerMaps.put(OBISCODE_HIGH_THRESHOLD_SENSOR1, "High treshold on sensor 1");
        registerMaps.put(OBISCODE_HIGH_THRESHOLD_SENSOR2, "High treshold on sensor 2");
        registerMaps.put(OBISCODE_LOW_THRESHOLD_SENSOR1, "Low treshold on sensor 1");
        registerMaps.put(OBISCODE_LOW_THRESHOLD_SENSOR2, "Low treshold on sensor 2");
        registerMaps.put(OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR1, "High threshold duration on sensor 1");
        registerMaps.put(OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR2, "High threshold duration on sensor 2");
        registerMaps.put(OBISCODE_LOW_THRESHOLD_DURATION_SENSOR1, "Low threshold duration on sensor 1");
        registerMaps.put(OBISCODE_LOW_THRESHOLD_DURATION_SENSOR2, "Low threshold duration on sensor 2");
        registerMaps.put(OBISCODE_DETECTION_MEASUREMENT_PERIOD, "Alarm detection measurement period");
    }

    private WaveTherm waveTherm;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveTherm the protocol
     */
    public ObisCodeMapper(final WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    public final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveTherm.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        return new RegisterInfo(info);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (OBISCODE_CURRENTVALUE_SENSOR1.equals(obisCode)) {
            CurrentReading currentReading = waveTherm.getRadioCommandFactory().readCurrentValue();
            if (currentReading.getCurrentValueA() == 0x4FFF) {
                throw new IOException("Sensor fault or absence, no temperature value available.");
            }
            return new RegisterValue(obisCode, new Quantity(currentReading.getCurrentValueA(), Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (OBISCODE_CURRENTVALUE_SENSOR2.equals(obisCode)) {
            CurrentReading currentReading = waveTherm.getRadioCommandFactory().readCurrentValue();
            if (currentReading.getCurrentValueB() == 0x4FFF) {
                throw new IOException("Sensor fault or absence, no temperature value available.");
            }
            return new RegisterValue(obisCode, new Quantity(currentReading.getCurrentValueB(), Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (obisCode.equals(OBISCODE_RSSI)) {
            double value = waveTherm.getRadioCommandFactory().readRSSI();
            return new RegisterValue(obisCode, new Quantity(value > 100 ? 100 : value, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
            return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, waveTherm.getFirmwareVersion());
        } else if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
            return new RegisterValue(obisCode, new Quantity(waveTherm.getParameterFactory().readApplicationStatus().getStatus(), Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_OPERATION_MODE)) {
            return new RegisterValue(obisCode, new Quantity(waveTherm.getParameterFactory().readOperatingMode().getOperationMode(), Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_HIGH_THRESHOLD_SENSOR1)) {
            double value = waveTherm.getParameterFactory().readHighThreshold(1);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (obisCode.equals(OBISCODE_HIGH_THRESHOLD_SENSOR2)) {
            double value = waveTherm.getParameterFactory().readHighThreshold(2);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (obisCode.equals(OBISCODE_LOW_THRESHOLD_SENSOR1)) {
            double value = waveTherm.getParameterFactory().readLowThreshold(1);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (obisCode.equals(OBISCODE_LOW_THRESHOLD_SENSOR2)) {
            double value = waveTherm.getParameterFactory().readLowThreshold(2);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DEGREE_CELSIUS)), new Date());
        } else if (obisCode.equals(OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR1)) {
            int value = waveTherm.getParameterFactory().readHighThresholdDuration(1);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_HIGH_THRESHOLD_DURATION_SENSOR2)) {
            int value = waveTherm.getParameterFactory().readHighThresholdDuration(2);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_LOW_THRESHOLD_DURATION_SENSOR1)) {
            int value = waveTherm.getParameterFactory().readLowThresholdDuration(1);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_LOW_THRESHOLD_DURATION_SENSOR2)) {
            int value = waveTherm.getParameterFactory().readLowThresholdDuration(2);
            return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_DETECTION_MEASUREMENT_PERIOD)) {
            int period = waveTherm.getParameterFactory().readDetectionMeasurementPeriod();
            return new RegisterValue(obisCode, new Quantity(period, Unit.get(BaseUnit.MINUTE)), new Date());
        } else {
            throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] is not supported");
        }
    }
}