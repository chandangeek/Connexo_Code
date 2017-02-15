/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class CommonObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<>();

    private static final int MULTIPLIER = 256;
    public static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.5.2.255");
    public static final ObisCode OBISCODE_OPERATION_MODE = ObisCode.fromString("0.0.96.5.1.255");

    public static final ObisCode OBISCODE_REMAINING_BATTERY = ObisCode.fromString("0.0.96.6.0.255");
    private static final ObisCode OBISCODE_PROFILE_TYPE = ObisCode.fromString("0.0.96.0.50.255");     //Waveflow specific register, E >= 50
    private static final ObisCode OBISCODE_PULSEWEIGHT_A = ObisCode.fromString("0.1.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_B = ObisCode.fromString("0.2.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_C = ObisCode.fromString("0.3.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_D = ObisCode.fromString("0.4.96.0.51.255");
    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");

    private static final ObisCode OBISCODE_SENT_FRAMES = ObisCode.fromString("0.0.96.0.52.255");
    public static final ObisCode OBISCODE_RELAYED_FRAMES = ObisCode.fromString("0.1.96.0.52.255");
    private static final ObisCode OBISCODE_RECEIVED_FRAMES = ObisCode.fromString("0.0.96.0.53.255");
    private static final ObisCode OBISCODE_ELAPSED_DAYS = ObisCode.fromString("0.0.96.0.54.255");

    private static final ObisCode OBISCODE_PROFILEDATA_INTERVAL = ObisCode.fromString("8.0.0.8.1.255");
    private static final ObisCode OBISCODE_LOGGING_MODE = ObisCode.fromString("0.0.96.0.55.255");
    private static final ObisCode OBISCODE_DATALOGGING_STARTHOUR = ObisCode.fromString("0.0.96.0.56.255");
    private static final ObisCode OBISCODE_DATALOGGING_STARTMINUTE = ObisCode.fromString("0.0.96.0.57.255");
    private static final ObisCode OBISCODE_DATALOGGING_DAYOFWEEK = ObisCode.fromString("0.0.96.0.58.255");

    private static final ObisCode OBISCODE_TIME_DURATION_RX = ObisCode.fromString("0.0.96.0.59.255");
    private static final ObisCode OBISCODE_TIME_DURATION_TX = ObisCode.fromString("0.0.96.0.60.255");
    private static final ObisCode OBISCODE_NUMBER_OF_FRAME_RX = ObisCode.fromString("0.0.96.0.61.255");
    private static final ObisCode OBISCODE_NUMBER_OF_FRAME_TX = ObisCode.fromString("0.0.96.0.62.255");

    public static final ObisCode OBISCODE_RSSI_LEVEL = ObisCode.fromString("0.0.96.0.63.255");

    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD1 = ObisCode.fromString("8.1.96.50.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD2 = ObisCode.fromString("8.2.96.50.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD3 = ObisCode.fromString("8.3.96.50.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD4 = ObisCode.fromString("8.4.96.50.0.255");

    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_THRESHOLD1 = ObisCode.fromString("8.1.96.50.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_THRESHOLD2 = ObisCode.fromString("8.2.96.50.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_THRESHOLD3 = ObisCode.fromString("8.3.96.50.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_THRESHOLD4 = ObisCode.fromString("8.4.96.50.1.255");

    private static final ObisCode OBISCODE_LEAKAGE_MEASUREMENTSTEP = ObisCode.fromString("8.0.96.51.2.255");

    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_PERIOD1 = ObisCode.fromString("8.1.96.51.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_PERIOD2 = ObisCode.fromString("8.2.96.51.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_PERIOD3 = ObisCode.fromString("8.3.96.51.0.255");
    private static final ObisCode OBISCODE_RESIDUAL_LEAKAGE_PERIOD4 = ObisCode.fromString("8.4.96.51.0.255");

    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_PERIOD1 = ObisCode.fromString("8.1.96.51.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_PERIOD2 = ObisCode.fromString("8.2.96.51.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_PERIOD3 = ObisCode.fromString("8.3.96.51.1.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKAGE_PERIOD4 = ObisCode.fromString("8.4.96.51.1.255");

    private static final ObisCode OBISCODE_SIMPLE_BACKFLOW_THRESHOLD1 = ObisCode.fromString("8.1.96.52.0.255");
    private static final ObisCode OBISCODE_SIMPLE_BACKFLOW_THRESHOLD2 = ObisCode.fromString("8.2.96.52.0.255");
    private static final ObisCode OBISCODE_ADVANCED_BACKFLOW_THRESHOLD1 = ObisCode.fromString("8.1.96.52.1.255");
    private static final ObisCode OBISCODE_ADVANCED_BACKFLOW_THRESHOLD2 = ObisCode.fromString("8.2.96.52.1.255");

    private static final ObisCode OBISCODE_SIMPLE_BACKFLOW_PERIOD1 = ObisCode.fromString("8.1.96.53.0.255");
    private static final ObisCode OBISCODE_SIMPLE_BACKFLOW_PERIOD2 = ObisCode.fromString("8.2.96.53.0.255");
    private static final ObisCode OBISCODE_ADVANCED_BACKFLOW_PERIOD1 = ObisCode.fromString("8.1.96.53.1.255");
    private static final ObisCode OBISCODE_ADVANCED_BACKFLOW_PERIOD2 = ObisCode.fromString("8.2.96.53.1.255");

    private static final ObisCode OBISCODE_RESIDUAL_LEAKDETECTION_ENABLED = ObisCode.fromString("8.0.96.54.0.255");
    private static final ObisCode OBISCODE_EXTREME_LEAKDETECTION_ENABLED = ObisCode.fromString("8.0.96.54.1.255");
    private static final ObisCode OBISCODE_REEDFAULTDETECTION_ENABLED = ObisCode.fromString("8.0.96.55.0.255");
    private static final ObisCode OBISCODE_WIRECUTDETECTION_ENABLED = ObisCode.fromString("8.0.96.56.0.255");
    private static final ObisCode OBISCODE_SIMPLEBACKFLOW_ENABLED = ObisCode.fromString("8.0.96.57.0.255");
    private static final ObisCode OBISCODE_ADVANCED_BACKFLOW_ENABLED = ObisCode.fromString("8.0.96.57.1.255");

    private static final ObisCode OBISCODE_WakeUpSystemStatusWord = ObisCode.fromString("0.0.96.0.103.255");
    private static final ObisCode OBISCODE_DefaultWakeUpPeriod = ObisCode.fromString("0.0.96.0.104.255");
    private static final ObisCode OBISCODE_StartTimeForTimeWindow1 = ObisCode.fromString("0.0.96.0.105.255");
    private static final ObisCode OBISCODE_WakeUpPeriodForTimeWindow1 = ObisCode.fromString("0.0.96.0.106.255");
    private static final ObisCode OBISCODE_StartTimeForTimeWindow2 = ObisCode.fromString("0.0.96.0.107.255");
    private static final ObisCode OBISCODE_WakeUpPeriodForTimeWindow2 = ObisCode.fromString("0.0.96.0.108.255");
    private static final ObisCode OBISCODE_EnableTimeWindowsByDayOfWeek = ObisCode.fromString("0.0.96.0.109.255");
    private static final ObisCode OBISCODE_EnableWakeUpPeriodsByDayOfWeek = ObisCode.fromString("0.0.96.0.110.255");
    private static final ObisCode OBISCODE_ALARM_CONFIG_BYTE = ObisCode.fromString("0.0.96.0.111.255");

    static {
        registerMaps.put(OBISCODE_REMAINING_BATTERY, "Available battery power in %");
        registerMaps.put(OBISCODE_RSSI_LEVEL, "Received signal strength indication");
        registerMaps.put(OBISCODE_APPLICATION_STATUS, "Application status");
        registerMaps.put(OBISCODE_OPERATION_MODE, "Operation mode");
        registerMaps.put(OBISCODE_PROFILE_TYPE, "Module profile type");
        registerMaps.put(OBISCODE_PULSEWEIGHT_A, "Pulse weight for input channel A");
        registerMaps.put(OBISCODE_PULSEWEIGHT_B, "Pulse weight for input channel B");
        registerMaps.put(OBISCODE_PULSEWEIGHT_C, "Pulse weight for input channel C");
        registerMaps.put(OBISCODE_PULSEWEIGHT_D, "Pulse weight for input channel D");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");
        registerMaps.put(OBISCODE_SENT_FRAMES, "Number of frames sent by the module");
        registerMaps.put(OBISCODE_RECEIVED_FRAMES, "Number of frames received by the module");
        registerMaps.put(OBISCODE_ELAPSED_DAYS, "Number of days elapsed by the module");

        registerMaps.put(OBISCODE_PROFILEDATA_INTERVAL, "Profile data interval");
        registerMaps.put(OBISCODE_LOGGING_MODE, "Data logging mode");
        registerMaps.put(OBISCODE_DATALOGGING_STARTHOUR, "Data logging start hour");
        registerMaps.put(OBISCODE_DATALOGGING_STARTMINUTE, "Data logging start minute");
        registerMaps.put(OBISCODE_DATALOGGING_DAYOFWEEK, "Data logging day of week/month");

        registerMaps.put(OBISCODE_LEAKAGE_MEASUREMENTSTEP, "Measurement step for leakage detection");

        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD1, "Residual leakage threshold on input 1");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD2, "Residual leakage threshold on input 2");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD3, "Residual leakage threshold on input 3");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD4, "Residual leakage threshold on input 4");

        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_THRESHOLD1, "Extreme leakage threshold on input 1");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_THRESHOLD2, "Extreme leakage threshold on input 2");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_THRESHOLD3, "Extreme leakage threshold on input 3");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_THRESHOLD4, "Extreme leakage threshold on input 4");

        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_PERIOD1, "Residual leakage period on input 1");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_PERIOD2, "Residual leakage period on input 2");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_PERIOD3, "Residual leakage period on input 3");
        registerMaps.put(OBISCODE_RESIDUAL_LEAKAGE_PERIOD4, "Residual leakage period on input 4");

        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_PERIOD1, "Extreme leakage period on input 1");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_PERIOD2, "Extreme leakage period on input 2");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_PERIOD3, "Extreme leakage period on input 3");
        registerMaps.put(OBISCODE_EXTREME_LEAKAGE_PERIOD4, "Extreme leakage period on input 4");

        registerMaps.put(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD1, "Simple backflow threshold on input 1");
        registerMaps.put(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD2, "Simple backflow threshold on input 2");
        registerMaps.put(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD1, "Advanced backflow threshold on input 1");
        registerMaps.put(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD2, "Advanced backflow threshold on input 2");

        registerMaps.put(OBISCODE_SIMPLE_BACKFLOW_PERIOD1, "Simple backflow period on input 1");
        registerMaps.put(OBISCODE_SIMPLE_BACKFLOW_PERIOD2, "Simple backflow period on input 2");
        registerMaps.put(OBISCODE_ADVANCED_BACKFLOW_PERIOD1, "Advanced backflow period on input 1");
        registerMaps.put(OBISCODE_ADVANCED_BACKFLOW_PERIOD2, "Advanced backflow period on input 2");

        registerMaps.put(OBISCODE_TIME_DURATION_RX, "Time duration on RX");
        registerMaps.put(OBISCODE_TIME_DURATION_TX, "Time duration on TX");
        registerMaps.put(OBISCODE_NUMBER_OF_FRAME_RX, "Number of frame in RX");
        registerMaps.put(OBISCODE_NUMBER_OF_FRAME_TX, "Number of frame in TX");

        registerMaps.put(OBISCODE_RESIDUAL_LEAKDETECTION_ENABLED, "Residual leak detection enabled");
        registerMaps.put(OBISCODE_EXTREME_LEAKDETECTION_ENABLED, "Extreme leak detection enabled");
        registerMaps.put(OBISCODE_REEDFAULTDETECTION_ENABLED, "Reed fault detection enabled");
        registerMaps.put(OBISCODE_SIMPLEBACKFLOW_ENABLED, "Simple backflow detection enabled");
        registerMaps.put(OBISCODE_ADVANCED_BACKFLOW_ENABLED, "Advanced backflow detection enabled");
        registerMaps.put(OBISCODE_WIRECUTDETECTION_ENABLED, "Wirecut detection enabled");

        registerMaps.put(OBISCODE_WakeUpSystemStatusWord, "WakeUp system status word");
        registerMaps.put(OBISCODE_DefaultWakeUpPeriod, "Default WakeUp period (in second)");
        registerMaps.put(OBISCODE_StartTimeForTimeWindow1, "Start time for 1st time window ");
        registerMaps.put(OBISCODE_WakeUpPeriodForTimeWindow1, "WakeUp period for 1st time window (in second) ");
        registerMaps.put(OBISCODE_StartTimeForTimeWindow2, "Start time for 2nd time window");
        registerMaps.put(OBISCODE_WakeUpPeriodForTimeWindow2, "WakeUp period for 2nd time window (in second) ");
        registerMaps.put(OBISCODE_EnableTimeWindowsByDayOfWeek, "Enable time windows by day of the week ");
        registerMaps.put(OBISCODE_EnableWakeUpPeriodsByDayOfWeek, "Enable WakeUp periods by day of the week");
        registerMaps.put(OBISCODE_ALARM_CONFIG_BYTE, "Alarm configuration byte");
    }

    private WaveFlow waveFlow;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlow the protocol containing all settings
     */
    public CommonObisCodeMapper(final WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public String getRegisterExtendedLogging() {

        StringBuilder strBuilder = new StringBuilder();

        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveFlow.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
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

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {
            OperatingMode operatingMode = waveFlow.getParameterFactory().readOperatingMode();
            if (obisCode.equals(OBISCODE_REMAINING_BATTERY)) {
                double level = waveFlow.getParameterFactory().readBatteryLifeDurationCounter();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(level), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
                int status = waveFlow.getParameterFactory().readApplicationStatus();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(status), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_PROFILEDATA_INTERVAL)) {
                int interval = waveFlow.getParameterFactory().getProfileIntervalInSeconds();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(interval), Unit.get(BaseUnit.SECOND)), new Date());
            } else if (obisCode.equals(OBISCODE_DATALOGGING_STARTHOUR)) {
                int hour = waveFlow.getParameterFactory().readStartHourOfMeasurement();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(hour), Unit.get(BaseUnit.HOUR)), new Date());
            } else if (obisCode.equals(OBISCODE_DATALOGGING_STARTMINUTE)) {
                if (waveFlow.isV1()) {
                    waveFlow.getLogger().log(Level.WARNING, "The WaveFlow V1 module doesn't support the data logging start minute parameter");
                    throw new NoSuchRegisterException("The WaveFlow V1 module doesn't support the data logging start minute parameter");
                }
                int minute = waveFlow.getParameterFactory().readStartMinuteOfMeasurement();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(minute), Unit.get(BaseUnit.MINUTE)), new Date());
            } else if (obisCode.equals(OBISCODE_DATALOGGING_DAYOFWEEK)) {
                int day = waveFlow.getParameterFactory().readDayOfWeek();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(day), Unit.get(BaseUnit.DAY)), new Date());
            } else if (obisCode.equals(OBISCODE_LEAKAGE_MEASUREMENTSTEP)) {
                int step = waveFlow.getParameterFactory().readMeasurementStep();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(step), Unit.get(BaseUnit.MINUTE)), new Date());
            } else if (obisCode.equals(OBISCODE_WakeUpSystemStatusWord)) {
                int value = waveFlow.getParameterFactory().getWakeUpSystemStatusWord();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_DefaultWakeUpPeriod)) {
                int value = waveFlow.getParameterFactory().getDefaultWakeUpPeriod();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_StartTimeForTimeWindow1)) {
                int value = waveFlow.getParameterFactory().getStartTimeForTimeWindow1();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_WakeUpPeriodForTimeWindow1)) {
                int value = waveFlow.getParameterFactory().getWakeUpPeriodForTimeWindow1();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_StartTimeForTimeWindow2)) {
                int value = waveFlow.getParameterFactory().getStartTimeForTimeWindow2();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_WakeUpPeriodForTimeWindow2)) {
                int value = waveFlow.getParameterFactory().getWakeUpPeriodForTimeWindow2();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EnableTimeWindowsByDayOfWeek)) {
                int value = waveFlow.getParameterFactory().getEnableTimeWindowsByDayOfWeek();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EnableWakeUpPeriodsByDayOfWeek)) {
                int value = waveFlow.getParameterFactory().getEnableWakeUpPeriodsByDayOfWeek();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_ALARM_CONFIG_BYTE)) {
                int value = waveFlow.getParameterFactory().readAlarmConfigurationValue();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD1)) {
                int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD2)) {
                int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD3)) {
                int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(3);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD4)) {
                int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(4);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD1)) {
                int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD2)) {
                int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD3)) {
                int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(3);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD4)) {
                int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(4);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD1)) {
                int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD2)) {
                int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD3)) {
                int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(3);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD4)) {
                int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(4);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD1)) {
                int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD2)) {
                int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD3)) {
                int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(3);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD4)) {
                int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(4);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(period), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD1)) {
                int threshold = waveFlow.getParameterFactory().readSimpleBackflowThreshold(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD2)) {
                int threshold = waveFlow.getParameterFactory().readSimpleBackflowThreshold(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD1)) {
                int threshold = waveFlow.getParameterFactory().readAdvancedBackflowThreshold(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD2)) {
                int threshold = waveFlow.getParameterFactory().readAdvancedBackflowThreshold(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_PERIOD1)) {
                int threshold = waveFlow.getParameterFactory().readSimpleBackflowDetectionPeriod(1);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.HOUR)), new Date());
            } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_PERIOD2)) {
                int threshold = waveFlow.getParameterFactory().readSimpleBackflowDetectionPeriod(2);
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.HOUR)), new Date());
            } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_PERIOD1)) {
                int threshold = waveFlow.getParameterFactory().readAdvancedBackflowDetectionPeriod(1) * 10;     //Expressed in multiples of 10 minutes
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.MINUTE)), new Date());
            } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_PERIOD2)) {
                int threshold = waveFlow.getParameterFactory().readAdvancedBackflowDetectionPeriod(2) * 10;     //Expressed in multiples of 10 minutes
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.MINUTE)), new Date());
            } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKDETECTION_ENABLED)) {
                int enabled = operatingMode.residualLeakDetectionIsEnabled() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_EXTREME_LEAKDETECTION_ENABLED)) {
                int enabled = operatingMode.extremeLeakDetectionIsEnabled() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_REEDFAULTDETECTION_ENABLED)) {
                int enabled = operatingMode.reedFaultDetectionIsEnabled() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_WIRECUTDETECTION_ENABLED)) {
                int enabled = operatingMode.wireCutDetectionIsEnabled() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_SIMPLEBACKFLOW_ENABLED)) {
                ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
                int enabled = profileType.supportsSimpleBackflowDetection() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_ENABLED)) {
                ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
                int enabled = profileType.supportsAdvancedBackflowDetection() ? 1 : 0;
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), new Date(), null, new Date(), new Date(), 0, enabled == 1 ? "Enabled" : "Disabled");
            } else if (obisCode.equals(OBISCODE_LOGGING_MODE)) {
                int steps = operatingMode.dataLoggingSteps();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(steps), Unit.get("")), new Date(), null, new Date(), new Date(), 0, operatingMode.getLoggingDescription());
            } else if (obisCode.equals(OBISCODE_OPERATION_MODE)) {
                int mode = operatingMode.getOperationMode();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(mode), Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
                return new RegisterValue(obisCode, waveFlow.readFirmwareVersion());
            } else if (obisCode.equals(OBISCODE_ELAPSED_DAYS)) {
                int nr = waveFlow.getParameterFactory().readElapsedDays();
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(nr), Unit.get(BaseUnit.DAY)), new Date());
            } else if (obisCode.equals(OBISCODE_SENT_FRAMES)) {
                int nr = waveFlow.getParameterFactory().readNumberOfSentFrames();
                return new RegisterValue(obisCode, new Quantity(nr * MULTIPLIER, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RECEIVED_FRAMES)) {
                int nr = waveFlow.getParameterFactory().readNumberOfReceivedFrames();
                return new RegisterValue(obisCode, new Quantity(nr * MULTIPLIER, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_PROFILE_TYPE)) {
                ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(profileType.getType()), Unit.get("")), new Date(), null, new Date(), new Date(), 0, profileType.getDescription());
            } else if (obisCode.equals(OBISCODE_TIME_DURATION_RX)) {
                int value = waveFlow.getParameterFactory().getTimeDurationRX();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.SECOND, -3)), new Date());
            } else if (obisCode.equals(OBISCODE_TIME_DURATION_TX)) {
                int value = waveFlow.getParameterFactory().getTimeDurationTX();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.SECOND, -3)), new Date());
            } else if (obisCode.equals(OBISCODE_NUMBER_OF_FRAME_RX)) {
                int value = waveFlow.getParameterFactory().getNumberOfFramesInRx();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_NUMBER_OF_FRAME_TX)) {
                int value = waveFlow.getParameterFactory().getNumberOfFramesInTx();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_RSSI_LEVEL)) {
                double value = waveFlow.getRadioCommandFactory().readRSSILevel();
                return new RegisterValue(obisCode, new Quantity(value > 100 ? 100 : value, Unit.get("")), new Date());      //A percentage representing the saturation
            } else if (isPulseWeightReadout(obisCode)) {
                int inputChannel = (obisCode.getB());
                PulseWeight pulseWeight = waveFlow.getPulseWeight(inputChannel - 1, true);
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(pulseWeight.getWeight()), pulseWeight.getUnit()));
            }
            waveFlow.getLogger().log(Level.SEVERE, "Register with obis code [" + obisCode + "] does not exist!");
            throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist!");
        } catch (IOException e) {
            if (!(e instanceof NoSuchRegisterException)) {
                waveFlow.getLogger().log(Level.SEVERE, "Error getting [" + obisCode + "]: timeout, " + e.getMessage());
            }
            throw e;
        }
    }

    private boolean isPulseWeightReadout(ObisCode obisCode) {
        return (obisCode.equals(OBISCODE_PULSEWEIGHT_A) || obisCode.equals(OBISCODE_PULSEWEIGHT_B) || obisCode.equals(OBISCODE_PULSEWEIGHT_C) || obisCode.equals(OBISCODE_PULSEWEIGHT_D));
    }
}