/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.EMCO;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.EMCO.frame.RegisterRequestFrame;
import com.energyict.protocolimpl.EMCO.frame.RegisterResponseFrame;
import com.energyict.protocolimpl.EMCO.frame.ResponseFrame;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

public class ObisCodeMapper {

    private List<RegisterMapping> registerMapping = new ArrayList<>();

    // Map containing the mapping of unit string to EIServer units.
    private Map<String, Unit> unitTextHashMap = new HashMap<>();

    // Mapping of Register Unit Number to its EIServer Unit -- used to prevent the same unit register is read out multiple times in one comm session.
    private Map<String, Unit> unitHashMap = new HashMap<>();
    private Map<BigDecimal, String> totalizerAssignmentMap = new HashMap<>(5);
    private FP93 meterProtocol;

    public ObisCodeMapper(FP93 meterProtocol) {
        this.meterProtocol = meterProtocol;
        initRegisterMapping();
        initUnitMapping();
        initTotalizerAssignment();
    }

    /**
     * Maps the obiscodes to an S-FP93 Communication Register
     */
    private void initRegisterMapping() {
        registerMapping.add(new RegisterMapping("1.0.1.7.1.255", 1, Unit.get(BaseUnit.OHM), "RTD #1 resistance"));
        registerMapping.add(new RegisterMapping("1.0.1.7.2.255", 2, Unit.get(BaseUnit.OHM), "RTD #2 resistance"));

        registerMapping.add(new RegisterMapping("1.0.1.8.1.255", 3, Unit.get(BaseUnit.AMPERE, -3), "Analog input #1 current"));
        registerMapping.add(new RegisterMapping("1.0.1.8.2.255", 4, Unit.get(BaseUnit.AMPERE, -3), "Analog input #2 current"));
        registerMapping.add(new RegisterMapping("1.0.1.8.3.255", 5, Unit.get(BaseUnit.AMPERE, -3), "Analog input #3 current"));
        registerMapping.add(new RegisterMapping("1.0.1.8.4.255", 6, Unit.get(BaseUnit.AMPERE, -3), "Analog input #4 current"));

        registerMapping.add(new RegisterMapping("1.0.0.6.2.255", 7, Unit.get(BaseUnit.HERTZ), "Frequency"));
        registerMapping.add(new RegisterMapping("7.0.43.0.1.255", 8, "Flow direction (1 = forward, 0 = reverse)"));

        // TEMPERATURE
        registerMapping.add(new RegisterMapping("7.0.41.0.1.255", 10, 19, "Temperature #1"));
        registerMapping.add(new RegisterMapping("7.0.41.0.2.255",11, 19, "Average temperature #1"));
        registerMapping.add(new RegisterMapping("7.0.41.0.3.255", 12, 19, "Minimum temperature #1"));
        registerMapping.add(new RegisterMapping("7.0.41.0.4.255", 13, 19, "Maximum temperature #1"));
        registerMapping.add(new RegisterMapping("7.1.41.0.1.255", 14, 19, "Temperature #2"));
        registerMapping.add(new RegisterMapping("7.1.41.0.2.255",15, 19, "Average temperature #2"));
        registerMapping.add(new RegisterMapping("7.1.41.0.3.255", 16, 19, "Minimum temperature #2"));
        registerMapping.add(new RegisterMapping("7.1.41.0.4.255", 17, 19, "Maximum temperature #2"));
        registerMapping.add(new RegisterMapping("7.2.41.0.1.255", 18, 19, "Differential temperature: (temp #1 - temp #2) or superheat"));
        registerMapping.add(new RegisterMapping("7.0.41.0.0.255", 19, "Temperature unit"));

        // PRESSURE
        registerMapping.add(new RegisterMapping("7.0.42.0.1.255", 20, 24, "Pressure"));
        registerMapping.add(new RegisterMapping("7.0.42.0.2.255", 21, 24, "Average pressure"));
        registerMapping.add(new RegisterMapping("7.0.42.0.3.255", 22, 24, "Minimum pressure"));
        registerMapping.add(new RegisterMapping("7.0.42.0.4.255", 23, 24, "Maximum pressure"));
        registerMapping.add(new RegisterMapping("7.0.42.0.0.255", 24, "Pressure unit"));

        // DENSITY
        registerMapping.add(new RegisterMapping("7.0.45.0.1.255", 25, 26, "Density"));
        registerMapping.add(new RegisterMapping("7.0.45.0.0.255", 26, "Density unit"));

        //VOLUME FLOW
        registerMapping.add(new RegisterMapping("7.0.1.0.1.255", 27, 38, "Specific volume"));
        registerMapping.add(new RegisterMapping("7.0.1.0.2.255", 30, 38, "Volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.3.255", 31, 38, "Average volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.4.255", 32, 38, "Minimum volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.5.255", 33, 38, "Maximum volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.6.255", 34, 38, "Temperature compensated volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.7.255", 35, 38, "Average temperature compensated volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.8.255", 36, 38, "Minimum temperature compensated volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.9.255", 37, 38, "Maximum temperature compensated volume flow"));
        registerMapping.add(new RegisterMapping("7.0.1.0.0.255", 38, "Volume flow unit"));

        // MASS FLOW
        registerMapping.add(new RegisterMapping("7.0.61.0.1.255", 40, 44, "Mass flow"));
        registerMapping.add(new RegisterMapping("7.0.61.0.2.255", 41, 44, "Average mass flow"));
        registerMapping.add(new RegisterMapping("7.0.61.0.3.255", 42, 44, "Minimum mass flow"));
        registerMapping.add(new RegisterMapping("7.0.61.0.4.255", 43, 44, "Maximum mass flow"));
        registerMapping.add(new RegisterMapping("7.0.61.0.0.255", 44, "Mass flow unit"));

        // ENERGY FLOW
        registerMapping.add(new RegisterMapping("7.0.31.0.1.255", 45, 49, "Energy flow"));
        registerMapping.add(new RegisterMapping("7.0.31.0.2.255", 46, 49, "Average energy flow"));
        registerMapping.add(new RegisterMapping("7.0.31.0.3.255", 47, 49, "Minimum energy flow"));
        registerMapping.add(new RegisterMapping("7.0.31.0.4.255", 48, 49, "Maximum energy flow"));
        registerMapping.add(new RegisterMapping("7.0.31.0.0.255", 49, "Energy flow unit"));

        registerMapping.add(new RegisterMapping("1.0.0.8.0.255", 50, Unit.get(BaseUnit.SECOND), "Calculation interval, seconds"));
        registerMapping.add(new RegisterMapping("1.1.1.8.1.255", 51, Unit.get(BaseUnit.AMPERE, -3), "Analog output current, mA"));

        // TOTALIZERS
        registerMapping.add(new RegisterMapping("7.0.96.5.1.255", 52, "Non-resettable totalizer #1 (forward)"));
        registerMapping.add(new RegisterMapping("7.0.96.5.2.255", 53, "Resettable totalizer #1 (forward)"));
        registerMapping.add(new RegisterMapping("7.1.96.5.1.255", 54, "Non-resettable totalizer #2 (reverse)"));
        registerMapping.add(new RegisterMapping("7.1.96.5.2.255", 55, "Resettable totalizer #2 (reverse)"));
        registerMapping.add(new RegisterMapping("7.2.96.5.1.255", 56, "Totalizer #1 assignment"));
        registerMapping.add(new RegisterMapping("7.2.96.5.2.255", 57, "Totalizer #1 scale factor"));
        registerMapping.add(new RegisterMapping("7.2.96.5.3.255", 58, "Totalizer #2 assignment"));
        registerMapping.add(new RegisterMapping("7.2.96.5.4.255", 59, "Totalizer #2 scale factor"));

        registerMapping.add(new RegisterMapping("7.0.44.0.1.255", 60, "Raw velocity"));
        registerMapping.add(new RegisterMapping("7.0.51.0.1.255", 61, "Profile factor"));
        registerMapping.add(new RegisterMapping("7.0.51.0.2.255", 62, "Obscuration factor"));
        registerMapping.add(new RegisterMapping("7.0.44.0.2.255", 63, "Line velocity"));
        registerMapping.add(new RegisterMapping("7.0.0.13.8.255", 64, "Viscosity"));
        registerMapping.add(new RegisterMapping("7.0.52.0.1.255", 65, "Reynolds number"));
        registerMapping.add(new RegisterMapping("7.0.53.0.1.255", 66, "Compressibility factor"));
        registerMapping.add(new RegisterMapping("7.0.53.0.2.255", 67, "Supercompressibility factor"));

        registerMapping.add(new RegisterMapping("5.0.0.4.3.255", 68, "Enthalpy (temperature #1)"));
        registerMapping.add(new RegisterMapping("5.1.0.4.3.255", 69, "Enthalpy (temperature #2)"));

        registerMapping.add(new RegisterMapping("0.0.1.0.0.255", 70, "Current time - seconds since 12:00 midnight, 1/1/80"));

        // TIME FAULT FLAG SET
        registerMapping.add(new RegisterMapping("0.0.96.11.0.255", 71, "Time changed flag set"));
        registerMapping.add(new RegisterMapping("0.1.96.11.0.255", 72, "Time statistical values last cleared"));
        registerMapping.add(new RegisterMapping("0.2.96.11.0.255", 73, "Time totalizers last cleared"));
        registerMapping.add(new RegisterMapping("0.3.96.11.0.255", 74, "Time of power failure"));
        registerMapping.add(new RegisterMapping("0.4.96.11.0.255", 75, "Time of relay output rate alarm"));
        registerMapping.add(new RegisterMapping("0.5.96.11.0.255", 76, "Time of analog output alarm"));
        registerMapping.add(new RegisterMapping("0.6.96.11.0.255", 77, "Time of flow input out of range alarm"));
        registerMapping.add(new RegisterMapping("0.7.96.11.0.255", 78, "Time of temperature input out of range alarm"));
        registerMapping.add(new RegisterMapping("0.8.96.11.0.255", 79, "Time of temperature input #2 out of range alarm"));
        registerMapping.add(new RegisterMapping("0.9.96.11.0.255", 80, "Time of pressure input out of range alarm"));
        registerMapping.add(new RegisterMapping("0.10.96.11.0.255", 81, "Time of A/D converter overrange alarm"));
        registerMapping.add(new RegisterMapping("0.11.96.11.0.255", 82, "Time of battery fault"));
        registerMapping.add(new RegisterMapping("0.12.96.11.0.255", 83, "Time of EEPROM checksum fault"));
        registerMapping.add(new RegisterMapping("0.13.96.11.0.255", 84, "Time of ROM checksum fault"));
        registerMapping.add(new RegisterMapping("0.14.96.11.0.255", 85, "Time of RAM read/write fault"));

        // FAULT FLAGS
        registerMapping.add(new RegisterMapping("0.0.97.97.0.255", 90, "Fault flags"));
        registerMapping.add(new RegisterMapping("0.0.10.0.1.255", 91, "Clear faults and changed flag"));
        registerMapping.add(new RegisterMapping("0.1.10.0.1.255", 92, "Clear statistical values"));
        registerMapping.add(new RegisterMapping("0.2.10.0.1.255", 93, "Clear resettable totalizers"));

        registerMapping.add(new RegisterMapping("1.0.0.2.0.255", 94, "Unit information block"));
    }

    /**
     * Maps a string representation of an unit to an Unit object.
     */
    private void initUnitMapping() {
        // Temperature units
        unitTextHashMap.put("deg F", Unit.get(BaseUnit.FAHRENHEIT));
        unitTextHashMap.put("deg C", Unit.get(BaseUnit.DEGREE_CELSIUS));
        unitTextHashMap.put("K", Unit.get(BaseUnit.KELVIN));

        // Pressure units
        unitTextHashMap.put("psi", Unit.get(BaseUnit.POUNDPERSQUAREINCH));
        unitTextHashMap.put("bars", Unit.get(BaseUnit.BAR));
        unitTextHashMap.put("kg cm^2", Unit.get(BaseUnit.GRAMPERSQUARECENTIMETER, 3));
        unitTextHashMap.put("mmHg", Unit.get(BaseUnit.METERMERCURY, -3));

        // Density units
        // NO density units present - will use always Undefined as unit.

        // Volume flow units
        unitTextHashMap.put("ft^3/h", Unit.get(BaseUnit.CUBICFEETPERHOUR));
        unitTextHashMap.put("ft^3/d", Unit.get(BaseUnit.CUBICFEETPERDAY));
        unitTextHashMap.put("gal/h", Unit.get(BaseUnit.US_GALLONPERHOUR));
        unitTextHashMap.put("cc^3/h", Unit.get(BaseUnit.CUBICMETERPERHOUR, -6));
        unitTextHashMap.put("cc^3/d", Unit.get(BaseUnit.CUBICMETERPERDAY, -6));
        unitTextHashMap.put("l/h", Unit.get(BaseUnit.LITERPERHOUR));
        unitTextHashMap.put("m^3/h", Unit.get(BaseUnit.CUBICMETERPERHOUR));
        unitTextHashMap.put("m^3/d", Unit.get(BaseUnit.CUBICMETERPERDAY));

        // Mass flow units
        unitTextHashMap.put("ton/h", Unit.get(BaseUnit.TONPERHOUR));
        unitTextHashMap.put("g/s", Unit.get(BaseUnit.KILOGRAMPERSECOND, -3));
        unitTextHashMap.put("g/h", Unit.get(BaseUnit.KILOGRAMPERHOUR, -3));
        unitTextHashMap.put("kg/s", Unit.get(BaseUnit.KILOGRAMPERSECOND));
        unitTextHashMap.put("kg/h", Unit.get(BaseUnit.KILOGRAMPERHOUR));

        // Energy flow units
        unitTextHashMap.put("kJ/h", Unit.get(BaseUnit.JOULEPERHOUR, 3));
    }

    private void initTotalizerAssignment() {
        totalizerAssignmentMap.put(BigDecimal.ZERO, "None");
        totalizerAssignmentMap.put(BigDecimal.ONE, "Volume flow");
        totalizerAssignmentMap.put(new BigDecimal(2), "Compensated volume flow");
        totalizerAssignmentMap.put(new BigDecimal(3), "Mass flow");
        totalizerAssignmentMap.put(new BigDecimal(4), "Energy flow");
    }

     /**
     * Try to get the matching registerMapping for a given obisCode
     *
     * @param obis: the given obiscode
     * @return a matching Object ID
     */
    public RegisterMapping searchRegisterMapping(ObisCode obis) throws NoSuchRegisterException {
        for (RegisterMapping regMapping : registerMapping) {
            if (obis.equals(regMapping.getObisCode())) {
                return regMapping;
            }
        }
        throw new NoSuchRegisterException("ObisCode " + obis.toString() + " is not supported!");
    }

     /**
     * Try to get the matching registerMapping for a given object Id
     *
     * @param objectId: the given obiscode
     * @return a matching Object ID
     */
    public RegisterMapping searchRegisterMapping(int objectId) throws NoSuchRegisterException {
        for (RegisterMapping regMapping : registerMapping) {
            if (objectId == regMapping.getObjectId()) {
                return regMapping;
            }
        }
        throw new NoSuchRegisterException("Register reg id "+objectId+" is not supported!");
    }

    /**
     * Read out the register with the given obiscode from the device
     * @param obisCode
     * @return
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

        RegisterMapping mapping = searchRegisterMapping(obisCode);
        RegisterRequestFrame requestFrame = new RegisterRequestFrame(meterProtocol.getDeviceID(), mapping.getObjectId());
        RegisterResponseFrame responseFrame = (RegisterResponseFrame) meterProtocol.getConnection().sendAndReceiveResponse(requestFrame);

        Unit unit = Unit.getUndefined();

        if (mapping.getUnit() != null) { // Predefined fixed unit
            unit = mapping.getUnit();
        } else if (mapping.getUnitRegisterId() != 0) { // Unit is stored in a unit register
            unit = retrieveUnitOfRegister(mapping.getUnitRegisterId());
        }

        switch (responseFrame.getDataType()) {
            case RegisterResponseFrame.TYPE_FLOATING_POINT:
                return new RegisterValue(obisCode, new Quantity(responseFrame.getValue(), unit), null, new Date());
            case RegisterResponseFrame.TYPE_HEX:
                return new RegisterValue(obisCode, new Quantity(responseFrame.getBitMask(), Unit.getUndefined()), null, null, null, new Date(), 0, meterProtocol.getEventLog().getEventDescriptions(responseFrame.getBitMask()));
            case RegisterResponseFrame.TYPE_LONG:
                if (obisCode.equals(ObisCode.fromString("7.2.96.5.1.255")) || obisCode.equals(ObisCode.fromString("7.2.96.5.3.255"))) {
                    // TOTALIZER ASSIGNMENT REGISTER
                    String s = totalizerAssignmentMap.get(responseFrame.getValue());
                    String text = (s == null) ? "Invalid totalizer assignment" : "Totalizer assignment: " + s;
                    return new RegisterValue(obisCode, new Quantity(responseFrame.getValue(), unit), null, null, null, new Date(), 0, text);
                } else if (obisCode.equals(ObisCode.fromString("0.0.1.0.0.255")) ||
                        ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0).equals(ObisCode.fromString("0.0.96.11.0.255"))) {
                    // TIME REGISTER
                    Calendar calendar = ProtocolUtils.getCleanGMTCalendar();
                    if (responseFrame.getValue().longValue() != 0) {
                        calendar.setTimeInMillis((315532800 + responseFrame.getValue().longValue()) * 1000);    // [ Number of seconds [1 jan 1970 - 1 jan 1980] + seconds since 1980 ] * 1000
                        return new RegisterValue(obisCode, new Quantity(responseFrame.getValue(), unit), null, null, null, new Date(), 0, calendar.getTime().toString());
                    } else {
                        return new RegisterValue(obisCode, new Quantity(responseFrame.getValue(), unit), null, new Date());
                    }
                } else {
                    return new RegisterValue(obisCode, new Quantity(responseFrame.getValue(), unit), null, new Date());
                }
            case RegisterResponseFrame.TYPE_STRING:
                return new RegisterValue(obisCode, responseFrame.getText());
            default:
                throw new ProtocolConnectionException("Invalid state of dataType.");
        }
    }

    /**
     * Some registers do not have a fixed unit, but the unit can be configured.
     * The unit of these registers can be read out in string representation from an 'unit register'.
     * @param unitRegisterId    The object id of the unit register, containing the unit.
     * @return
     */
    private Unit retrieveUnitOfRegister(int unitRegisterId) throws IOException {
        Unit unit = unitHashMap.get(Integer.toString(unitRegisterId));
        if (unit == null) {
            RegisterRequestFrame requestFrame = new RegisterRequestFrame(meterProtocol.getDeviceID(), unitRegisterId);
            ResponseFrame responseFrame = meterProtocol.getConnection().sendAndReceiveResponse(requestFrame);

            String unitString;
            if (responseFrame.getResponseType() != ResponseFrame.REGISTER_RESPONSE) {
                throw new NoSuchElementException("Failure while reading out unit register " + unitRegisterId);
            }
            unitString = ((RegisterResponseFrame) responseFrame).getText();
            unit = unitTextHashMap.get(unitString);
            String unitRegInfo = searchRegisterMapping(unitRegisterId).getDescription();
            if (unit == null) {
                unitHashMap.put(Integer.toString(unitRegisterId), Unit.getUndefined());
                meterProtocol.getLogger().log(Level.INFO, unitRegInfo + " " + unitString + " has no corresponding EIServer unit. Undefined will be used.");
                return Unit.getUndefined();
            } else {
                unitHashMap.put(Integer.toString(unitRegisterId), unit);
                meterProtocol.getLogger().log(Level.INFO, unitRegInfo + " " + unitString + " will be mapped to EIServer unit " + unit + ".");
                return unit;
            }
        }
        return unit;
    }
}