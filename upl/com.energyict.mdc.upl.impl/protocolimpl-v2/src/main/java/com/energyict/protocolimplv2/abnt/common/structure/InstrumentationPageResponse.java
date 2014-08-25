package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.FloatField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.ReactivePowerCharacteristicField;
import com.energyict.protocolimplv2.abnt.common.structure.field.UnitConversionIndicatorField;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class InstrumentationPageResponse extends Data<InstrumentationPageResponse> {

    private static final int NULL_DATA_LENGTH = 6;
    private static final Integer[] NOT_SUPPORTED_DATA_LENGTHS = new Integer[]{12, 4, 48, 18, 24};

    private FloatField voltagePhaseA;   //TODO: check translations are correct
    private FloatField voltagePhaseB;
    private FloatField voltagePhaseC;
    private FloatField currentPhaseA;
    private FloatField currentPhaseB;
    private FloatField currentPhaseC;
    private FloatField activePowerPhaseA;
    private FloatField activePowerPhaseB;
    private FloatField activePowerPhaseC;
    private FloatField activePowerAllPhases;
    private FloatField reactivePowerPhaseA;
    private FloatField reactivePowerPhaseB;
    private FloatField reactivePowerPhaseC;
    private FloatField reactivePowerAllPhases;
    private FloatField apparentPowerPhaseA;
    private FloatField apparentPowerPhaseB;
    private FloatField apparentPowerPhaseC;
    private FloatField apparentPowerAllPhases;
    private ReactivePowerCharacteristicField reactiveCharacteristicsPhaseA; //TODO: usage of this field?
    private ReactivePowerCharacteristicField reactiveCharacteristicsPhaseB;
    private ReactivePowerCharacteristicField reactiveCharacteristicsPhaseC;
    private ReactivePowerCharacteristicField reactiveCharacteristicsAllPhase;
    private FloatField powerFactorRMSPhaseA;
    private FloatField powerFactorRMSPhaseB;
    private FloatField powerFactorRMSPhaseC;
    private FloatField powerFactorRMSAllPhase;
    private FloatField phaseShiftBetweenVoltageAndCurrentPhaseA;
    private FloatField phaseShiftBetweenVoltageAndCurrentPhaseB;
    private FloatField phaseShiftBetweenVoltageAndCurrentPhaseC;
    private FloatField voltageAnglePhaseA;
    private FloatField voltageAnglePhaseB;
    private FloatField voltageAnglePhaseC;
    private FloatField voltageAngleBetweenPhaseAB;
    private FloatField voltageAngleBetweenPhaseBC;
    private FloatField voltageAngleBetweenPhaseCA;
    private UnitConversionIndicatorField unitConversionIndicator;  //TODO: usage of this field?

    public InstrumentationPageResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        voltagePhaseA = new FloatField();
        voltagePhaseB = new FloatField();
        voltagePhaseC = new FloatField();
        currentPhaseA = new FloatField();
        currentPhaseB = new FloatField();
        currentPhaseC = new FloatField();
        activePowerPhaseA = new FloatField();
        activePowerPhaseB = new FloatField();
        activePowerPhaseC = new FloatField();
        activePowerAllPhases = new FloatField();
        reactivePowerPhaseA = new FloatField();
        reactivePowerPhaseB = new FloatField();
        reactivePowerPhaseC = new FloatField();
        reactivePowerAllPhases = new FloatField();
        apparentPowerPhaseA = new FloatField();
        apparentPowerPhaseB = new FloatField();
        apparentPowerPhaseC = new FloatField();
        apparentPowerAllPhases = new FloatField();
        reactiveCharacteristicsPhaseA = new ReactivePowerCharacteristicField();
        reactiveCharacteristicsPhaseB = new ReactivePowerCharacteristicField();
        reactiveCharacteristicsPhaseC = new ReactivePowerCharacteristicField();
        reactiveCharacteristicsAllPhase = new ReactivePowerCharacteristicField();
        powerFactorRMSPhaseA = new FloatField();
        powerFactorRMSPhaseB = new FloatField();
        powerFactorRMSPhaseC = new FloatField();
        powerFactorRMSAllPhase = new FloatField();
        phaseShiftBetweenVoltageAndCurrentPhaseA = new FloatField();
        phaseShiftBetweenVoltageAndCurrentPhaseB = new FloatField();
        phaseShiftBetweenVoltageAndCurrentPhaseC = new FloatField();
        voltageAnglePhaseA = new FloatField();
        voltageAnglePhaseB = new FloatField();
        voltageAnglePhaseC = new FloatField();
        voltageAngleBetweenPhaseAB = new FloatField();
        voltageAngleBetweenPhaseBC = new FloatField();
        voltageAngleBetweenPhaseCA = new FloatField();
        unitConversionIndicator = new UnitConversionIndicatorField();
    }

    @Override
    public InstrumentationPageResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        ptr += NULL_DATA_LENGTH;

        voltagePhaseA.parse(rawData, ptr);
        ptr += voltagePhaseA.getLength();
        voltagePhaseB.parse(rawData, ptr);
        ptr += voltagePhaseB.getLength();
        voltagePhaseC.parse(rawData, ptr);
        ptr += voltagePhaseC.getLength();

        ptr += NOT_SUPPORTED_DATA_LENGTHS[0];

        currentPhaseA.parse(rawData, ptr);
        ptr += currentPhaseA.getLength();
        currentPhaseB.parse(rawData, ptr);
        ptr += currentPhaseB.getLength();
        currentPhaseC.parse(rawData, ptr);
        ptr += currentPhaseC.getLength();

        ptr += NOT_SUPPORTED_DATA_LENGTHS[1];

        activePowerPhaseA.parse(rawData, ptr);
        ptr += activePowerPhaseA.getLength();
        activePowerPhaseB.parse(rawData, ptr);
        ptr += activePowerPhaseB.getLength();
        activePowerPhaseC.parse(rawData, ptr);
        ptr += activePowerPhaseC.getLength();
        activePowerAllPhases.parse(rawData, ptr);
        ptr += activePowerAllPhases.getLength();

        reactivePowerPhaseA.parse(rawData, ptr);
        ptr += reactivePowerPhaseA.getLength();
        reactivePowerPhaseB.parse(rawData, ptr);
        ptr += reactivePowerPhaseB.getLength();
        reactivePowerPhaseC.parse(rawData, ptr);
        ptr += reactivePowerPhaseC.getLength();
        reactivePowerAllPhases.parse(rawData, ptr);
        ptr += reactivePowerAllPhases.getLength();

        apparentPowerPhaseA.parse(rawData, ptr);
        ptr += apparentPowerPhaseA.getLength();
        apparentPowerPhaseB.parse(rawData, ptr);
        ptr += apparentPowerPhaseB.getLength();
        apparentPowerPhaseC.parse(rawData, ptr);
        ptr += apparentPowerPhaseC.getLength();
        apparentPowerAllPhases.parse(rawData, ptr);
        ptr += apparentPowerAllPhases.getLength();

        ptr += NOT_SUPPORTED_DATA_LENGTHS[2];

        reactiveCharacteristicsPhaseA.parse(rawData, ptr);
        ptr += reactiveCharacteristicsPhaseA.getLength();
        reactiveCharacteristicsPhaseB.parse(rawData, ptr);
        ptr += reactiveCharacteristicsPhaseB.getLength();
        reactiveCharacteristicsPhaseC.parse(rawData, ptr);
        ptr += reactiveCharacteristicsPhaseC.getLength();
        reactiveCharacteristicsAllPhase.parse(rawData, ptr);
        ptr += reactiveCharacteristicsAllPhase.getLength();

        powerFactorRMSPhaseA.parse(rawData, ptr);
        ptr += powerFactorRMSPhaseA.getLength();
        powerFactorRMSPhaseB.parse(rawData, ptr);
        ptr += powerFactorRMSPhaseB.getLength();
        powerFactorRMSPhaseC.parse(rawData, ptr);
        ptr += powerFactorRMSPhaseC.getLength();
        powerFactorRMSAllPhase.parse(rawData, ptr);
        ptr += powerFactorRMSAllPhase.getLength();

        phaseShiftBetweenVoltageAndCurrentPhaseA.parse(rawData, ptr);
        ptr += phaseShiftBetweenVoltageAndCurrentPhaseA.getLength();
        phaseShiftBetweenVoltageAndCurrentPhaseB.parse(rawData, ptr);
        ptr += phaseShiftBetweenVoltageAndCurrentPhaseB.getLength();
        phaseShiftBetweenVoltageAndCurrentPhaseC.parse(rawData, ptr);
        ptr += phaseShiftBetweenVoltageAndCurrentPhaseC.getLength();

        ptr += NOT_SUPPORTED_DATA_LENGTHS[3];

        voltageAnglePhaseA.parse(rawData, ptr);
        ptr += voltageAnglePhaseA.getLength();
        voltageAnglePhaseB.parse(rawData, ptr);
        ptr += voltageAnglePhaseB.getLength();
        voltageAnglePhaseC.parse(rawData, ptr);
        ptr += voltageAnglePhaseC.getLength();

        voltageAngleBetweenPhaseAB.parse(rawData, ptr);
        ptr += voltageAngleBetweenPhaseAB.getLength();
        voltageAngleBetweenPhaseBC.parse(rawData, ptr);
        ptr += voltageAngleBetweenPhaseBC.getLength();
        voltageAngleBetweenPhaseCA.parse(rawData, ptr);
        ptr += voltageAngleBetweenPhaseCA.getLength();

        ptr += NOT_SUPPORTED_DATA_LENGTHS[4];

        unitConversionIndicator.parse(rawData, ptr);
        return this;
    }

    public FloatField getVoltagePhaseA() {
        return voltagePhaseA;
    }

    public FloatField getVoltagePhaseB() {
        return voltagePhaseB;
    }

    public FloatField getVoltagePhaseC() {
        return voltagePhaseC;
    }

    public FloatField getCurrentPhaseA() {
        return currentPhaseA;
    }

    public FloatField getCurrentPhaseB() {
        return currentPhaseB;
    }

    public FloatField getCurrentPhaseC() {
        return currentPhaseC;
    }

    public FloatField getActivePowerPhaseA() {
        return activePowerPhaseA;
    }

    public FloatField getActivePowerPhaseB() {
        return activePowerPhaseB;
    }

    public FloatField getActivePowerPhaseC() {
        return activePowerPhaseC;
    }

    public FloatField getActivePowerAllPhases() {
        return activePowerAllPhases;
    }

    public FloatField getReactivePowerPhaseA() {
        return reactivePowerPhaseA;
    }

    public FloatField getReactivePowerPhaseB() {
        return reactivePowerPhaseB;
    }

    public FloatField getReactivePowerPhaseC() {
        return reactivePowerPhaseC;
    }

    public FloatField getReactivePowerAllPhases() {
        return reactivePowerAllPhases;
    }

    public FloatField getApparentPowerPhaseA() {
        return apparentPowerPhaseA;
    }

    public FloatField getApparentPowerPhaseB() {
        return apparentPowerPhaseB;
    }

    public FloatField getApparentPowerPhaseC() {
        return apparentPowerPhaseC;
    }

    public FloatField getApparentPowerAllPhases() {
        return apparentPowerAllPhases;
    }

    public ReactivePowerCharacteristicField getReactiveCharacteristicsPhaseA() {
        return reactiveCharacteristicsPhaseA;
    }

    public ReactivePowerCharacteristicField getReactiveCharacteristicsPhaseB() {
        return reactiveCharacteristicsPhaseB;
    }

    public ReactivePowerCharacteristicField getReactiveCharacteristicsPhaseC() {
        return reactiveCharacteristicsPhaseC;
    }

    public ReactivePowerCharacteristicField getReactiveCharacteristicsAllPhase() {
        return reactiveCharacteristicsAllPhase;
    }

    public FloatField getPowerFactorRMSPhaseA() {
        return powerFactorRMSPhaseA;
    }

    public FloatField getPowerFactorRMSPhaseB() {
        return powerFactorRMSPhaseB;
    }

    public FloatField getPowerFactorRMSPhaseC() {
        return powerFactorRMSPhaseC;
    }

    public FloatField getPowerFactorRMSAllPhase() {
        return powerFactorRMSAllPhase;
    }

    public FloatField getPhaseShiftBetweenVoltageAndCurrentPhaseA() {
        return phaseShiftBetweenVoltageAndCurrentPhaseA;
    }

    public FloatField getPhaseShiftBetweenVoltageAndCurrentPhaseB() {
        return phaseShiftBetweenVoltageAndCurrentPhaseB;
    }

    public FloatField getPhaseShiftBetweenVoltageAndCurrentPhaseC() {
        return phaseShiftBetweenVoltageAndCurrentPhaseC;
    }

    public FloatField getVoltageAnglePhaseB() {
        return voltageAnglePhaseB;
    }

    public FloatField getVoltageAnglePhaseC() {
        return voltageAnglePhaseC;
    }

    public FloatField getVoltageAngleBetweenPhaseAB() {
        return voltageAngleBetweenPhaseAB;
    }

    public FloatField getVoltageAngleBetweenPhaseBC() {
        return voltageAngleBetweenPhaseBC;
    }

    public FloatField getVoltageAngleBetweenPhaseCA() {
        return voltageAngleBetweenPhaseCA;
    }

    public UnitConversionIndicatorField getUnitConversionIndicator() {
        return unitConversionIndicator;
    }

    public FloatField getVoltageAnglePhaseA() {
        return voltageAnglePhaseA;
    }
}