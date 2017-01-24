package com.energyict.protocolimpl.coronis.amco.rtm;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.EncoderUnit;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.RtmUnit;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.CurrentRegisterReading;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.ReadTOUBuckets;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.ValveStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_REMAINING_BATTERY = ObisCode.fromString("0.0.96.6.0.255");
    private static final ObisCode OBISCODE_PROFILE_TYPE = ObisCode.fromString("0.0.96.0.50.255");     //Waveflow specific register, E >= 50
    private static final ObisCode OBISCODE_ENCODER_MODEL_TYPE_A = ObisCode.fromString("0.1.96.0.64.255");
    private static final ObisCode OBISCODE_ENCODER_MODEL_TYPE_B = ObisCode.fromString("0.2.96.0.64.255");

    private static final ObisCode OBISCODE_OPERATION_MODE = ObisCode.fromString("0.0.96.5.1.255");
    private static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.5.2.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_A = ObisCode.fromString("0.1.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_B = ObisCode.fromString("0.2.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_C = ObisCode.fromString("0.3.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_D = ObisCode.fromString("0.4.96.0.51.255");
    private static final ObisCode OBISCODE_ENCODERUNIT_A = ObisCode.fromString("0.1.96.0.52.255");
    private static final ObisCode OBISCODE_ENCODERUNIT_B = ObisCode.fromString("0.2.96.0.52.255");
    private static final ObisCode OBISCODE_VALVE_STATUS = ObisCode.fromString("0.0.96.3.10.255");

    private static final ObisCode OBISCODE_TOUBUCKET1_PORT1 = ObisCode.fromString("0.1.96.0.53.255");
    private static final ObisCode OBISCODE_TOUBUCKET2_PORT1 = ObisCode.fromString("0.1.96.0.54.255");
    private static final ObisCode OBISCODE_TOUBUCKET3_PORT1 = ObisCode.fromString("0.1.96.0.55.255");
    private static final ObisCode OBISCODE_TOUBUCKET4_PORT1 = ObisCode.fromString("0.1.96.0.56.255");
    private static final ObisCode OBISCODE_TOUBUCKET5_PORT1 = ObisCode.fromString("0.1.96.0.57.255");
    private static final ObisCode OBISCODE_TOUBUCKET6_PORT1 = ObisCode.fromString("0.1.96.0.58.255");

    private static final ObisCode OBISCODE_TOUBUCKET1_PORT2 = ObisCode.fromString("0.2.96.0.53.255");
    private static final ObisCode OBISCODE_TOUBUCKET2_PORT2 = ObisCode.fromString("0.2.96.0.54.255");
    private static final ObisCode OBISCODE_TOUBUCKET3_PORT2 = ObisCode.fromString("0.2.96.0.55.255");
    private static final ObisCode OBISCODE_TOUBUCKET4_PORT2 = ObisCode.fromString("0.2.96.0.56.255");
    private static final ObisCode OBISCODE_TOUBUCKET5_PORT2 = ObisCode.fromString("0.2.96.0.57.255");
    private static final ObisCode OBISCODE_TOUBUCKET6_PORT2 = ObisCode.fromString("0.2.96.0.58.255");

    private static final ObisCode OBISCODE_TOUBUCKET1_PORT3 = ObisCode.fromString("0.3.96.0.53.255");
    private static final ObisCode OBISCODE_TOUBUCKET2_PORT3 = ObisCode.fromString("0.3.96.0.54.255");
    private static final ObisCode OBISCODE_TOUBUCKET3_PORT3 = ObisCode.fromString("0.3.96.0.55.255");
    private static final ObisCode OBISCODE_TOUBUCKET4_PORT3 = ObisCode.fromString("0.3.96.0.56.255");
    private static final ObisCode OBISCODE_TOUBUCKET5_PORT3 = ObisCode.fromString("0.3.96.0.57.255");
    private static final ObisCode OBISCODE_TOUBUCKET6_PORT3 = ObisCode.fromString("0.3.96.0.58.255");

    private static final ObisCode OBISCODE_TOUBUCKET1_PORT4 = ObisCode.fromString("0.4.96.0.53.255");
    private static final ObisCode OBISCODE_TOUBUCKET2_PORT4 = ObisCode.fromString("0.4.96.0.54.255");
    private static final ObisCode OBISCODE_TOUBUCKET3_PORT4 = ObisCode.fromString("0.4.96.0.55.255");
    private static final ObisCode OBISCODE_TOUBUCKET4_PORT4 = ObisCode.fromString("0.4.96.0.56.255");
    private static final ObisCode OBISCODE_TOUBUCKET5_PORT4 = ObisCode.fromString("0.4.96.0.57.255");
    private static final ObisCode OBISCODE_TOUBUCKET6_PORT4 = ObisCode.fromString("0.4.96.0.58.255");

    private static final ObisCode OBISCODE_COMMAND_BUFFER = ObisCode.fromString("0.0.96.0.101.255");
    private static final ObisCode OBISCODE_BUBBLE_UP_START_HOUR = ObisCode.fromString("0.0.96.0.102.255");
    private static final ObisCode OBISCODE_RSSI = ObisCode.fromString("0.0.96.0.63.255");

    private static final String OBISCODE_PORT1 = "1.1.82.8.0.255";
    private static final String OBISCODE_PORT2 = "1.2.82.8.0.255";
    private static final String OBISCODE_PORT3 = "1.3.82.8.0.255";
    private static final String OBISCODE_PORT4 = "1.4.82.8.0.255";

    private static final ObisCode OBISCODE_PROFILEDATA_INTERVAL = ObisCode.fromString("8.0.0.8.1.255");
    private static final ObisCode OBISCODE_LOGGING_MODE = ObisCode.fromString("0.0.96.0.55.255");
    private static final ObisCode OBISCODE_DATALOGGING_STARTHOUR = ObisCode.fromString("0.0.96.0.56.255");
    private static final ObisCode OBISCODE_DATALOGGING_DAYOFWEEK = ObisCode.fromString("0.0.96.0.58.255");

    static {
        registerMaps.put(OBISCODE_PROFILEDATA_INTERVAL, "Profile data interval");
        registerMaps.put(OBISCODE_LOGGING_MODE, "Data logging mode");
        registerMaps.put(OBISCODE_DATALOGGING_STARTHOUR, "Data logging start hour");
        registerMaps.put(OBISCODE_DATALOGGING_DAYOFWEEK, "Data logging day of week/month");

        registerMaps.put(OBISCODE_REMAINING_BATTERY, "Available battery power in %");
        registerMaps.put(OBISCODE_APPLICATION_STATUS, "Application status");
        registerMaps.put(OBISCODE_OPERATION_MODE, "Operation mode");
        registerMaps.put(OBISCODE_ENCODER_MODEL_TYPE_A, "Encoder model type A");
        registerMaps.put(OBISCODE_ENCODER_MODEL_TYPE_B, "Encoder model type B");
        registerMaps.put(OBISCODE_PROFILE_TYPE, "Module profile type");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");
        registerMaps.put(OBISCODE_VALVE_STATUS, "Status of the valve");

        registerMaps.put(OBISCODE_PULSEWEIGHT_A, "Pulse weight for port A");
        registerMaps.put(OBISCODE_PULSEWEIGHT_B, "Pulse weight for port B");
        registerMaps.put(OBISCODE_PULSEWEIGHT_C, "Pulse weight for port C");
        registerMaps.put(OBISCODE_PULSEWEIGHT_D, "Pulse weight for port D");

        registerMaps.put(OBISCODE_ENCODERUNIT_A, "Encoder unit for port A");
        registerMaps.put(OBISCODE_ENCODERUNIT_B, "Encoder unit for port B");

        registerMaps.put(OBISCODE_TOUBUCKET1_PORT1, "TOU Bucket 1 totalizer for port 1");
        registerMaps.put(OBISCODE_TOUBUCKET2_PORT1, "TOU Bucket 2 totalizer for port 1");
        registerMaps.put(OBISCODE_TOUBUCKET3_PORT1, "TOU Bucket 3 totalizer for port 1");
        registerMaps.put(OBISCODE_TOUBUCKET4_PORT1, "TOU Bucket 4 totalizer for port 1");
        registerMaps.put(OBISCODE_TOUBUCKET5_PORT1, "TOU Bucket 5 totalizer for port 1");
        registerMaps.put(OBISCODE_TOUBUCKET6_PORT1, "TOU Bucket 6 totalizer for port 1");

        registerMaps.put(OBISCODE_TOUBUCKET1_PORT2, "TOU Bucket 1 totalizer for port 2");
        registerMaps.put(OBISCODE_TOUBUCKET2_PORT2, "TOU Bucket 2 totalizer for port 2");
        registerMaps.put(OBISCODE_TOUBUCKET3_PORT2, "TOU Bucket 3 totalizer for port 2");
        registerMaps.put(OBISCODE_TOUBUCKET4_PORT2, "TOU Bucket 4 totalizer for port 2");
        registerMaps.put(OBISCODE_TOUBUCKET5_PORT2, "TOU Bucket 5 totalizer for port 2");
        registerMaps.put(OBISCODE_TOUBUCKET6_PORT2, "TOU Bucket 6 totalizer for port 2");

        registerMaps.put(OBISCODE_TOUBUCKET1_PORT3, "TOU Bucket 1 totalizer for port 3");
        registerMaps.put(OBISCODE_TOUBUCKET2_PORT3, "TOU Bucket 2 totalizer for port 3");
        registerMaps.put(OBISCODE_TOUBUCKET3_PORT3, "TOU Bucket 3 totalizer for port 3");
        registerMaps.put(OBISCODE_TOUBUCKET4_PORT3, "TOU Bucket 4 totalizer for port 3");
        registerMaps.put(OBISCODE_TOUBUCKET5_PORT3, "TOU Bucket 5 totalizer for port 3");
        registerMaps.put(OBISCODE_TOUBUCKET6_PORT3, "TOU Bucket 6 totalizer for port 3");

        registerMaps.put(OBISCODE_TOUBUCKET1_PORT4, "TOU Bucket 1 totalizer for port 4");
        registerMaps.put(OBISCODE_TOUBUCKET2_PORT4, "TOU Bucket 2 totalizer for port 4");
        registerMaps.put(OBISCODE_TOUBUCKET3_PORT4, "TOU Bucket 3 totalizer for port 4");
        registerMaps.put(OBISCODE_TOUBUCKET4_PORT4, "TOU Bucket 4 totalizer for port 4");
        registerMaps.put(OBISCODE_TOUBUCKET5_PORT4, "TOU Bucket 5 totalizer for port 4");
        registerMaps.put(OBISCODE_TOUBUCKET6_PORT4, "TOU Bucket 6 totalizer for port 4");
        registerMaps.put(OBISCODE_RSSI, "RSSI Level");
        registerMaps.put(OBISCODE_COMMAND_BUFFER, "Bubble up command buffer");
        registerMaps.put(OBISCODE_BUBBLE_UP_START_HOUR, "Bubble up start hour");

        registerMaps.put(ObisCode.fromString(OBISCODE_PORT1), "Port A current index");
        registerMaps.put(ObisCode.fromString(OBISCODE_PORT2), "Port B current index");
        registerMaps.put(ObisCode.fromString(OBISCODE_PORT3), "Port C current index");
        registerMaps.put(ObisCode.fromString(OBISCODE_PORT4), "Port D current index");
    }

    private RTM rtm;

    public ObisCodeMapper(final RTM rtm) {
        this.rtm = rtm;
    }

    public final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            rtm.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        String info = registerMaps.get(obisCode);
        return new RegisterInfo(info);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode, RTMFactory rtmFactory) throws IOException {
        try {
            if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
                int status = rtm.getParameterFactory().readApplicationStatus().getStatus();
                return new RegisterValue(obisCode, new Quantity(status, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_REMAINING_BATTERY)) {
                int value = rtm.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(value), Unit.get(BaseUnit.UNITLESS)), new Date());
            } else if (obisCode.equals(OBISCODE_OPERATION_MODE)) {
                int mode = rtm.getParameterFactory().readOperatingMode().getOperationMode();
                return new RegisterValue(obisCode, new Quantity(mode, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, rtm.readFirmwareVersion());
            } else if (obisCode.equals(OBISCODE_RSSI)) {
                double value = rtm.getRadioCommandFactory().readRSSI().getRssiLevel();
                return new RegisterValue(obisCode, new Quantity(value > 100 ? 100 : value, Unit.get("")), new Date());
            } else if (obisCode.equals(OBISCODE_PROFILEDATA_INTERVAL)) {
                int profileIntervalInSeconds = rtm.getParameterFactory().getProfileIntervalInSeconds();
                return new RegisterValue(obisCode, new Quantity(profileIntervalInSeconds, Unit.get(BaseUnit.SECOND)), new Date());
            } else if (obisCode.equals(OBISCODE_LOGGING_MODE)) {
                OperatingMode operatingMode = rtm.getParameterFactory().readOperatingMode();
                int mode = operatingMode.getDataLoggingMode();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(mode), Unit.get("")), new Date(), null, new Date(), new Date(), 0, operatingMode.getLoggingDescription());
            } else if (obisCode.equals(OBISCODE_DATALOGGING_STARTHOUR)) {
                int hour = rtm.getParameterFactory().readTimeOfMeasurement();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(hour), Unit.get(BaseUnit.HOUR)), new Date());
            } else if (obisCode.equals(OBISCODE_DATALOGGING_DAYOFWEEK)) {
                int day = rtm.getParameterFactory().readDayOfWeek();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(day), Unit.get(BaseUnit.DAY)), new Date());
            } else if (obisCode.equals(OBISCODE_COMMAND_BUFFER)) {
                String cmdBuffer = rtm.getParameterFactory().readPushCommandBuffer().getBuffer();
                int cmd;
                try {
                    cmd = Integer.parseInt(cmdBuffer.substring(2, 4));
                } catch (Exception e) {
                    rtm.getLogger().log(Level.WARNING, "Could not parse command buffer: " + cmdBuffer);
                    throw new IOException("Could not parse command buffer: " + cmdBuffer);
                }
                String additionalText = "Full buffer content (hex): " + cmdBuffer.substring(2);
                return new RegisterValue(obisCode, new Quantity(cmd, Unit.get("")), new Date(), null, new Date(), new Date(), 0, additionalText);
            } else if (obisCode.equals(OBISCODE_VALVE_STATUS)) {
                if (!rtm.getParameterFactory().readProfileType().isValve()) {
                    rtm.getLogger().log(Level.WARNING, "Module doesn't support valve control");
                    throw new NoSuchRegisterException("Module doesn't support valve control");
                }
                ValveStatus valveStatus = rtm.getRadioCommandFactory().readValveStatus();
                return new RegisterValue(obisCode, new Quantity(valveStatus.getState(), Unit.get("")), new Date(), null, new Date(), new Date(), 0, valveStatus.getDescription());
            } else if (obisCode.equals(OBISCODE_BUBBLE_UP_START_HOUR)) {
                String startHour = rtm.getParameterFactory().readStartOfPushFrameMechanism();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, startHour);
            } else if (obisCode.equals(OBISCODE_PROFILE_TYPE)) {
                ProfileType profileType = rtm.getParameterFactory().readProfileType();
                return new RegisterValue(obisCode, new Quantity(profileType.getProfile(), Unit.get("")), new Date(), null, new Date(), new Date(), 0, profileType.getDescription());
            } else if (obisCode.equals(OBISCODE_ENCODER_MODEL_TYPE_A)) {
                String description = rtm.getRadioCommandFactory().readEncoderModelTypeA();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, description);
            } else if (obisCode.equals(OBISCODE_ENCODER_MODEL_TYPE_B)) {
                String description = rtm.getRadioCommandFactory().readEncoderModelTypeB();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, description);
            } else if (isCurrentIndexReading(obisCode)) {
                int port = obisCode.getB();
                CurrentRegisterReading currentRegister = rtm.getRadioCommandFactory().readCurrentRegister();
                if (rtm.getParameterFactory().readProfileType().isEvoHop()) {
                    rtm.getLogger().log(Level.WARNING, "Reading of current indexes is not supported by the EvoHop module");
                    throw new NoSuchRegisterException("Reading of current indexes is not supported by the EvoHop module");
                }
                if (port > rtm.getParameterFactory().readOperatingMode().readNumberOfPorts()) {
                    rtm.getLogger().log(Level.WARNING, "Port " + port + " is not supported by the module");
                    throw new NoSuchRegisterException("Port " + port + " is not supported by the module");
                }
                if (Integer.MAX_VALUE == currentRegister.getCurrentReading(port)) {
                    rtm.getLogger().log(Level.WARNING, "No index logged yet for port " + port);
                    throw new NoSuchRegisterException("No index logged yet for port " + port);      //Indicated by value 0x7FFFFFFF
                }
                RtmUnit rtmUnit = currentRegister.getGenericHeader().getRtmUnit(port - 1, rtmFactory);
                return new RegisterValue(obisCode, new Quantity(currentRegister.getCurrentReading(port) * rtmUnit.getMultiplier(), rtmUnit.getUnit()));
            } else if (isPulseWeightReadout(obisCode)) {
                if (!rtm.getParameterFactory().readProfileType().isPulse()) {
                    rtm.getLogger().log(Level.WARNING, "Reading the pulse weight is only supported by modules managing pulse registers");
                    throw new NoSuchRegisterException("Reading the pulse weight is only supported by modules managing pulse registers");
                }
                int inputChannel = (obisCode.getB());
                PulseWeight pulseWeight = rtm.getParameterFactory().readPulseWeight(inputChannel);
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(pulseWeight.getMultiplier()), pulseWeight.getUnit()));
            } else if (isEncoderUnitReadout(obisCode)) {
                if (!rtm.getParameterFactory().readProfileType().isEncoder()) {
                    rtm.getLogger().log(Level.WARNING, "Reading the encoder unit is only supported by modules with an encoder profile");
                    throw new NoSuchRegisterException("Reading the encoder unit is only supported by modules with an encoder profile");
                }
                int inputChannel = (obisCode.getB());
                EncoderUnit encoderUnit = rtm.getParameterFactory().readEncoderUnit(inputChannel);
                return new RegisterValue(obisCode, new Quantity(1, encoderUnit.getUnit()));
            } else if (isTOUBucketTotalizer(obisCode)) {
                int port = obisCode.getB();
                int bucket = obisCode.getE() - 53;
                int numberOfPorts = rtm.getParameterFactory().readOperatingMode().readNumberOfPorts();
                if (port > numberOfPorts) {
                    rtm.getLogger().log(Level.WARNING, "Port " + port + " is not supported by the module");
                    throw new NoSuchRegisterException("Port " + port + " is not supported by the module");
                }
                ReadTOUBuckets bucketTotalizers = rtm.getRadioCommandFactory().readTOUBuckets();
                int value = bucketTotalizers.getListOfAllTotalizers().get(port - 1).getTOUBucketsTotalizers()[bucket];
                RtmUnit unit = bucketTotalizers.getGenericHeader().getRtmUnit(port - 1, rtmFactory);
                return new RegisterValue(obisCode, new Quantity(value * unit.getMultiplier(), unit.getUnit()));
            } else {
                rtm.getLogger().log(Level.WARNING, "Register with obiscode [" + obisCode + "] is not supported");
                throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] is not supported");
            }
        } catch (IOException e) {
            if (!(e instanceof NoSuchRegisterException)) {
                rtm.getLogger().log(Level.WARNING, "Register with obiscode [" + obisCode + "] timed out: " + e.getMessage());
            }
            throw e;
        }
    }

    private boolean isTOUBucketTotalizer(ObisCode obisCode) {
        return ((obisCode.getC() == 96) && (obisCode.getE() > 52) && (obisCode.getE() < 59));
    }

    private boolean isPulseWeightReadout(ObisCode obisCode) {
        return (obisCode.equals(OBISCODE_PULSEWEIGHT_A) || obisCode.equals(OBISCODE_PULSEWEIGHT_B) || obisCode.equals(OBISCODE_PULSEWEIGHT_C) || obisCode.equals(OBISCODE_PULSEWEIGHT_D));
    }

    private boolean isEncoderUnitReadout(ObisCode obisCode) {
        return (obisCode.equals(OBISCODE_ENCODERUNIT_A) || obisCode.equals(OBISCODE_ENCODERUNIT_B));
    }

    private boolean isInputPulseRegister(ObisCode obisCode) {
        return ((obisCode.getA() == 1) &&
                ((obisCode.getB() < 5) && (obisCode.getB()) > 0) &&
                (obisCode.getC() == 82) &&
                (obisCode.getD() == 8) &&
                (obisCode.getE() == 0));
    }

    private boolean isCurrentIndexReading(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 255);
    }
}