package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.field.parser.BcdEncodedFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.DateTimeFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.FieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.FloatFieldParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.PaddingDataParser;
import com.energyict.protocolimplv2.abnt.common.field.parser.SimpleFieldParser;
import com.energyict.protocolimplv2.abnt.common.structure.field.ConnectionTypeField;
import com.energyict.protocolimplv2.abnt.common.structure.field.QuantityConversionIndicatorField;
import com.energyict.protocolimplv2.abnt.common.structure.field.ReactivePowerCharacteristicField;

/**
 * @author sva
 * @since 11/09/2014 - 10:08
 */
public enum InstrumentationPageFields {
    readingDate(0, new DateTimeFieldParser(InstrumentationPageResponse.dateFormatter, InstrumentationPageResponse.DATE_TIME_LENGTH)),
    voltagePhaseA(1, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    voltagePhaseB(2, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    voltagePhaseC(3, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    voltagePhaseAB(4, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    voltagePhaseBC(5, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    voltagePhaseCA(6, new FloatFieldParser(InstrumentationPageResponse.VOLTAGE_UNIT)),
    currentPhaseA(7, new FloatFieldParser(InstrumentationPageResponse.CURRENT_UNIT)),
    currentPhaseB(8, new FloatFieldParser(InstrumentationPageResponse.CURRENT_UNIT)),
    currentPhaseC(9, new FloatFieldParser(InstrumentationPageResponse.CURRENT_UNIT)),
    currentPhaseN(10, new FloatFieldParser(InstrumentationPageResponse.CURRENT_UNIT)),
    activePowerPhaseA(11, new FloatFieldParser(InstrumentationPageResponse.ACTIVE_POWER_UNIT)),
    activePowerPhaseB(12, new FloatFieldParser(InstrumentationPageResponse.ACTIVE_POWER_UNIT)),
    activePowerPhaseC(13, new FloatFieldParser(InstrumentationPageResponse.ACTIVE_POWER_UNIT)),
    activePowerThreePhase(14, new FloatFieldParser(InstrumentationPageResponse.ACTIVE_POWER_UNIT)),
    reactivePowerPhaseA(15, new FloatFieldParser(InstrumentationPageResponse.REACTIVE_POWER_UNIT)),
    reactivePowerPhaseB(16, new FloatFieldParser(InstrumentationPageResponse.REACTIVE_POWER_UNIT)),
    reactivePowerPhaseC(17, new FloatFieldParser(InstrumentationPageResponse.REACTIVE_POWER_UNIT)),
    reactivePowerThreePhase(18, new FloatFieldParser(InstrumentationPageResponse.REACTIVE_POWER_UNIT)),
    apparentPowerQuadraticPhaseA(19, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerQuadraticPhaseB(20, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerQuadraticPhaseC(21, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerQuadraticThreePhase(22, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerVectorialPhaseA(23, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerVectorialPhaseB(24, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerVectorialPhaseC(25, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    apparentPowerVectorialThreePhase(26, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    distortivePowerPhaseA(27, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    distortivePowerPhaseB(28, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    distortivePowerPhaseC(29, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    distortivePowerThreePhase(30, new FloatFieldParser(InstrumentationPageResponse.APPARENT_POWER_UNIT)),
    cosinusFiPhaseA(31, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    cosinusFiPhaseB(32, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    cosinusFiPhaseC(33, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    cosinusFiThreePhase(34, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    reactiveCharacteristicsPhaseA(35, new SimpleFieldParser<>(ReactivePowerCharacteristicField.class)),
    reactiveCharacteristicsPhaseB(36, new SimpleFieldParser<>(ReactivePowerCharacteristicField.class)),
    reactiveCharacteristicsPhaseC(37, new SimpleFieldParser<>(ReactivePowerCharacteristicField.class)),
    reactiveCharacteristicsThreePhase(38, new SimpleFieldParser<>(ReactivePowerCharacteristicField.class)),
    powerFactorRMSPhaseA(39, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    powerFactorRMSPhaseB(40, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    powerFactorRMSPhaseC(41, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    powerFactorRMSThreePhase(42, new FloatFieldParser(InstrumentationPageResponse.UNDEFINED_UNIT)),
    angularOffsetBetweenVoltageAndCurrentPhaseA(43, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    angularOffsetBetweenVoltageAndCurrentPhaseB(44, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    angularOffsetBetweenVoltageAndCurrentPhaseC(45, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    temperature(46, new FloatFieldParser(InstrumentationPageResponse.TEMPERATURE_UNIT)),
    frequency(47, new FloatFieldParser(InstrumentationPageResponse.FREQUENCY_UNIT)),
    connectionType(48, new SimpleFieldParser<>(ConnectionTypeField.class)),
    dummyData(49, new PaddingDataParser(InstrumentationPageResponse.NOT_SUPPORTED_DATA_LENGTH)),
    meterModel(50, new BcdEncodedFieldParser(InstrumentationPageResponse.METER_MODEL_LENGTH)),
    voltageAnglePhaseA(51, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageAnglePhaseB(52, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageAnglePhaseC(53, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageAngleBetweenPhaseAB(54, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageAngleBetweenPhaseBC(55, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageAngleBetweenPhaseCA(56, new FloatFieldParser(InstrumentationPageResponse.ANGLE_UNIT)),
    voltageHarmonicDistortionPhaseA(57, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    voltageHarmonicDistortionPhaseB(58, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    voltageHarmonicDistortionPhaseC(59, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    currentHarmonicDistortionPhaseA(60, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    currentHarmonicDistortionPhaseB(61, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    currentHarmonicDistortionPhaseC(62, new FloatFieldParser(InstrumentationPageResponse.DISTORTION_UNIT)),
    quantityConversionIndicator(63, new SimpleFieldParser<>(QuantityConversionIndicatorField.class));

    private final int code;
    private final FieldParser fieldParser;

    InstrumentationPageFields(int code, FieldParser fieldParser) {
        this.code = code;
        this.fieldParser = fieldParser;
    }

    public FieldParser getFieldParser() {
        return fieldParser;
    }

    public int getCode() {
        return code;
    }

    public static InstrumentationPageFields fromCode(int code) {
        for (InstrumentationPageFields instrumentationPageFields : values()) {
            if (instrumentationPageFields.getCode() == code) {
                return instrumentationPageFields;
            }
        }
        return null;
    }
}