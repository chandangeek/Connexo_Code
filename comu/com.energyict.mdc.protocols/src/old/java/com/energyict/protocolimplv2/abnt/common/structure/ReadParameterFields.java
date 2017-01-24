package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.field.parser.BcdEncodedFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.DateTimeFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.FieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.PaddingDataParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.SimpleFieldParser;
import com.energyict.protocolimplv2.abnt.common.structure.field.AutomaticDemandResetCondition;
import com.energyict.protocolimplv2.abnt.common.structure.field.BatteryStatusField;
import com.energyict.protocolimplv2.abnt.common.structure.field.DstConfigurationRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.SoftwareVersionField;
import com.energyict.protocolimplv2.abnt.common.structure.field.TariffConfigurationField;
import com.energyict.protocolimplv2.abnt.common.structure.field.TariffEntries;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitField;

/**
 * @author sva
 * @since 11/09/2014 - 11:33
 */
public enum ReadParameterFields {

    currentDateTime(0, new DateTimeFieldParser(ReadParametersResponse.dateFormatter, ReadParametersResponse.DATE_TIME_LENGTH)),
    dayOfWeek(1, new SimpleFieldParser<>(BcdEncodedField.class)),
    dateTimeOfLastDemandInterval(2, new DateTimeFieldParser(ReadParametersResponse.dateFormatter, ReadParametersResponse.DATE_TIME_LENGTH)),
    dateTimeOfLastDemandReset(3, new DateTimeFieldParser(ReadParametersResponse.dateFormatter, ReadParametersResponse.DATE_TIME_LENGTH)),
    dateTimeOfSecondLastDemandReset(4, new DateTimeFieldParser(ReadParametersResponse.dateFormatter, ReadParametersResponse.DATE_TIME_LENGTH)),
    dummyDataA(5, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[0])),
    tariffEntriesTariffA(6, new SimpleFieldParser<>(TariffEntries.class)),
    tariffEntriesTariffB(7, new SimpleFieldParser<>(TariffEntries.class)),
    tariffEntriesTariffC(8, new SimpleFieldParser<>(TariffEntries.class)),
    numberOfLoadProfileWords(9, new BcdEncodedFieldParser(ReadParametersResponse.NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH)),
    numberOfLoadProfileWordsInPreviousBilling(10, new BcdEncodedFieldParser(ReadParametersResponse.NUMBER_OF_LOAD_PROFILE_WORDS_LENGTH)),
    numberOfDemandResets(11, new SimpleFieldParser<>(BcdEncodedField.class)),
    demandInterval(12, new SimpleFieldParser<>(BcdEncodedField.class)),
    demandIntervalInPreviousBilling(13, new SimpleFieldParser<>(BcdEncodedField.class)),
    holidayRecords(14, new SimpleFieldParser<>(HolidayRecords.class)),
    numeratorChn1(15, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    denominatorChn1(16, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    numeratorChn2(17, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    denominatorChn2(18, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    numeratorChn3(19, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    denominatorChn3(20, new BcdEncodedFieldParser(ReadParametersResponse.NUMERATOR_LENGTH)),
    batteryStatus(21, new SimpleFieldParser<>(BatteryStatusField.class)),
    softwareVersion(22, new SimpleFieldParser<>(SoftwareVersionField.class)),
    dummyDataB(23, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[1])),
    meterModel(24, new BcdEncodedFieldParser(ReadParametersResponse.METER_MODEL_LENGTH)),
    dummyDataC(25, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[2])),
    automaticDemandResetCondition(26, new SimpleFieldParser<>(AutomaticDemandResetCondition.class)),
    automaticDemandResetDay(27, new SimpleFieldParser<>(BcdEncodedField.class)),
    dstInfo(28, new SimpleFieldParser<>(DstConfigurationRecord.class)),
    conditionOfConjunctionOfSegments(29, new SimpleFieldParser<>(BcdEncodedField.class)),
    dummyDataD(30, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[3])),
    unitChn1(31, new SimpleFieldParser<>(UnitField.class)),
    unitChn2(32, new SimpleFieldParser<>(UnitField.class)),
    unitChn3(33, new SimpleFieldParser<>(UnitField.class)),
    dummyDataE(34, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[4])),
    loadProfileInterval(35, new SimpleFieldParser<>(BcdEncodedField.class)),
    dummyDataF(36, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[5])),
    activeTariffsOnSaturdays(37, new SimpleFieldParser<>(TariffConfigurationField.class)),
    activeTariffsOnSundays(38, new SimpleFieldParser<>(TariffConfigurationField.class)),
    activeTariffsOnHolidays(39, new SimpleFieldParser<>(TariffConfigurationField.class)),
    dummyDataG(40, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[6])),
    numberOfTimeSegments(41, new SimpleFieldParser<>(BcdEncodedField.class)),
    dummyDataH(42, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[7])),
    availabilityOfMeasurementParameters(43, new SimpleFieldParser<>(BcdEncodedField.class)),
    dummyDataI(44, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[8])),
    availabilityOfFiscalPage(45, new SimpleFieldParser<>(BcdEncodedField.class)),
    numberOfLoadProfileChannelGroups(46, new SimpleFieldParser<>(BcdEncodedField.class)),
    dummyDataJ(47, new PaddingDataParser(ReadParametersResponse.NULL_DATA_LENGTHS[9])),
    automaticDemandResetHour(48, new SimpleFieldParser<>(BcdEncodedField.class));

    private final int code;
    private final FieldParser fieldParser;

    ReadParameterFields(int code, FieldParser fieldParser) {
        this.code = code;
        this.fieldParser = fieldParser;
    }

    public FieldParser getFieldParser() {
        return fieldParser;
    }

    public int getCode() {
        return code;
    }

    public static ReadParameterFields fromCode(int code) {
        for (ReadParameterFields readParameterFields : values()) {
            if (readParameterFields.getCode() == code){
                return readParameterFields;
            }
        }
        return null;
    }
}