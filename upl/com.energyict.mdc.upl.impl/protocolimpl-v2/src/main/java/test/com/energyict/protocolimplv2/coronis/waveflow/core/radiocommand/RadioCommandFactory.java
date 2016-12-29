package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.ProfileType;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.Read4DailySegmentsParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCountOfTransmission;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCumulativeFlowDaily;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCumulativeFlowVolume;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCumulativeFlowVolumeParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCumulativeNoFlowTime;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCurrentFlowRate;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadCustomerNumber;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadDataFeature;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadDateOfInstallation;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadOverSpeedParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadOverspeedAlarmInfo;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadPeakFlowData;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadPeakFlowSettings;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.ReadTariffMode;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.Write4DailySegmentsParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteCumulativeFlowVolumeParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteCustomerNumber;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteDataFeature;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteDateOfInstallation;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteOverSpeedParameters;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WritePeakFlowSettings;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210.WriteTariffMode;

import java.util.Date;

public class RadioCommandFactory {

    protected final WaveFlow waveFlow;

    // cached
    private FirmwareVersion firmwareVersion = null;
    private DailyConsumption dailyConsumption = null;
    private ReadTariffMode tariffMode = null;
    private ReadPeakFlowSettings peakFlowSettings = null;
    private ReadDateOfInstallation dateOfInstallation = null;
    private ReadCumulativeFlowVolumeParameters parameters7bands = null;
    private Read4DailySegmentsParameters dailySegmentsParameters = null;
    private ReadOverSpeedParameters overSpeedParameters = null;
    private GlobalIndexReading currentIndexes = null;
    private ModuleType moduleType = null;

    public static final Unit BILLABLE_UNIT = Unit.get(BaseUnit.CUBICMETER);
    public static final Unit SMALL_BILLABLE_UNIT = Unit.get(BaseUnit.LITER);
    private ExtendedIndexReading extendedIndexReading = null;

    public RadioCommandFactory(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    /**
     * Read the meter's current indexes (for all inputs: A, B, C, D)
     * Cache the results.
     *
     * @return the current readings
     */
    public final GlobalIndexReading readCurrentReading() {
        if (currentIndexes == null) {
            currentIndexes = new GlobalIndexReading(waveFlow);
            currentIndexes.set();
        }
        return currentIndexes;
    }

    public void initializeRoute(int alarmMode) {
        InitializeAlarmRoute initializeAlarmRoute = new InitializeAlarmRoute(waveFlow);
        initializeAlarmRoute.setAlarmMode(alarmMode);
        initializeAlarmRoute.set();
    }

    public final ExtendedDataloggingTable readExtendedDataloggingTable(int indexChannel, final int nrOfValues, final Date toDate) {
        ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow, indexChannel, nrOfValues, toDate);
        o.set();
        return o;
    }

    public void resetIndexes() {
        WriteIndexes writeIndexes = new WriteIndexes(waveFlow);
        writeIndexes.setIndexA(0);
        writeIndexes.setIndexB(0);
        writeIndexes.setIndexC(0);
        writeIndexes.setIndexD(0);
        writeIndexes.set();
    }

    public void writeIndexes(int index, int input) {
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

    public DataloggingTable readDataloggingTable(int channels) {
        DataloggingTable dataloggingTable = new DataloggingTable(waveFlow);
        dataloggingTable.setChannels(channels);
        dataloggingTable.set();
        return dataloggingTable;
    }

    public final ExtendedDataloggingTable readExtendedDataloggingTable(int indexChannel, final int nrOfValues, final Date toDate, final long offset) {
        ExtendedDataloggingTable o = new ExtendedDataloggingTable(waveFlow, indexChannel, nrOfValues, toDate, offset);
        o.set();
        return o;
    }

    public final FirmwareVersion readFirmwareVersion() {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveFlow);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public final double readRSSILevel() {
        if (moduleType == null) {
            moduleType = new ModuleType(waveFlow);
            moduleType.set();
        }
        return moduleType.getRssiLevel();
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public final ExtendedIndexReading readExtendedIndexConfiguration() {
        if (extendedIndexReading == null) {
            extendedIndexReading = new ExtendedIndexReading(waveFlow);
            extendedIndexReading.set();

            currentIndexes = new GlobalIndexReading(waveFlow);             //Cache the current indexes!
            for (int i = 0; i < extendedIndexReading.getNumberOfEnabledInputs(); i++) {
                int currentIndex = extendedIndexReading.getCurrentIndex(i);
                currentIndexes.setReading(i, currentIndex);
            }

            //Cache the received sampling period
            waveFlow.getParameterFactory().setSamplingPeriod(extendedIndexReading.getDataloggingMeasurementPeriod());
        }
        return extendedIndexReading;
    }

    public ExtendedDataloggingTable getMostRecentRecord() {
        ExtendedDataloggingTable newestRecord = new ExtendedDataloggingTable(waveFlow, 1, 1, 0);
        newestRecord.set();
        return newestRecord;
    }

    public final LeakageEventTable readLeakageEventTable() {
        LeakageEventTable leakageEventTable = new LeakageEventTable(waveFlow);
        leakageEventTable.set();
        return leakageEventTable;
    }

    public final DailyConsumption readDailyConsumption() {
        if (dailyConsumption == null) {
            dailyConsumption = new DailyConsumption(waveFlow);
            dailyConsumption.setGenericHeaderLength(23);
            dailyConsumption.set();

            //Cache the contents: TODO: index B, C and D?
            currentIndexes = new GlobalIndexReading(waveFlow);
            currentIndexes.setReading(0, dailyConsumption.getIndexZone().getCurrentIndexOnA());
            currentIndexes.setReading(1, dailyConsumption.getIndexZone().getExpectedIndexOnB());
            currentIndexes.setReading(2, dailyConsumption.getIndexZone().getExpectedIndexOnC());
            currentIndexes.setReading(3, dailyConsumption.getIndexZone().getExpectedIndexOnD());
            waveFlow.getParameterFactory().setSamplingPeriod(dailyConsumption.getSamplingPeriod());
        }
        return dailyConsumption;
    }

    public final BackFlowEventTableByVolumeMeasuring readBackFlowEventTableByVolumeMeasuring() {
        BackFlowEventTableByVolumeMeasuring backFlowEventTable = new BackFlowEventTableByVolumeMeasuring(waveFlow);
        backFlowEventTable.set();
        return backFlowEventTable;
    }

    public final BackFlowEventTableByFlowRate readBackFlowEventTableByFlowRate() {
        BackFlowEventTableByFlowRate backFlowEventTable = new BackFlowEventTableByFlowRate(waveFlow);
        backFlowEventTable.set();
        return backFlowEventTable;
    }

    public boolean openWaterValve() {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            OpenWaterValveCommand openWaterValveCommand = new OpenWaterValveCommand(waveFlow);
            openWaterValveCommand.set();
            return openWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean closeWaterValve() {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            CloseWaterValveCommand closeWaterValveCommand = new CloseWaterValveCommand(waveFlow);
            closeWaterValveCommand.set();
            return closeWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean cleanWaterValve() {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            CleanWaterValveCommand cleanWaterValveCommand = new CleanWaterValveCommand(waveFlow);
            cleanWaterValveCommand.set();
            return cleanWaterValveCommand.isSuccess();
        }
        return false;
    }

    public int readValveStatus() {
        ReadValveStatus readValveStatus = new ReadValveStatus(waveFlow);
        readValveStatus.set();
        return readValveStatus.getStatus();
    }

    public boolean addCreditBeforeClosing(int quantity, int add, int close) {
        ProfileType profileType = waveFlow.getParameterFactory().readProfileType();
        if (profileType.supportsWaterValveControl()) {
            AddCreditBeforeClosing addCreditBeforeClosing = new AddCreditBeforeClosing(waveFlow, quantity, add, close);
            addCreditBeforeClosing.set();
            return addCreditBeforeClosing.isSuccess();
        }
        return false;
    }

    public ReadDataFeature readFeatureData() {
        ReadDataFeature dataFeature = new ReadDataFeature(waveFlow);
        dataFeature.set();
        return dataFeature;
    }

    public void resetAlarmDisplay() {
        WriteDataFeature dataFeature = new WriteDataFeature(waveFlow);
        dataFeature.setFeatureData(readFeatureData().getFeatureData());
        dataFeature.resetAlarmDisplay();
        dataFeature.set();
    }

    public void writeFeatureData(int value) {
        WriteDataFeature dataFeature = new WriteDataFeature(waveFlow);
        dataFeature.setFeatureData(value);
        dataFeature.set();
    }

    public void setTimeOfUseTariffs(int periodMode, int duration, int startHourOrMonth, int startMinuteOrDay) {
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

    public void setRisingBlockTariffs(int numberOfLogBlocks, int scale, int period, int periodMode, int startHourOrMonth, int rb1, int rb2) {
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

    public ReadTariffMode readTariffSettings() {
        if (tariffMode == null) {
            tariffMode = new ReadTariffMode(waveFlow);
            tariffMode.set();
        }
        return tariffMode;
    }

    public void writePeakFlowSettings(int period, int dayOfWeek, int weekOfYear) {
        this.peakFlowSettings = null;    //Reset cache

        WritePeakFlowSettings peakFlowSettings = new WritePeakFlowSettings(waveFlow);
        peakFlowSettings.setMonitoringTimePeriod(period);
        peakFlowSettings.setStartTime(dayOfWeek);
        peakFlowSettings.setWeekOfYear(weekOfYear);
        peakFlowSettings.set();
    }

    public ReadPeakFlowSettings readPeakFlowSettings() {
        if (peakFlowSettings == null) {
            peakFlowSettings = new ReadPeakFlowSettings(waveFlow);
            peakFlowSettings.set();
        }
        return peakFlowSettings;
    }

    public ReadPeakFlowData readPeakFlowData() {
        ReadPeakFlowData peakFlowData = new ReadPeakFlowData(waveFlow);
        peakFlowData.set();
        return peakFlowData;
    }

    public void writeDateOfInstallation(int day, int month, int year) {
        this.dateOfInstallation = null;  //Reset cache

        WriteDateOfInstallation dateOfInstallation = new WriteDateOfInstallation(waveFlow);
        dateOfInstallation.setDay(day);
        dateOfInstallation.setMonth(month);
        dateOfInstallation.setYear(year);
        dateOfInstallation.set();
    }

    public ReadDateOfInstallation readInstallationDate() {
        if (dateOfInstallation == null) {
            dateOfInstallation = new ReadDateOfInstallation(waveFlow);
            dateOfInstallation.set();
        }
        return dateOfInstallation;
    }

    public void writeCustomerNumber(String number) {
        WriteCustomerNumber customerNumber = new WriteCustomerNumber(waveFlow);
        customerNumber.setCustomerNumber(number);
        customerNumber.set();
    }

    public String readCustomerNumber() {
        ReadCustomerNumber customerNumber = new ReadCustomerNumber(waveFlow);
        customerNumber.set();
        return customerNumber.getCustomerNumber();
    }

    public int readCurrentFlowRate() {
        ReadCurrentFlowRate currentFlowRate = new ReadCurrentFlowRate(waveFlow);
        currentFlowRate.set();
        return currentFlowRate.getCurrentFlowRate();
    }

    public int readFrameTransmissionCount() {
        ReadCountOfTransmission countOfTransmission = new ReadCountOfTransmission(waveFlow);
        countOfTransmission.set();
        return countOfTransmission.getCount();
    }

    public int readNoFlowTime() {
        ReadCumulativeNoFlowTime noFlowTime = new ReadCumulativeNoFlowTime(waveFlow);
        noFlowTime.set();
        return noFlowTime.getDays();
    }

    public ReadOverspeedAlarmInfo readOverSpeedAlarmInfo() {
        ReadOverspeedAlarmInfo overspeedAlarmInfo = new ReadOverspeedAlarmInfo(waveFlow);
        overspeedAlarmInfo.set();
        return overspeedAlarmInfo;
    }

    public int readCumulativeVolumeInDailySegment(int segment) {
        ReadCumulativeFlowDaily cumulativeFlowDaily = new ReadCumulativeFlowDaily(waveFlow);
        cumulativeFlowDaily.set();
        return cumulativeFlowDaily.getFlow()[segment];
    }

    public int readCumulativeVolumeInBand(int band) {
        ReadCumulativeFlowVolume cumulativeFlowVolumePerBand = new ReadCumulativeFlowVolume(waveFlow);
        cumulativeFlowVolumePerBand.set();
        return cumulativeFlowVolumePerBand.getFlow(band);
    }

    public ReadCumulativeFlowVolumeParameters readBandParameters() {
        if (parameters7bands == null) {
            parameters7bands = new ReadCumulativeFlowVolumeParameters(waveFlow);
            parameters7bands.set();
        }
        return parameters7bands;
    }

    public Unit readBandUnit() {
        readBandParameters();
        if (parameters7bands.getUnitFlag() == 0) {
            return SMALL_BILLABLE_UNIT;
        } else {
            return BILLABLE_UNIT;
        }
    }

    public Quantity readBandPeriod() {
        return new Quantity(readBandParameters().getPeriod(), readBandParameters().getPeriodMode() == 0 ? Unit.get(BaseUnit.DAY) : Unit.get(BaseUnit.WEEK));
    }

    public int readBandLowThreshold(int band) {
        return readBandParameters().getBandLowThreshold(band);
    }

    public int readBandHighThreshold(int band) {
        return readBandParameters().getBandHighThreshold(band);
    }

    public void write7BandParameters(int[] thresholds, int year, int month, int day, int periodMode, int scale, int period) {
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

    public Read4DailySegmentsParameters readDailySegmentsParameters() {
        if (dailySegmentsParameters == null) {
            dailySegmentsParameters = new Read4DailySegmentsParameters(waveFlow);
            dailySegmentsParameters.set();
        }
        return dailySegmentsParameters;
    }

    public void write4DailySegmentsParameters(int startHour, int startMinute, int[] stopHours, int[] stopMinutes, int year, int month, int day, int periodMode, int period) {
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

    public ReadOverSpeedParameters readOverSpeedParameters() {
        if (overSpeedParameters == null) {
            overSpeedParameters = new ReadOverSpeedParameters(waveFlow);
            overSpeedParameters.set();
        }
        return overSpeedParameters;
    }

    public void writeOverSpeedParameters(int threshold, int time) {
        overSpeedParameters = null;

        WriteOverSpeedParameters writeOverSpeedParameters = new WriteOverSpeedParameters(waveFlow);
        writeOverSpeedParameters.setSpeedThreshold(threshold);
        writeOverSpeedParameters.setTimeForOverSpeedAlarm(time);
        writeOverSpeedParameters.set();
    }
}