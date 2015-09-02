package test.com.energyict.protocolimplv2.coronis.waveflow.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.OperatingMode;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.ProfileType;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.PulseWeight;

import java.math.BigDecimal;

public class CommonObisCodeMapper {

    public static final ObisCode OBISCODE_APPLICATION_STATUS = ObisCode.fromString("0.0.96.5.2.255");
    public static final ObisCode OBISCODE_OPERATION_MODE = ObisCode.fromString("0.0.96.5.1.255");
    public static final ObisCode OBISCODE_REMAINING_BATTERY = ObisCode.fromString("0.0.96.6.0.255");
    public static final ObisCode OBISCODE_RELAYED_FRAMES = ObisCode.fromString("0.1.96.0.52.255");
    public static final ObisCode OBISCODE_RSSI_LEVEL = ObisCode.fromString("0.0.96.0.63.255");
    private static final int MULTIPLIER = 256;
    private static final ObisCode OBISCODE_PROFILE_TYPE = ObisCode.fromString("0.0.96.0.50.255");     //Waveflow specific register, E >= 50
    private static final ObisCode OBISCODE_PULSEWEIGHT_A = ObisCode.fromString("0.1.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_B = ObisCode.fromString("0.2.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_C = ObisCode.fromString("0.3.96.0.51.255");
    private static final ObisCode OBISCODE_PULSEWEIGHT_D = ObisCode.fromString("0.4.96.0.51.255");
    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode OBISCODE_SENT_FRAMES = ObisCode.fromString("0.0.96.0.52.255");
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

    private WaveFlow waveFlow;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlow the protocol containing all settings
     */
    public CommonObisCodeMapper(final WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public CollectedRegister getRegisterValue(OfflineRegister register) {
        ObisCode obisCode = register.getObisCode();
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterIdentifierById(register.getRegisterId(), register.getObisCode()));

        OperatingMode operatingMode = waveFlow.getParameterFactory().readOperatingMode();
        if (obisCode.equals(OBISCODE_REMAINING_BATTERY)) {
            double level = waveFlow.getParameterFactory().readBatteryLifeDurationCounter();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(level), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_APPLICATION_STATUS)) {
            int status = waveFlow.getParameterFactory().readApplicationStatus();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(status), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_PROFILEDATA_INTERVAL)) {
            int interval = waveFlow.getParameterFactory().getProfileIntervalInSeconds();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(interval), Unit.get(BaseUnit.SECOND)));
        } else if (obisCode.equals(OBISCODE_DATALOGGING_STARTHOUR)) {
            int hour = waveFlow.getParameterFactory().readStartHourOfMeasurement();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(hour), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_DATALOGGING_STARTMINUTE)) {
            if (waveFlow.isV1()) {
                collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(this, "The WaveFlow V1 module doesn't support the data logging start minute parameter"));
            } else {
                int minute = waveFlow.getParameterFactory().readStartMinuteOfMeasurement();
                collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(minute), Unit.get(BaseUnit.MINUTE)));
            }
        } else if (obisCode.equals(OBISCODE_DATALOGGING_DAYOFWEEK)) {
            int day = waveFlow.getParameterFactory().readDayOfWeek();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(day), Unit.get(BaseUnit.DAY)));
        } else if (obisCode.equals(OBISCODE_LEAKAGE_MEASUREMENTSTEP)) {
            int step = waveFlow.getParameterFactory().readMeasurementStep();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(step), Unit.get(BaseUnit.MINUTE)));
        } else if (obisCode.equals(OBISCODE_WakeUpSystemStatusWord)) {
            int value = waveFlow.getParameterFactory().getWakeUpSystemStatusWord();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_DefaultWakeUpPeriod)) {
            int value = waveFlow.getParameterFactory().getDefaultWakeUpPeriod();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_StartTimeForTimeWindow1)) {
            int value = waveFlow.getParameterFactory().getStartTimeForTimeWindow1();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_WakeUpPeriodForTimeWindow1)) {
            int value = waveFlow.getParameterFactory().getWakeUpPeriodForTimeWindow1();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_StartTimeForTimeWindow2)) {
            int value = waveFlow.getParameterFactory().getStartTimeForTimeWindow2();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_WakeUpPeriodForTimeWindow2)) {
            int value = waveFlow.getParameterFactory().getWakeUpPeriodForTimeWindow2();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EnableTimeWindowsByDayOfWeek)) {
            int value = waveFlow.getParameterFactory().getEnableTimeWindowsByDayOfWeek();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EnableWakeUpPeriodsByDayOfWeek)) {
            int value = waveFlow.getParameterFactory().getEnableWakeUpPeriodsByDayOfWeek();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_ALARM_CONFIG_BYTE)) {
            int value = waveFlow.getParameterFactory().readAlarmConfigurationValue();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD1)) {
            int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD2)) {
            int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD3)) {
            int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(3);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_THRESHOLD4)) {
            int threshold = waveFlow.getParameterFactory().readResidualLeakageThreshold(4);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD1)) {
            int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD2)) {
            int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD3)) {
            int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(3);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_THRESHOLD4)) {
            int threshold = waveFlow.getParameterFactory().readExtremeLeakageThreshold(4);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD1)) {
            int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD2)) {
            int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD3)) {
            int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(3);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKAGE_PERIOD4)) {
            int period = waveFlow.getParameterFactory().readResidualLeakageDetectionPeriod(4);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD1)) {
            int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD2)) {
            int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD3)) {
            int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(3);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKAGE_PERIOD4)) {
            int period = waveFlow.getParameterFactory().readExtremeLeakageDetectionPeriod(4);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(period), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD1)) {
            int threshold = waveFlow.getParameterFactory().readSimpleBackflowThreshold(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_THRESHOLD2)) {
            int threshold = waveFlow.getParameterFactory().readSimpleBackflowThreshold(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD1)) {
            int threshold = waveFlow.getParameterFactory().readAdvancedBackflowThreshold(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_THRESHOLD2)) {
            int threshold = waveFlow.getParameterFactory().readAdvancedBackflowThreshold(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_PERIOD1)) {
            int threshold = waveFlow.getParameterFactory().readSimpleBackflowDetectionPeriod(1);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_SIMPLE_BACKFLOW_PERIOD2)) {
            int threshold = waveFlow.getParameterFactory().readSimpleBackflowDetectionPeriod(2);
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.HOUR)));
        } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_PERIOD1)) {
            int threshold = waveFlow.getParameterFactory().readAdvancedBackflowDetectionPeriod(1) * 10;     //Expressed in multiples of 10 minutes
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.MINUTE)));
        } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_PERIOD2)) {
            int threshold = waveFlow.getParameterFactory().readAdvancedBackflowDetectionPeriod(2) * 10;     //Expressed in multiples of 10 minutes
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(threshold), Unit.get(BaseUnit.MINUTE)));
        } else if (obisCode.equals(OBISCODE_RESIDUAL_LEAKDETECTION_ENABLED)) {
            int enabled = operatingMode.residualLeakDetectionIsEnabled() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_EXTREME_LEAKDETECTION_ENABLED)) {
            int enabled = operatingMode.extremeLeakDetectionIsEnabled() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_REEDFAULTDETECTION_ENABLED)) {
            int enabled = operatingMode.reedFaultDetectionIsEnabled() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_WIRECUTDETECTION_ENABLED)) {
            int enabled = operatingMode.wireCutDetectionIsEnabled() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_SIMPLEBACKFLOW_ENABLED)) {
            ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
            int enabled = profileType.supportsSimpleBackflowDetection() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_ADVANCED_BACKFLOW_ENABLED)) {
            ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
            int enabled = profileType.supportsAdvancedBackflowDetection() ? 1 : 0;
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(enabled), Unit.get("")), enabled == 1 ? "Enabled" : "Disabled");
        } else if (obisCode.equals(OBISCODE_LOGGING_MODE)) {
            int steps = operatingMode.dataLoggingSteps();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(steps), Unit.get("")), operatingMode.getLoggingDescription());
        } else if (obisCode.equals(OBISCODE_OPERATION_MODE)) {
            int mode = operatingMode.getOperationMode();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(mode), Unit.get("")));
        } else if (obisCode.equals(OBISCODE_FIRMWARE)) {
            collectedRegister.setCollectedData(waveFlow.getRadioCommandFactory().readFirmwareVersion().toString());
        } else if (obisCode.equals(OBISCODE_ELAPSED_DAYS)) {
            int nr = waveFlow.getParameterFactory().readElapsedDays();
            collectedRegister.setCollectedData(new Quantity(new BigDecimal(nr), Unit.get(BaseUnit.DAY)));
        } else if (obisCode.equals(OBISCODE_SENT_FRAMES)) {
            int nr = waveFlow.getParameterFactory().readNumberOfSentFrames();
            collectedRegister.setCollectedData(new Quantity(nr * MULTIPLIER, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RECEIVED_FRAMES)) {
            int nr = waveFlow.getParameterFactory().readNumberOfReceivedFrames();
            collectedRegister.setCollectedData(new Quantity(nr * MULTIPLIER, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_PROFILE_TYPE)) {
            ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
            collectedRegister.setCollectedData(new Quantity(BigDecimal.valueOf(profileType.getType()), Unit.get("")), profileType.getDescription());
        } else if (obisCode.equals(OBISCODE_TIME_DURATION_RX)) {
            int value = waveFlow.getParameterFactory().getTimeDurationRX();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get(BaseUnit.SECOND, -3)));
        } else if (obisCode.equals(OBISCODE_TIME_DURATION_TX)) {
            int value = waveFlow.getParameterFactory().getTimeDurationTX();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get(BaseUnit.SECOND, -3)));
        } else if (obisCode.equals(OBISCODE_NUMBER_OF_FRAME_RX)) {
            int value = waveFlow.getParameterFactory().getNumberOfFramesInRx();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_NUMBER_OF_FRAME_TX)) {
            int value = waveFlow.getParameterFactory().getNumberOfFramesInTx();
            collectedRegister.setCollectedData(new Quantity(value, Unit.get("")));
        } else if (obisCode.equals(OBISCODE_RSSI_LEVEL)) {
            double value = waveFlow.getRadioCommandFactory().readRSSILevel();
            collectedRegister.setCollectedData(new Quantity(value > 100 ? 100 : value, Unit.get("")));      //A percentage representing the saturation
        } else if (isPulseWeightReadout(obisCode)) {
            int inputChannel = (obisCode.getB());
            PulseWeight pulseWeight = waveFlow.getPulseWeight(inputChannel - 1, true);
            collectedRegister.setCollectedData(new Quantity(new BigDecimal(pulseWeight.getWeight()), pulseWeight.getUnit()));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(this, "Register with obiscode {0} is not supported by the protocol", obisCode.toString()));
        }
        return collectedRegister;
    }

    private boolean isPulseWeightReadout(ObisCode obisCode) {
        return (obisCode.equals(OBISCODE_PULSEWEIGHT_A) || obisCode.equals(OBISCODE_PULSEWEIGHT_B) || obisCode.equals(OBISCODE_PULSEWEIGHT_C) || obisCode.equals(OBISCODE_PULSEWEIGHT_D));
    }
}