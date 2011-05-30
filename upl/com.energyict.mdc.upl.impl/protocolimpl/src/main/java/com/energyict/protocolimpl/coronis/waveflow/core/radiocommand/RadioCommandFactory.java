package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.cbo.*;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.*;

import java.io.IOException;
import java.util.Date;

public class RadioCommandFactory {


    private WaveFlow waveFlow;

    // cached
    private FirmwareVersion firmwareVersion = null;
    private DailyConsumption dailyConsumption = null;
    private ReadTariffMode tariffMode = null;
    private ReadPeakFlowSettings peakFlowSettings = null;
    private ReadDateOfInstallation dateOfInstallation = null;
    private ReadCumulativeFlowVolumeParameters parameters7bands = null;
    private Read4DailySegmentsParameters dailySegmentsParameters = null;
    private ReadOverSpeedParameters overSpeedParameters = null;


    public static final Unit BILLABLE_UNIT = Unit.get(BaseUnit.CUBICMETER);
    public static final Unit SMALL_BILLABLE_UNIT = Unit.get(BaseUnit.LITER);

    public RadioCommandFactory(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    /**
     * Read the meter's current indexes (for all inputs: A, B, C, D)
     *
     * @return the current readings
     * @throws IOException when the communication failed
     */
    final public GlobalIndexReading readCurrentReading() throws IOException {
        GlobalIndexReading o = new GlobalIndexReading(waveFlow);
        o.set();
        return o;
    }

    final public ExtendedDataloggingTable readExtendedDataloggingTable(int indexChannel, final int nrOfValues, final Date toDate) throws IOException {
        ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow, indexChannel, nrOfValues, toDate);
        o.set();
        return o;
    }

    public void resetIndexes() throws IOException {
        WriteIndexes writeIndexes = new WriteIndexes(waveFlow);
        writeIndexes.setIndexA(0);
        writeIndexes.setIndexB(0);
        writeIndexes.setIndexC(0);
        writeIndexes.setIndexD(0);
        writeIndexes.set();
    }

    public void writeIndexes(int index, int input) throws IOException {
        WriteIndexes writeIndexes = new WriteIndexes(waveFlow, input);
        switch (input) {
            case 1:
                writeIndexes.setIndexA(index);
                break;
            case 2:
                writeIndexes.setIndexB(index);
                break;
            case 3:
                writeIndexes.setIndexC(index);
                break;
            case 4:
                writeIndexes.setIndexD(index);
                break;
        }
        writeIndexes.set();
    }

    public DataloggingTable readDataloggingTable(int channels) throws IOException {
        DataloggingTable dataloggingTable = new DataloggingTable(waveFlow);
        dataloggingTable.setChannels(channels);
        dataloggingTable.set();
        return dataloggingTable;
    }

    final public ExtendedDataloggingTable readExtendedDataloggingTable(int indexChannel, final int nrOfValues, final Date toDate, final long offset) throws IOException {
        ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow, indexChannel, nrOfValues, toDate, offset);
        o.set();
        return o;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveFlow);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public final ModuleType readModuleType() throws IOException {
        ModuleType moduleType = new ModuleType(waveFlow);
        moduleType.set();
        return moduleType;
    }

    public final ExtendedIndexReading readExtendedIndexConfiguration() throws IOException {
        ExtendedIndexReading o = new ExtendedIndexReading(waveFlow);
        o.set();
        return o;
    }

    public ExtendedDataloggingTable getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable newestRecord = new ExtendedDataloggingTable(waveFlow, 1, 1, 0);
        newestRecord.set();
        return newestRecord;
    }

    final public LeakageEventTable readLeakageEventTable() throws IOException {
        LeakageEventTable leakageEventTable = new LeakageEventTable(waveFlow);
        leakageEventTable.set();
        return leakageEventTable;
    }

    final public DailyConsumption readDailyConsumption() throws IOException {
        if (dailyConsumption == null) {
            dailyConsumption = new DailyConsumption(waveFlow);
            dailyConsumption.set();
        }
        return dailyConsumption;
    }

    final public BackFlowEventTableByVolumeMeasuring readBackFlowEventTableByVolumeMeasuring() throws IOException {
        BackFlowEventTableByVolumeMeasuring backFlowEventTable = new BackFlowEventTableByVolumeMeasuring(waveFlow);
        backFlowEventTable.set();
        return backFlowEventTable;
    }

    final public BackFlowEventTableByFlowRate readBackFlowEventTableByFlowRate() throws IOException {
        BackFlowEventTableByFlowRate backFlowEventTable = new BackFlowEventTableByFlowRate(waveFlow);
        backFlowEventTable.set();
        return backFlowEventTable;
    }

    public boolean openWaterValve() throws IOException {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            OpenWaterValveCommand openWaterValveCommand = new OpenWaterValveCommand(waveFlow);
            openWaterValveCommand.set();
            return openWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean closeWaterValve() throws IOException {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            CloseWaterValveCommand closeWaterValveCommand = new CloseWaterValveCommand(waveFlow);
            closeWaterValveCommand.set();
            return closeWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean cleanWaterValve() throws IOException {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            CleanWaterValveCommand cleanWaterValveCommand = new CleanWaterValveCommand(waveFlow);
            cleanWaterValveCommand.set();
            return cleanWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean addCreditBeforeClosing(int quantity, int add, int close) throws IOException {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            AddCreditBeforeClosing addCreditBeforeClosing = new AddCreditBeforeClosing(waveFlow, quantity, add, close);
            addCreditBeforeClosing.set();
            return addCreditBeforeClosing.isSuccess();
        }
        return false;
    }

    public ReadDataFeature readFeatureData() throws IOException {
        ReadDataFeature dataFeature = new ReadDataFeature(waveFlow);
        dataFeature.set();
        return dataFeature;
    }

    public void resetAlarmDisplay() throws IOException {
        WriteDataFeature dataFeature = new WriteDataFeature(waveFlow);
        dataFeature.setFeatureData(readFeatureData().getFeatureData());
        dataFeature.resetAlarmDisplay();
        dataFeature.set();
    }

    public void writeFeatureData(int value) throws IOException {
        WriteDataFeature dataFeature = new WriteDataFeature(waveFlow);
        dataFeature.setFeatureData(value);
        dataFeature.set();
    }

    public void setTimeOfUseTariffs(int periodMode, int duration, int startHourOrMonth, int startMinuteOrDay) throws IOException {
        ReadDataFeature dataFeature = readFeatureData();
        dataFeature.setTariffMode(0);
        dataFeature.set();
        this.tariffMode = null;     //Reset cache

        WriteTariffMode tariffMode = new WriteTariffMode(waveFlow);
        tariffMode.setTimeOfUseTariffs();
        tariffMode.setTariffPeriodMode(periodMode);
        tariffMode.setDuration(duration);
        tariffMode.setStartHourOrMonth(startHourOrMonth);
        tariffMode.setStartMinuteOrDay(startMinuteOrDay);
        tariffMode.set();
    }

    public void setRisingBlockTariffs(int numberOfLogBlocks, int scale, int period, int periodMode, int startHourOrMonth, int rb1, int rb2) throws IOException {
        ReadDataFeature dataFeature = readFeatureData();
        dataFeature.setTariffMode(1);
        dataFeature.set();
        this.tariffMode = null;     //Reset cache

        WriteTariffMode tariffMode = new WriteTariffMode(waveFlow);
        tariffMode.setRisingBlockTariffs();
        tariffMode.setRisingBlockTariffPeriodMode(periodMode);
        tariffMode.setLogBlocks(numberOfLogBlocks);
        tariffMode.setUnit(scale);
        tariffMode.setDuration(period);
        tariffMode.setStartTime(startHourOrMonth);
        tariffMode.setRB1(rb1);
        tariffMode.setRB2(rb2);
        tariffMode.set();
    }

    public ReadTariffMode readTariffSettings() throws IOException {
        if (tariffMode == null) {
            tariffMode = new ReadTariffMode(waveFlow);
            tariffMode.set();
        }
        return tariffMode;
    }

    public void writePeakFlowSettings(int period, int dayOfWeek, int weekOfYear) throws IOException {
        this.peakFlowSettings = null;    //Reset cache

        WritePeakFlowSettings peakFlowSettings = new WritePeakFlowSettings(waveFlow);
        peakFlowSettings.setMonitoringTimePeriod(period);
        peakFlowSettings.setStartTime(dayOfWeek);
        peakFlowSettings.setWeekOfYear(weekOfYear);
        peakFlowSettings.set();
    }

    public ReadPeakFlowSettings readPeakFlowSettings() throws IOException {
        if (peakFlowSettings == null) {
            peakFlowSettings = new ReadPeakFlowSettings(waveFlow);
            peakFlowSettings.set();
        }
        return peakFlowSettings;
    }

    public ReadPeakFlowData readPeakFlowData() throws IOException {
        ReadPeakFlowData peakFlowData = new ReadPeakFlowData(waveFlow);
        peakFlowData.set();
        return peakFlowData;
    }

    public void writeDateOfInstallation(int day, int month, int year) throws IOException {
        this.dateOfInstallation = null;  //Reset cache

        WriteDateOfInstallation dateOfInstallation = new WriteDateOfInstallation(waveFlow);
        dateOfInstallation.setDay(day);
        dateOfInstallation.setMonth(month);
        dateOfInstallation.setYear(year);
        dateOfInstallation.set();
    }

    public ReadDateOfInstallation readInstallationDate() throws IOException {
        if (dateOfInstallation == null) {
            dateOfInstallation = new ReadDateOfInstallation(waveFlow);
            dateOfInstallation.set();
        }
        return dateOfInstallation;
    }

    public void writeCustomerNumber(String number) throws IOException {
        WriteCustomerNumber customerNumber = new WriteCustomerNumber(waveFlow);
        customerNumber.setCustomerNumber(number);
        customerNumber.set();
    }

    public String readCustomerNumber() throws IOException {
        ReadCustomerNumber customerNumber = new ReadCustomerNumber(waveFlow);
        customerNumber.set();
        return customerNumber.getCustomerNumber();
    }

    public int readCurrentFlowRate() throws IOException {
        ReadCurrentFlowRate currentFlowRate = new ReadCurrentFlowRate(waveFlow);
        currentFlowRate.set();
        return currentFlowRate.getCurrentFlowRate();
    }

    public int readFrameTransmissionCount() throws IOException {
        ReadCountOfTransmission countOfTransmission = new ReadCountOfTransmission(waveFlow);
        countOfTransmission.set();
        return countOfTransmission.getCount();
    }

    public int readNoFlowTime() throws IOException {
        ReadCumulativeNoFlowTime noFlowTime = new ReadCumulativeNoFlowTime(waveFlow);
        noFlowTime.set();
        return noFlowTime.getDays();
    }

    public ReadOverspeedAlarmInfo readOverSpeedAlarmInfo() throws IOException {
        ReadOverspeedAlarmInfo overspeedAlarmInfo = new ReadOverspeedAlarmInfo(waveFlow);
        overspeedAlarmInfo.set();
        return overspeedAlarmInfo;
    }

    public int readCumulativeVolumeInDailySegment(int segment) throws IOException {
        ReadCumulativeFlowDaily cumulativeFlowDaily = new ReadCumulativeFlowDaily(waveFlow);
        cumulativeFlowDaily.set();
        return cumulativeFlowDaily.getFlow()[segment];
    }

    public int readCumulativeVolumeInBand(int band) throws IOException {
        ReadCumulativeFlowVolume cumulativeFlowVolumePerBand = new ReadCumulativeFlowVolume(waveFlow);
        cumulativeFlowVolumePerBand.set();
        return cumulativeFlowVolumePerBand.getFlow(band);
    }

    public ReadCumulativeFlowVolumeParameters readBandParameters() throws IOException {
        if (parameters7bands == null) {
            parameters7bands = new ReadCumulativeFlowVolumeParameters(waveFlow);
            parameters7bands.set();
        }
        return parameters7bands;
    }

    public Unit readBandUnit() throws IOException {
        readBandParameters();
        if (parameters7bands.getUnitFlag() == 0) {
            return SMALL_BILLABLE_UNIT;
        } else {
            return BILLABLE_UNIT;
        }
    }

    public Quantity readBandPeriod() throws IOException {
        return new Quantity(readBandParameters().getPeriod(), readBandParameters().getPeriodMode() == 0 ? Unit.get(BaseUnit.DAY) : Unit.get(BaseUnit.WEEK));
    }

    public int readBandLowThreshold(int band) throws IOException {
        return readBandParameters().getBandLowThreshold(band);
    }

    public int readBandHighThreshold(int band) throws IOException {
        return readBandParameters().getBandHighThreshold(band);
    }

    public void write7BandParameters(int[] thresholds, int year, int month, int day, int periodMode, int scale, int period) throws IOException {
        parameters7bands = null;

        WriteCumulativeFlowVolumeParameters parameters = new WriteCumulativeFlowVolumeParameters(waveFlow);
        parameters.setBand1LowThreshold(thresholds[0]);
        parameters.setBand2LowThreshold(thresholds[1]);
        parameters.setBand3LowThreshold(thresholds[2]);
        parameters.setBand4LowThreshold(thresholds[3]);
        parameters.setBand5LowThreshold(thresholds[4]);
        parameters.setBand6LowThreshold(thresholds[5]);
        parameters.setBand7LowThreshold(thresholds[6]);
        parameters.setBand7HighThreshold(thresholds[7]);
        parameters.setStartYear(year);
        parameters.setStartMonth(month);
        parameters.setStartDay(day);
        parameters.setPeriodMode(periodMode);
        parameters.setPeriod(period);
        parameters.setUnitFlag(scale == -3 ? 0 : 1);

        parameters.set();
    }

    public Read4DailySegmentsParameters readDailySegmentsParameters() throws IOException {
        if (dailySegmentsParameters == null) {
            dailySegmentsParameters = new Read4DailySegmentsParameters(waveFlow);
            dailySegmentsParameters.set();
        }
        return dailySegmentsParameters;
    }

    public void write4DailySegmentsParameters(int startHour, int startMinute, int[] stopHours, int[] stopMinutes, int year, int month, int day, int periodMode, int period) throws IOException {
        dailySegmentsParameters = null;

        Write4DailySegmentsParameters parameters = new Write4DailySegmentsParameters(waveFlow);
        parameters.setPeriod(period);
        parameters.setPeriodMode(periodMode);
        parameters.setStartDay(day);
        parameters.setStartMonth(month);
        parameters.setStartYear(year);
        parameters.setSegment1StartHour(startHour);
        parameters.setSegment1StartMinute(startMinute);

        parameters.setSegment1StopHour(stopHours[0]);
        parameters.setSegment1StopMinute(stopMinutes[0]);
        parameters.setSegment2StopHour(stopHours[1]);
        parameters.setSegment2StopMinute(stopMinutes[1]);
        parameters.setSegment3StopHour(stopHours[2]);
        parameters.setSegment3StopMinute(stopMinutes[2]);
        parameters.setSegment4StopHour(stopHours[3]);
        parameters.setSegment4StopMinute(stopMinutes[3]);
        parameters.set();
    }

    public ReadOverSpeedParameters readOverSpeedParameters() throws IOException {
        if (overSpeedParameters == null) {
            overSpeedParameters = new ReadOverSpeedParameters(waveFlow);
            overSpeedParameters.set();
        }
        return overSpeedParameters;
    }

    public void writeOverSpeedParameters(int threshold, int time) throws IOException {
        overSpeedParameters = null;

        WriteOverSpeedParameters writeOverSpeedParameters = new WriteOverSpeedParameters(waveFlow);
        writeOverSpeedParameters.setSpeedThreshold(threshold);
        writeOverSpeedParameters.setTimeForOverSpeedAlarm(time);
        writeOverSpeedParameters.set();
    }
}