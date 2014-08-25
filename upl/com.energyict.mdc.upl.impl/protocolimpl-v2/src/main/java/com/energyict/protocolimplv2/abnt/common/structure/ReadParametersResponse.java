package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.DateTimeField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.HolidayRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.TariffConfigurationField;
import com.energyict.protocolimplv2.abnt.common.structure.field.TariffEntryRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitField;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ReadParametersResponse extends Data<ReadParametersResponse> {

    private static final Integer[] NULL_DATA_LENGTHS = new Integer[]{20, 3, 8, 32, 5, 5, 25, 1, 5, 5};
    private static final int DATE_TIME_LENGTH = 6;
    private static final int NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH = 3;
    private static final int NUMERATOR_LENGTH = 3;
    private static final int SOFTWARE_VERSION_LENGTH = 2;
    private static final int METER_MODEL_LENGTH = 2;
    private static final int NUMBER_OF_HOLIDAY_RECORDS = 15;
    private static final int NUMBER_OF_TARIFF_ENTRY_RECORDS = 2;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmmssddMMyy");

    private DateTimeField currentDateTime;          //TODO: check usage of some fields ?!
    private BcdEncodedField dayOfWeek;
    private DateTimeField dateTimeOfLastDemandInterval;
    private DateTimeField dateTimeOfLastDemandReset;
    private DateTimeField dateTimeOfSecondLastDemandRest;
    private List<TariffEntryRecord> entryRecordsTariffA;
    private List<TariffEntryRecord> entryRecordsTariffB;
    private List<TariffEntryRecord> entryRecordsTariffC;
    private BcdEncodedField numberOfLoadProfileWords;
    private BcdEncodedField numberOfLoadProfileWordsInPreviousBilling;
    private BcdEncodedField numberOfDemandResets;
    private BcdEncodedField demandInterval;
    private BcdEncodedField demandIntervalInPreviousBilling;
    private List<HolidayRecord> holidayRecords;
    private BcdEncodedField numeratorChn1;
    private BcdEncodedField denominatorChn1;
    private BcdEncodedField numeratorChn2;
    private BcdEncodedField denominatorChn2;
    private BcdEncodedField numeratorChn3;
    private BcdEncodedField denominatorChn3;
    private BcdEncodedField batteryStatus;
    private BcdEncodedField softwareVersion;
    private BcdEncodedField meterModel;
    private BcdEncodedField conditionOfConjunctionOfSegments;   //TODO: usage/translation?
    private UnitField unitChn1;
    private UnitField unitChn2;
    private UnitField unitChn3;
    private BcdEncodedField loadProfileInterval;
    private TariffConfigurationField activeTariffsOnSaturdays;
    private TariffConfigurationField activeTariffsOnSundays;
    private TariffConfigurationField activeTariffsOnHolidays;
    private BcdEncodedField numberOfTimeSegments;   //TODO: usage?
    private BcdEncodedField availabilityOfMeasurementParameters;
    private BcdEncodedField availabilityOfFiscalPage;
    private BcdEncodedField numberOfLoadProfileChannelGroups;
    //TODO: bitflags

    public ReadParametersResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        currentDateTime = new DateTimeField(dateFormatter, DATE_TIME_LENGTH);
        dayOfWeek = new BcdEncodedField();
        dateTimeOfLastDemandInterval = new DateTimeField(dateFormatter, DATE_TIME_LENGTH);
        dateTimeOfLastDemandReset = new DateTimeField(dateFormatter, DATE_TIME_LENGTH);
        dateTimeOfSecondLastDemandRest = new DateTimeField(dateFormatter, DATE_TIME_LENGTH);
        entryRecordsTariffA = new ArrayList<>();
        entryRecordsTariffB = new ArrayList<>();
        entryRecordsTariffC = new ArrayList<>();
        numberOfLoadProfileWords = new BcdEncodedField(NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH);
        numberOfLoadProfileWordsInPreviousBilling = new BcdEncodedField(NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH);
        numberOfDemandResets = new BcdEncodedField();
        demandInterval = new BcdEncodedField();
        demandIntervalInPreviousBilling = new BcdEncodedField();
        holidayRecords = new ArrayList<>();
        numeratorChn1 = new BcdEncodedField(NUMERATOR_LENGTH);
        denominatorChn1 = new BcdEncodedField(NUMERATOR_LENGTH);
        numeratorChn2 = new BcdEncodedField(NUMERATOR_LENGTH);
        denominatorChn2 = new BcdEncodedField(NUMERATOR_LENGTH);
        numeratorChn3 = new BcdEncodedField(NUMERATOR_LENGTH);
        denominatorChn3 = new BcdEncodedField(NUMERATOR_LENGTH);
        batteryStatus = new BcdEncodedField();
        softwareVersion = new BcdEncodedField(SOFTWARE_VERSION_LENGTH);
        meterModel = new BcdEncodedField(METER_MODEL_LENGTH);
        conditionOfConjunctionOfSegments = new BcdEncodedField();
        unitChn1 = new UnitField();
        unitChn2 = new UnitField();
        unitChn3 = new UnitField();
        loadProfileInterval = new BcdEncodedField();
        activeTariffsOnSaturdays = new TariffConfigurationField();
        activeTariffsOnSundays = new TariffConfigurationField();
        activeTariffsOnHolidays = new TariffConfigurationField();
        numberOfTimeSegments = new BcdEncodedField();
        availabilityOfMeasurementParameters = new BcdEncodedField();
        availabilityOfFiscalPage = new BcdEncodedField();
        numberOfLoadProfileChannelGroups = new BcdEncodedField();
    }

    @Override
    public ReadParametersResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);

        currentDateTime.parse(rawData, ptr);
        ptr += currentDateTime.getLength();

        dayOfWeek.parse(rawData, ptr);
        ptr += dayOfWeek.getLength();

        dateTimeOfLastDemandInterval.parse(rawData, ptr);
        ptr += dateTimeOfLastDemandInterval.getLength();

        dateTimeOfLastDemandReset.parse(rawData, ptr);
        ptr += dateTimeOfLastDemandReset.getLength();

        dateTimeOfSecondLastDemandRest.parse(rawData, ptr);
        ptr += dateTimeOfSecondLastDemandRest.getLength();

        ptr += NULL_DATA_LENGTHS[0];

        for (int i = 0; i < NUMBER_OF_TARIFF_ENTRY_RECORDS; i++) {
            TariffEntryRecord record = new TariffEntryRecord();
            record.parse(rawData, ptr);
            ptr += record.getLength();
            entryRecordsTariffA.add(record);
        }

        for (int i = 0; i < NUMBER_OF_TARIFF_ENTRY_RECORDS; i++) {
            TariffEntryRecord record = new TariffEntryRecord();
            record.parse(rawData, ptr);
            ptr += record.getLength();
            entryRecordsTariffB.add(record);
        }

        for (int i = 0; i < NUMBER_OF_TARIFF_ENTRY_RECORDS; i++) {
            TariffEntryRecord record = new TariffEntryRecord();
            record.parse(rawData, ptr);
            ptr += record.getLength();
            entryRecordsTariffC.add(record);
        }

        numberOfLoadProfileWords.parse(rawData, ptr);
        ptr += numberOfLoadProfileWords.getLength();

        numberOfLoadProfileWordsInPreviousBilling.parse(rawData, ptr);
        ptr += numberOfLoadProfileWordsInPreviousBilling.getLength();

        numberOfDemandResets.parse(rawData, ptr);
        ptr += numberOfDemandResets.getLength();

        demandInterval.parse(rawData, ptr);
        ptr += demandInterval.getLength();

        demandIntervalInPreviousBilling.parse(rawData, ptr);
        ptr += demandIntervalInPreviousBilling.getLength();

        for (int i = 0; i < NUMBER_OF_HOLIDAY_RECORDS; i++) {
            HolidayRecord record = new HolidayRecord(getTimeZone());
            record.parse(rawData, ptr);
            holidayRecords.add(record);
            ptr += record.getLength();
        }

        numeratorChn1.parse(rawData, ptr);
        ptr += numeratorChn1.getLength();

        denominatorChn1.parse(rawData, ptr);
        ptr += denominatorChn1.getLength();

        numeratorChn2.parse(rawData, ptr);
        ptr += numeratorChn2.getLength();

        denominatorChn2.parse(rawData, ptr);
        ptr += denominatorChn2.getLength();

        numeratorChn3.parse(rawData, ptr);
        ptr += numeratorChn3.getLength();

        denominatorChn3.parse(rawData, ptr);
        ptr += denominatorChn3.getLength();

        batteryStatus.parse(rawData, ptr);
        ptr += batteryStatus.getLength();

        softwareVersion.parse(rawData, ptr);
        ptr += softwareVersion.getLength();

        ptr += NULL_DATA_LENGTHS[1];

        meterModel.parse(rawData, ptr);
        ptr += meterModel.getLength();

        ptr += NULL_DATA_LENGTHS[2];

        conditionOfConjunctionOfSegments.parse(rawData, ptr);
        ptr += conditionOfConjunctionOfSegments.getLength();

        ptr += NULL_DATA_LENGTHS[3];

        unitChn1.parse(rawData, ptr);
        ptr += unitChn1.getLength();

        unitChn2.parse(rawData, ptr);
        ptr += unitChn2.getLength();

        unitChn3.parse(rawData, ptr);
        ptr += unitChn3.getLength();

        ptr += NULL_DATA_LENGTHS[4];

        loadProfileInterval.parse(rawData, ptr);
        ptr += loadProfileInterval.getLength();

        ptr += NULL_DATA_LENGTHS[5];

        activeTariffsOnSaturdays.parse(rawData, ptr);
        ptr += activeTariffsOnSaturdays.getLength();

        activeTariffsOnSundays.parse(rawData, ptr);
        ptr += activeTariffsOnSundays.getLength();

        activeTariffsOnHolidays.parse(rawData, ptr);
        ptr += activeTariffsOnHolidays.getLength();

        ptr += NULL_DATA_LENGTHS[6];

        numberOfTimeSegments.parse(rawData, ptr);
        ptr += numberOfTimeSegments.getLength();

        ptr += NULL_DATA_LENGTHS[7];

        availabilityOfMeasurementParameters.parse(rawData, ptr);
        ptr += availabilityOfMeasurementParameters.getLength();

        ptr += NULL_DATA_LENGTHS[8];

        availabilityOfFiscalPage.parse(rawData, ptr);
        ptr += availabilityOfFiscalPage.getLength();

        numberOfLoadProfileChannelGroups.parse(rawData, ptr);
        return this;
    }

    public DateTimeField getCurrentDateTime() {
        return currentDateTime;
    }

    public BcdEncodedField getDayOfWeek() {
        return dayOfWeek;
    }

    public DateTimeField getDateTimeOfLastDemandInterval() {
        return dateTimeOfLastDemandInterval;
    }

    public DateTimeField getDateTimeOfLastDemandReset() {
        return dateTimeOfLastDemandReset;
    }

    public DateTimeField getDateTimeOfSecondLastDemandRest() {
        return dateTimeOfSecondLastDemandRest;
    }

    public List<TariffEntryRecord> getEntryRecordsTariffA() {
        return entryRecordsTariffA;
    }

    public List<TariffEntryRecord> getEntryRecordsTariffB() {
        return entryRecordsTariffB;
    }

    public List<TariffEntryRecord> getEntryRecordsTariffC() {
        return entryRecordsTariffC;
    }

    public BcdEncodedField getNumberOfLoadProfileWords() {
        return numberOfLoadProfileWords;
    }

    public BcdEncodedField getNumberOfLoadProfileWordsInPreviousBilling() {
        return numberOfLoadProfileWordsInPreviousBilling;
    }

    public BcdEncodedField getNumberOfDemandResets() {
        return numberOfDemandResets;
    }

    public BcdEncodedField getDemandInterval() {
        return demandInterval;
    }

    public BcdEncodedField getDemandIntervalInPreviousBilling() {
        return demandIntervalInPreviousBilling;
    }

    public List<HolidayRecord> getHolidayRecords() {
        return holidayRecords;
    }

    public BcdEncodedField getNumeratorChn1() {
        return numeratorChn1;
    }

    public BcdEncodedField getDenominatorChn1() {
        return denominatorChn1;
    }

    public BcdEncodedField getNumeratorChn2() {
        return numeratorChn2;
    }

    public BcdEncodedField getDenominatorChn2() {
        return denominatorChn2;
    }

    public BcdEncodedField getNumeratorChn3() {
        return numeratorChn3;
    }

    public BcdEncodedField getDenominatorChn3() {
        return denominatorChn3;
    }

    public BcdEncodedField getBatteryStatus() {
        return batteryStatus;
    }

    public BcdEncodedField getSoftwareVersion() {
        return softwareVersion;
    }

    public BcdEncodedField getMeterModel() {
        return meterModel;
    }

    public BcdEncodedField getConditionOfConjunctionOfSegments() {
        return conditionOfConjunctionOfSegments;
    }

    public UnitField getUnitChn1() {
        return unitChn1;
    }

    public UnitField getUnitChn2() {
        return unitChn2;
    }

    public UnitField getUnitChn3() {
        return unitChn3;
    }

    public BcdEncodedField getLoadProfileInterval() {
        return loadProfileInterval;
    }

    public TariffConfigurationField getActiveTariffsOnSaturdays() {
        return activeTariffsOnSaturdays;
    }

    public TariffConfigurationField getActiveTariffsOnSundays() {
        return activeTariffsOnSundays;
    }

    public TariffConfigurationField getActiveTariffsOnHolidays() {
        return activeTariffsOnHolidays;
    }

    public BcdEncodedField getNumberOfTimeSegments() {
        return numberOfTimeSegments;
    }

    public BcdEncodedField getAvailabilityOfMeasurementParameters() {
        return availabilityOfMeasurementParameters;
    }

    public BcdEncodedField getAvailabilityOfFiscalPage() {
        return availabilityOfFiscalPage;
    }

    public BcdEncodedField getNumberOfLoadProfileChannelGroups() {
        return numberOfLoadProfileChannelGroups;
    }
}