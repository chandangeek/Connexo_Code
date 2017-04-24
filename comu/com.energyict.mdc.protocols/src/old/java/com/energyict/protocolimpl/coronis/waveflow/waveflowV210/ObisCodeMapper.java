package com.energyict.protocolimpl.coronis.waveflow.waveflowV210;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.waveflow.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ExtendedIndexReading;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.Read4DailySegmentsParameters;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.ReadCumulativeFlowVolumeParameters;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.ReadDateOfInstallation;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.ReadPeakFlowData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    public static final Unit BILLABLE_UNIT = Unit.get(BaseUnit.CUBICMETER);
    public static final Unit SMALL_BILLABLE_UNIT = Unit.get(BaseUnit.LITER);

    public static final ObisCode OBISCODE_INDEXA = ObisCode.fromString("1.1.82.8.0.255");
    public static final ObisCode OBISCODE_INDEXB = ObisCode.fromString("1.2.82.8.0.255");
    public static final ObisCode OBISCODE_INDEXC = ObisCode.fromString("1.3.82.8.0.255");
    public static final ObisCode OBISCODE_INDEXD = ObisCode.fromString("1.4.82.8.0.255");

    public static final ObisCode OBISCODE_BILLING_INDEXA = ObisCode.fromString("1.1.82.8.0.0");
    public static final ObisCode OBISCODE_BILLING_INDEXB = ObisCode.fromString("1.2.82.8.0.0");
    public static final ObisCode OBISCODE_BILLING_INDEXC = ObisCode.fromString("1.3.82.8.0.0");
    public static final ObisCode OBISCODE_BILLING_INDEXD = ObisCode.fromString("1.4.82.8.0.0");

    public static final ObisCode OBISCODE_FEATURE_DATA = ObisCode.fromString("0.0.96.5.3.255");
    public static final ObisCode OBISCODE_TARIFF_MODE = ObisCode.fromString("0.0.96.0.70.255");
    public static final ObisCode OBISCODE_DURATION = ObisCode.fromString("0.0.96.0.71.255");
    public static final ObisCode OBISCODE_STARTHOUR = ObisCode.fromString("0.0.96.0.72.255");
    public static final ObisCode OBISCODE_STARTMINUTE = ObisCode.fromString("0.0.96.0.73.255");
    public static final ObisCode OBISCODE_STARTMONTH = ObisCode.fromString("0.0.96.0.74.255");
    public static final ObisCode OBISCODE_STARTDAY = ObisCode.fromString("0.0.96.0.75.255");
    public static final ObisCode OBISCODE_NUMBER_OF_LOG_BLOCKS = ObisCode.fromString("0.0.96.0.76.255");
    public static final ObisCode OBISCODE_CURRENT_LOG_BLOCK_NUMBER = ObisCode.fromString("0.0.96.0.77.255");
    public static final ObisCode OBISCODE_TARIFF_PERIOD_MODE = ObisCode.fromString("0.0.96.0.78.255");
    public static final ObisCode OBISCODE_RB1 = ObisCode.fromString("0.0.96.0.79.255");
    public static final ObisCode OBISCODE_RB2 = ObisCode.fromString("0.0.96.0.80.255");

    public static final ObisCode OBISCODE_ACCUMULATIVE_VOLUME_MEASURED_TARIFFPERIOD1 = ObisCode.fromString("8.0.1.1.0.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK1_LASTPERIOD = ObisCode.fromString("8.0.1.0.1.0");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK2_LASTPERIOD = ObisCode.fromString("8.0.1.0.2.0");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK3_LASTPERIOD = ObisCode.fromString("8.0.1.0.3.0");

    public static final ObisCode OBISCODE_PEAKFLOW_MONITORING_PERIOD = ObisCode.fromString("0.0.96.0.81.255");
    public static final ObisCode OBISCODE_PEAKFLOW_DAY_OF_WEEK = ObisCode.fromString("0.0.96.0.82.255");
    public static final ObisCode OBISCODE_PEAKFLOW_WEEK_OF_YEAR = ObisCode.fromString("0.0.96.0.83.255");
    public static final ObisCode OBISCODE_DATE_OF_INSTALLATION = ObisCode.fromString("0.0.96.0.101.255");

    public static final ObisCode OBISCODE_PEAKFLOW = ObisCode.fromString("8.0.2.5.0.255");
    public static final ObisCode OBISCODE_PEAKFLOW_LAST_YEAR = ObisCode.fromString("8.0.2.5.0.0");
    public static final ObisCode OBISCODE_CURRENT_FLOW = ObisCode.fromString("8.0.2.0.0.255");

    public static final ObisCode OBISCODE_CUSTOMERNUMBER = ObisCode.fromString("0.0.96.0.84.255");
    public static final ObisCode OBISCODE_TRANSMISSION_COUNT = ObisCode.fromString("0.0.96.0.85.255");
    public static final ObisCode OBISCODE_NO_FLOW_TIME = ObisCode.fromString("0.0.96.0.86.255");

    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT1 = ObisCode.fromString("8.0.1.1.1.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT2 = ObisCode.fromString("8.0.1.1.2.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT3 = ObisCode.fromString("8.0.1.1.3.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT4 = ObisCode.fromString("8.0.1.1.4.255");

    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_1 = ObisCode.fromString("8.0.1.0.1.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_2 = ObisCode.fromString("8.0.1.0.2.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_3 = ObisCode.fromString("8.0.1.0.3.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_4 = ObisCode.fromString("8.0.1.0.4.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_5 = ObisCode.fromString("8.0.1.0.5.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_6 = ObisCode.fromString("8.0.1.0.6.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_7 = ObisCode.fromString("8.0.1.0.7.255");

    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND1_LOW_THRESHOLD = ObisCode.fromString("0.1.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND2_LOW_THRESHOLD = ObisCode.fromString("0.2.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND3_LOW_THRESHOLD = ObisCode.fromString("0.3.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND4_LOW_THRESHOLD = ObisCode.fromString("0.4.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND5_LOW_THRESHOLD = ObisCode.fromString("0.5.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND6_LOW_THRESHOLD = ObisCode.fromString("0.6.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND7_LOW_THRESHOLD = ObisCode.fromString("0.7.96.0.87.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND1_HIGH_THRESHOLD = ObisCode.fromString("0.1.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND2_HIGH_THRESHOLD = ObisCode.fromString("0.2.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND3_HIGH_THRESHOLD = ObisCode.fromString("0.3.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND4_HIGH_THRESHOLD = ObisCode.fromString("0.4.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND5_HIGH_THRESHOLD = ObisCode.fromString("0.5.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND6_HIGH_THRESHOLD = ObisCode.fromString("0.6.96.0.88.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND7_HIGH_THRESHOLD = ObisCode.fromString("0.7.96.0.88.255");

    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_PERIOD = ObisCode.fromString("0.0.96.0.89.255");
    public static final ObisCode OBISCODE_CUMULATIVE_VOLUME_BAND_START_MOMENT = ObisCode.fromString("0.0.96.0.90.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_PERIOD = ObisCode.fromString("0.0.96.0.95.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_START_MOMENT = ObisCode.fromString("0.0.96.0.96.255");

    public static final ObisCode OBISCODE_DAILY_SEGMENT_1_STARTHOUR = ObisCode.fromString("0.1.96.0.91.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_1_STOPHOUR = ObisCode.fromString("0.1.96.0.92.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_1_STARTMINUTE = ObisCode.fromString("0.1.96.0.93.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_1_STOPMINUTE = ObisCode.fromString("0.1.96.0.94.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_2_STARTHOUR = ObisCode.fromString("0.2.96.0.91.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_2_STOPHOUR = ObisCode.fromString("0.2.96.0.92.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_2_STARTMINUTE = ObisCode.fromString("0.2.96.0.93.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_2_STOPMINUTE = ObisCode.fromString("0.2.96.0.94.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_3_STARTHOUR = ObisCode.fromString("0.3.96.0.91.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_3_STOPHOUR = ObisCode.fromString("0.3.96.0.92.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_3_STARTMINUTE = ObisCode.fromString("0.3.96.0.93.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_3_STOPMINUTE = ObisCode.fromString("0.3.96.0.94.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_4_STARTHOUR = ObisCode.fromString("0.4.96.0.91.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_4_STOPHOUR = ObisCode.fromString("0.4.96.0.92.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_4_STARTMINUTE = ObisCode.fromString("0.4.96.0.93.255");
    public static final ObisCode OBISCODE_DAILY_SEGMENT_4_STOPMINUTE = ObisCode.fromString("0.4.96.0.94.255");

    public static final ObisCode OBISCODE_OVERSPEED_THRESHOLD = ObisCode.fromString("0.0.96.0.97.255");
    public static final ObisCode OBISCODE_OVERSPEED_TIME = ObisCode.fromString("0.0.96.0.98.255");

    public static final ObisCode OBISCODE_CUMULATIVE_FLOW_VOLUME_BANDS_ENABLED = ObisCode.fromString("0.0.96.0.99.255");
    public static final ObisCode OBISCODE_CUMULATIVE_FLOW_DAILY_4_SEGMENTS_ENABLED = ObisCode.fromString("0.0.96.0.100.255");


    static {
        // specific waveflow registers
        registerMaps.put(OBISCODE_INDEXA, "Input A index");
        registerMaps.put(OBISCODE_INDEXB, "Input B index");
        registerMaps.put(OBISCODE_INDEXC, "Input C index");
        registerMaps.put(OBISCODE_INDEXD, "Input D index");

        registerMaps.put(OBISCODE_BILLING_INDEXA, "Last Billing period index for input A");
        registerMaps.put(OBISCODE_BILLING_INDEXB, "Last Billing period index for input B");
        registerMaps.put(OBISCODE_BILLING_INDEXC, "Last Billing period index for input C");
        registerMaps.put(OBISCODE_BILLING_INDEXD, "Last Billing period index for input D");

        registerMaps.put(OBISCODE_FEATURE_DATA, "Feature data");
        registerMaps.put(OBISCODE_TARIFF_MODE, "Tariff mode (0: time of use, 1: rising blocks)");
        registerMaps.put(OBISCODE_DURATION, "Tariff period duration");
        registerMaps.put(OBISCODE_STARTHOUR, "Tariff start hour");
        registerMaps.put(OBISCODE_STARTMINUTE, "Tariff start minute");
        registerMaps.put(OBISCODE_STARTMONTH, "Tariff start month (for seasonal tariff)");
        registerMaps.put(OBISCODE_STARTDAY, "Tariff start day");
        registerMaps.put(OBISCODE_NUMBER_OF_LOG_BLOCKS, "Number of log blocks");
        registerMaps.put(OBISCODE_CURRENT_LOG_BLOCK_NUMBER, "Current log block number");
        registerMaps.put(OBISCODE_TARIFF_PERIOD_MODE, "Tariff period mode (0 = day, 1 = seasonal or months)");
        registerMaps.put(OBISCODE_RB1, "Log block 1 volume threshold");
        registerMaps.put(OBISCODE_RB2, "Log block 2 volume threshold");

        registerMaps.put(OBISCODE_ACCUMULATIVE_VOLUME_MEASURED_TARIFFPERIOD1, "Accumulative volume measured in tariff period");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK1_LASTPERIOD, "Cumulative volume measured in block 1, last period");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK2_LASTPERIOD, "Cumulative volume measured in block 2, last period");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK3_LASTPERIOD, "Cumulative volume measured in block 3, last period");

        registerMaps.put(OBISCODE_PEAKFLOW_WEEK_OF_YEAR, "Week of year for peak flow detection");
        registerMaps.put(OBISCODE_PEAKFLOW_DAY_OF_WEEK, "Day of week for peak flow detection");
        registerMaps.put(OBISCODE_PEAKFLOW_MONITORING_PERIOD, "Monitoring period for peak flow detection");
        registerMaps.put(OBISCODE_DATE_OF_INSTALLATION, "Date of installation");

        registerMaps.put(OBISCODE_PEAKFLOW, "Current defined period peak flow");
        registerMaps.put(OBISCODE_PEAKFLOW_LAST_YEAR, "Last year peak flow");
        registerMaps.put(OBISCODE_CURRENT_FLOW, "Current flow rate");

        registerMaps.put(OBISCODE_CUSTOMERNUMBER, "Customer number");
        registerMaps.put(OBISCODE_TRANSMISSION_COUNT, "Frame transmission count");
        registerMaps.put(OBISCODE_NO_FLOW_TIME, "Cumulative no flow time");

        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT1, "Cumulative volume in daily segment 1");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT2, "Cumulative volume in daily segment 2");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT3, "Cumulative volume in daily segment 3");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT4, "Cumulative volume in daily segment 4");

        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_1, "Cumulative volume in band 1");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_2, "Cumulative volume in band 2");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_3, "Cumulative volume in band 3");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_4, "Cumulative volume in band 4");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_5, "Cumulative volume in band 5");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_6, "Cumulative volume in band 6");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_7, "Cumulative volume in band 7");

        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND1_LOW_THRESHOLD, "Cumulative volume band 1 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND2_LOW_THRESHOLD, "Cumulative volume band 2 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND3_LOW_THRESHOLD, "Cumulative volume band 3 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND4_LOW_THRESHOLD, "Cumulative volume band 4 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND5_LOW_THRESHOLD, "Cumulative volume band 5 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND6_LOW_THRESHOLD, "Cumulative volume band 6 low threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND7_LOW_THRESHOLD, "Cumulative volume band 7 low threshold");

        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND1_HIGH_THRESHOLD, "Cumulative volume band 1 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND2_HIGH_THRESHOLD, "Cumulative volume band 2 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND3_HIGH_THRESHOLD, "Cumulative volume band 3 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND4_HIGH_THRESHOLD, "Cumulative volume band 4 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND5_HIGH_THRESHOLD, "Cumulative volume band 5 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND6_HIGH_THRESHOLD, "Cumulative volume band 6 high threshold");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND7_HIGH_THRESHOLD, "Cumulative volume band 7 high threshold");

        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_PERIOD, "Cumulative volume band period");
        registerMaps.put(OBISCODE_CUMULATIVE_VOLUME_BAND_START_MOMENT, "Start date for the cumulative volume bands");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_PERIOD, "Daily segments period");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_START_MOMENT, "Start date for the daily segments");

        registerMaps.put(OBISCODE_DAILY_SEGMENT_1_STARTHOUR, "Daily segment 1, start hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_1_STOPHOUR, "Daily segment 1, stop hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_1_STARTMINUTE, "Daily segment 1, start minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_1_STOPMINUTE, "Daily segment 1, stop minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_2_STARTHOUR, "Daily segment 2, start hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_2_STOPHOUR, "Daily segment 2, stop hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_2_STARTMINUTE, "Daily segment 2, start minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_2_STOPMINUTE, "Daily segment 2, stop minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_3_STARTHOUR, "Daily segment 3, start hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_3_STOPHOUR, "Daily segment 3, stop hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_3_STARTMINUTE, "Daily segment 3, start minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_3_STOPMINUTE, "Daily segment 3, stop minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_4_STARTHOUR, "Daily segment 4, start hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_4_STOPHOUR, "Daily segment 4, stop hour");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_4_STARTMINUTE, "Daily segment 4, start minute");
        registerMaps.put(OBISCODE_DAILY_SEGMENT_4_STOPMINUTE, "Daily segment 4, stop minute");

        registerMaps.put(OBISCODE_OVERSPEED_THRESHOLD, "Threshold for over speed");
        registerMaps.put(OBISCODE_OVERSPEED_TIME, "Time for over speed alarm");

        registerMaps.put(OBISCODE_CUMULATIVE_FLOW_VOLUME_BANDS_ENABLED, "Cumulative flow volume (7 bands) enabled");
        registerMaps.put(OBISCODE_CUMULATIVE_FLOW_DAILY_4_SEGMENTS_ENABLED, "Cumulative flow daily (4 segments) enabled");


    }

    private WaveFlowV210 waveFlowV1;

    /**
     * Creates a new instance of ObisCodeMapper
     *
     * @param waveFlowV1 the protocol
     */
    public ObisCodeMapper(final WaveFlowV210 waveFlowV1) {
        this.waveFlowV1 = waveFlowV1;
    }

    final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            waveFlowV1.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        strBuilder.append(waveFlowV1.getCommonObisCodeMapper().getRegisterExtendedLogging());
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        if (info != null) {
            return new RegisterInfo(info);
        } else {
            return CommonObisCodeMapper.getRegisterInfo(obisCode);
        }
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {

            //Uses the pulse weight to convert the received pulse amount in liters
            if (isCurrentIndexReading(obisCode)) {
                int channel = obisCode.getB() - 1;
                if (channel > (waveFlowV1.getNumberOfChannels() - 1)) {
                    throw new NoSuchRegisterException("This channel is not supported");
                }

                PulseWeight pulseWeight = waveFlowV1.getPulseWeight(channel, true);
                BigDecimal currentIndexValue = new BigDecimal(pulseWeight.getWeight() * waveFlowV1.getRadioCommandFactory().readCurrentReading().getReadings()[channel]);
                return new RegisterValue(obisCode, new Quantity(currentIndexValue, pulseWeight.getUnit()), new Date());

                // Billing data request for inputs A ... D
            } else if (isLastBillingPeriodIndexReading(obisCode)) {
                int channel = obisCode.getB() - 1;
                PulseWeight pulseWeight = waveFlowV1.getPulseWeight(channel);
                ExtendedIndexReading extendedIndexReadingConfiguration = waveFlowV1.getRadioCommandFactory().readExtendedIndexConfiguration();
                if (channel > (waveFlowV1.getNumberOfChannels() - 1)) {
                    throw new NoSuchRegisterException("No billing data available this channel");
                }
                int value = extendedIndexReadingConfiguration.getIndexOfLastMonth(channel);
                if (value == -1) {
                    waveFlowV1.getLogger().log(Level.WARNING, "No billing data available yet, values are 0xFFFFFFFF");
                    throw new NoSuchRegisterException("No billing data available yet");
                }
                BigDecimal lastMonthsIndexValue = new BigDecimal(pulseWeight.getWeight() * value);
                Date toDate = extendedIndexReadingConfiguration.getDateOfLastMonthsEnd();
                return new RegisterValue(obisCode, new Quantity(lastMonthsIndexValue, pulseWeight.getUnit()), toDate, toDate);

            } else if (OBISCODE_FEATURE_DATA.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readFeatureData().getFeatureData();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_CUMULATIVE_FLOW_DAILY_4_SEGMENTS_ENABLED.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readFeatureData().is4DaySegmentsEnabled();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_CUMULATIVE_FLOW_VOLUME_BANDS_ENABLED.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readFeatureData().is7BandsEnabled();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_OVERSPEED_THRESHOLD.equals(obisCode)) {
                PulseWeight pulseWeight = waveFlowV1.getPulseWeight(0, true);
                int value = waveFlowV1.getRadioCommandFactory().readOverSpeedParameters().getSpeedThreshold() * 3600 * pulseWeight.getWeight();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.CUBICMETERPERHOUR, pulseWeight.getUnitScaler() - 3)), new Date());
            } else if (OBISCODE_OVERSPEED_TIME.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readOverSpeedParameters().getTimeForOverSpeedAlarm();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.SECOND)), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_PERIOD.equals(obisCode)) {
                return new RegisterValue(obisCode, waveFlowV1.getRadioCommandFactory().readBandPeriod(), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_PERIOD.equals(obisCode)) {
                Quantity period = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getPeriodQuantity();
                return new RegisterValue(obisCode, period, new Date());
            } else if (OBISCODE_DAILY_SEGMENT_START_MOMENT.equals(obisCode)) {
                Read4DailySegmentsParameters segmentsParameters = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), segmentsParameters.getDate(), null, new Date(), new Date(), 0, segmentsParameters.getDescription());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_START_MOMENT.equals(obisCode)) {
                ReadCumulativeFlowVolumeParameters parameters = waveFlowV1.getRadioCommandFactory().readBandParameters();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), parameters.getDate(), null, new Date(), new Date(), 0, parameters.getDescription());

            } else if (OBISCODE_DAILY_SEGMENT_1_STARTHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartHour(0);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_2_STARTHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartHour(1);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_3_STARTHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartHour(2);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_4_STARTHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartHour(3);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());

            } else if (OBISCODE_DAILY_SEGMENT_1_STARTMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartMinute(0);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_2_STARTMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartMinute(1);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_3_STARTMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartMinute(2);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_4_STARTMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStartMinute(3);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());

            } else if (OBISCODE_DAILY_SEGMENT_1_STOPHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopHour(0);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_2_STOPHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopHour(1);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_3_STOPHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopHour(2);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_4_STOPHOUR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopHour(3);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());

            } else if (OBISCODE_DAILY_SEGMENT_1_STOPMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopMinute(0);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_2_STOPMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopMinute(1);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_3_STOPMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopMinute(2);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_DAILY_SEGMENT_4_STOPMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readDailySegmentsParameters().getStopMinute(3);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT1.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInDailySegment(0);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT2.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInDailySegment(1);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT3.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInDailySegment(2);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_DAILY_SEGMENT4.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInDailySegment(3);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_1.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(0);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_2.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(1);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_3.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(2);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_4.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(3);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_5.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(4);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_6.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(5);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND_7.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCumulativeVolumeInBand(6);
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND1_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(0);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND2_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(1);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND3_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(2);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND4_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(3);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND5_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(4);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND6_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(5);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND7_LOW_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandLowThreshold(6);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND1_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(0);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND2_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(1);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND3_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(2);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND4_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(3);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND5_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(4);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND6_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(5);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_BAND7_HIGH_THRESHOLD.equals(obisCode)) {
                Unit unit = waveFlowV1.getRadioCommandFactory().readBandUnit();
                int value = waveFlowV1.getRadioCommandFactory().readBandHighThreshold(6);
                return new RegisterValue(obisCode, new Quantity(value, unit), new Date());

            } else if (OBISCODE_NO_FLOW_TIME.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readNoFlowTime();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DAY)), new Date());
            } else if (OBISCODE_TRANSMISSION_COUNT.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readFrameTransmissionCount();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_TARIFF_MODE.equals(obisCode)) {
                int mode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() >> 7;
                return new RegisterValue(obisCode, new Quantity(mode, Unit.get("")), new Date());
            } else if (OBISCODE_DURATION.equals(obisCode)) {
                int mode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() >> 7;
                Unit unit = Unit.get("");
                if (mode == 0) {
                    int subMode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x03;
                    if (subMode == 0) {
                        unit = Unit.get(BaseUnit.HOUR);
                    }
                    if (subMode == 1) {
                        unit = Unit.get(BaseUnit.DAY);
                    }
                } else {
                    int subMode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x03;
                    if (subMode == 1) {
                        unit = Unit.get(BaseUnit.DAY);
                    }
                    if (subMode == 2) {
                        unit = Unit.get(BaseUnit.MONTH);
                    }
                }
                int duration = waveFlowV1.getRadioCommandFactory().readTariffSettings().getDuration();
                return new RegisterValue(obisCode, new Quantity(duration, unit), new Date());
            } else if (OBISCODE_STARTHOUR.equals(obisCode)) {
                int mode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() >> 7;
                int value;
                if (mode == 0) {
                    value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartHourOrMonth();     //Time of use tariffs
                } else {
                    value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartTime();            //Rising block tariffs
                }
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_STARTMINUTE.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartMinuteOrDay();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_CUSTOMERNUMBER.equals(obisCode)) {
                String number = waveFlowV1.getRadioCommandFactory().readCustomerNumber();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), null, new Date(), new Date(), 0, number);
            } else if (OBISCODE_DATE_OF_INSTALLATION.equals(obisCode)) {
                ReadDateOfInstallation dateOfInstallation = waveFlowV1.getRadioCommandFactory().readInstallationDate();
                return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), dateOfInstallation.getDate(), null, new Date(), new Date(), 0, dateOfInstallation.getDescription());
            } else if (OBISCODE_PEAKFLOW.equals(obisCode)) {
                ReadPeakFlowData peakFlowData = waveFlowV1.getRadioCommandFactory().readPeakFlowData();
                if (peakFlowData.getCurrentPeriodPeakFlow() == -1) {
                    waveFlowV1.getLogger().log(Level.WARNING, "No peak flow data available");
                    throw new NoSuchRegisterException("No peak flow data available");
                }
                return new RegisterValue(obisCode, new Quantity(peakFlowData.getCurrentPeriodPeakFlow(), Unit.get(BaseUnit.CUBICMETERPERHOUR, -3)), peakFlowData.getCurrentPeriodPeakFlowTimeStamp());
            } else if (OBISCODE_CURRENT_FLOW.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readCurrentFlowRate();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.CUBICMETERPERHOUR, -3)), new Date());
            } else if (OBISCODE_PEAKFLOW_LAST_YEAR.equals(obisCode)) {
                ReadPeakFlowData peakFlowData = waveFlowV1.getRadioCommandFactory().readPeakFlowData();
                if (peakFlowData.getLastYearPeakFlow() == -1) {
                    waveFlowV1.getLogger().log(Level.WARNING, "No peak flow data available");
                    throw new NoSuchRegisterException("No peak flow data available");
                }
                return new RegisterValue(obisCode, new Quantity(peakFlowData.getLastYearPeakFlow(), Unit.get(BaseUnit.CUBICMETERPERHOUR, -3)), peakFlowData.getLastYearPeakFlowTimeStamp());
            } else if (OBISCODE_PEAKFLOW_MONITORING_PERIOD.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readPeakFlowSettings().getMonitoringTimePeriod();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get(BaseUnit.DAY)), new Date());
            } else if (OBISCODE_PEAKFLOW_DAY_OF_WEEK.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readPeakFlowSettings().getStartTime();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_PEAKFLOW_WEEK_OF_YEAR.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readPeakFlowSettings().getWeekOfYear();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_STARTMONTH.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartHourOrMonth();
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            } else if (OBISCODE_NUMBER_OF_LOG_BLOCKS.equals(obisCode)) {
                int value = (waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x60) >> 5;
                return new RegisterValue(obisCode, new Quantity(value + 1, Unit.get("")), new Date());
            } else if (OBISCODE_CURRENT_LOG_BLOCK_NUMBER.equals(obisCode)) {
                int value = (waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x18) >> 3;
                return new RegisterValue(obisCode, new Quantity(value + 1, Unit.get("")), new Date());
            } else if (OBISCODE_ACCUMULATIVE_VOLUME_MEASURED_TARIFFPERIOD1.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getMeasuredVolumeInTariffPeriod();
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());

            } else if (OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK1_LASTPERIOD.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getCumulativeMeasuredVolumeInBlock1();
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK2_LASTPERIOD.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getCumulativeMeasuredVolumeInBlock2();
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());
            } else if (OBISCODE_CUMULATIVE_VOLUME_MEASURED_BLOCK3_LASTPERIOD.equals(obisCode)) {
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getCumulativeMeasuredVolumeInBlock3();
                return new RegisterValue(obisCode, new Quantity(value, SMALL_BILLABLE_UNIT), new Date());

            } else if (OBISCODE_RB1.equals(obisCode)) {
                int unitScale = (waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x04) >> 2;
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getRB1();
                return new RegisterValue(obisCode, new Quantity(value, unitScale == 0 ? SMALL_BILLABLE_UNIT : BILLABLE_UNIT), new Date());
            } else if (OBISCODE_RB2.equals(obisCode)) {
                int unitScale = (waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x04) >> 2;
                int value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getRB2();
                return new RegisterValue(obisCode, new Quantity(value, unitScale == 0 ? SMALL_BILLABLE_UNIT : BILLABLE_UNIT), new Date());
            } else if (OBISCODE_TARIFF_PERIOD_MODE.equals(obisCode)) {
                int mode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() >> 7;
                int value = (waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() & 0x03);
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date(), null, new Date(), new Date(), 0, getTariffModeDescription(value, mode));
            } else if (OBISCODE_STARTDAY.equals(obisCode)) {
                int mode = waveFlowV1.getRadioCommandFactory().readTariffSettings().getTariffMode() >> 7;
                int value;
                if (mode == 0) {
                    value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartMinuteOrDay();     //Time of use tariffs
                } else {
                    value = waveFlowV1.getRadioCommandFactory().readTariffSettings().getStartTime();            //Rising block tariffs
                }
                return new RegisterValue(obisCode, new Quantity(value, Unit.get("")), new Date());
            }

            // Other cases
            else {
                return waveFlowV1.getCommonObisCodeMapper().getRegisterValue(obisCode);
            }

        } catch (IOException e) {
            if (!(e instanceof NoSuchRegisterException)) {
                waveFlowV1.getLogger().log(Level.SEVERE, "Error getting [" + obisCode + "]: timeout, " + e.getMessage());
            }
            throw e;
        }
    }

    private String getTariffModeDescription(int value, int mode) {
        if (mode == 0) {
            if (value == 0) {
                return "Daily tariff";
            }
            if (value == 1) {
                return "Seasonal tariff";
            }
        }
        if (mode == 1) {
            if (value == 1) {
                return "Days mode";
            }
            if (value == 2) {
                return "Months mode";
            }
        }
        return "Unknown tariff mode";
    }

    /**
     * Checks if the obis code is of the form 1.b.82.8.0.f       (indicates an input pulse channel)
     * Where b = 1, 2, 3 or 4 and f = 0 or 255.
     *
     * @param obisCode the obis code
     * @return true or false
     */
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

    private boolean isLastBillingPeriodIndexReading(ObisCode obisCode) {
        return isInputPulseRegister(obisCode) && (obisCode.getF() == 0);
    }
}
