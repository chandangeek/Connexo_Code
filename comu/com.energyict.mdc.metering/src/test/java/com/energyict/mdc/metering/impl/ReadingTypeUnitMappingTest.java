/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import org.fest.assertions.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ReadingTypeUnitMappingTest {

    private final Set<Unit> unmappableUnits = new HashSet<>(Arrays.asList(
            Unit.get(BaseUnit.YEAR),
            Unit.get(BaseUnit.MONTH),
            Unit.get(BaseUnit.WEEK),
            Unit.get(BaseUnit.DAY),
            Unit.get(BaseUnit.CUBICMETERPERDAY),
            Unit.get(BaseUnit.NORMALCUBICMETERPERDAY),
            Unit.get(BaseUnit.NEWTONMETER),
            Unit.get(BaseUnit.JOULEPERHOUR),
            Unit.get(BaseUnit.VOLTPERMETER),
            Unit.get(BaseUnit.ACTIVEENERGY),
            Unit.get(BaseUnit.REACTIVEENERGY),
            Unit.get(BaseUnit.APPARENTENERGY),
            Unit.get(BaseUnit.KILOGRAMPERSECOND),
            Unit.get(BaseUnit.METERPERHOUR),
            Unit.get(BaseUnit.PERHOUR),
            Unit.get(BaseUnit.JOULEPERNORMALCUBICMETER),
            Unit.get(BaseUnit.WATTHOURPERNORMALCUBICMETER),
            Unit.get(BaseUnit.TON),
            Unit.get(BaseUnit.KILOGRAMPERHOUR),
            Unit.get(BaseUnit.TONPERHOUR),
            Unit.get(BaseUnit.WATTPERSQUAREMETER),
            Unit.get(BaseUnit.FEET),
            Unit.get(BaseUnit.FEETPERSECOND),
            Unit.get(BaseUnit.CUBICFEETPERDAY),
            Unit.get(BaseUnit.THERMPERDAY),
            Unit.get(BaseUnit.THERMPERHOUR),
            Unit.get(BaseUnit.ACREFEET),
            Unit.get(BaseUnit.ACREFEETPERHOUR),
            Unit.get(BaseUnit.TOTALHARMONICDISTORTIONV_IEEE),
            Unit.get(BaseUnit.TOTALHARMONICDISTORTIONI_IEEE),
            Unit.get(BaseUnit.TOTALHARMONICDISTORTIONV_IC),
            Unit.get(BaseUnit.TOTALHARMONICDISTORTIONI_IC),
            Unit.get(BaseUnit.PASCALPERHOUR),
            Unit.get(BaseUnit.GRAMPERSQUARECENTIMETER),
            Unit.get(BaseUnit.METERMERCURY),
            Unit.get(BaseUnit.INCHESMERCURY),
            Unit.get(BaseUnit.INCHESWATER),
            Unit.get(BaseUnit.GALLON),
            Unit.get(BaseUnit.GALLONPERHOUR),
            Unit.get(BaseUnit.INCH),
            Unit.get(BaseUnit.AMPEREHOUR),
            Unit.get(BaseUnit.VOLTHOUR),
            Unit.get(BaseUnit.FAHRENHEIT),
            Unit.get(BaseUnit.CUBICINCH),
            Unit.get(BaseUnit.CUBICINCHPERHOUR),
            Unit.get(BaseUnit.YARD),
            Unit.get(BaseUnit.CUBICYARD),
            Unit.get(BaseUnit.CUBICYARDPERHOUR),
            Unit.get(BaseUnit.BAR),
            Unit.get(BaseUnit.LITERPERHOUR)
    ));

    @Test
    public void noMultiplierTest() {
        Unit watt = Unit.get("W");
        Pair<ReadingTypeUnit, MetricMultiplier> scaledCIMUnitFor = ReadingTypeUnitMapping.getScaledCIMUnitFor(watt);

        assertThat(scaledCIMUnitFor.getLast().getMultiplier()).isEqualTo(0);
    }

    @Test
    public void forcedMultiplierFromBaseUnitToCimUnitTest() {
        Unit kg = Unit.get("kg");
        Pair<ReadingTypeUnit, MetricMultiplier> scaledCIMUnitFor = ReadingTypeUnitMapping.getScaledCIMUnitFor(kg);

        assertThat(scaledCIMUnitFor.getLast().getMultiplier()).isEqualTo(3);
    }

    /**
     * Tests the set of unmappable Units so they result in a not_found-unit
     */
    @Test
    public void unMappableCimUnitsTest() {
        for (Unit unmappableUnit : unmappableUnits) {
            Pair<ReadingTypeUnit, MetricMultiplier> scaledCIMUnitFor = ReadingTypeUnitMapping.getScaledCIMUnitFor(unmappableUnit);
            assertThat(scaledCIMUnitFor).describedAs("I don't think we should have a CIM mapping for the MDC unit '" + unmappableUnit + "'." +
                    "If you think we are wrong, then correct the unmappableUnits Set.")
                    .isEqualTo(ReadingTypeUnitMapping.NO_CIM_UNIT_FOUND);
        }
    }

    /**
     * This should represent a set of Unit which have a mapping in the ReadingTypeMapping.
     *
     * @return a Set of Units which contains a mapping in the ReadingTypeMapping
     */
    private List<Unit> getAllMappableMDCUnits() {
        List<Unit> uniqueSets = new ArrayList<>();
        Iterator<BaseUnit> iterator = BaseUnit.iterator();
        while (iterator.hasNext()) {
            BaseUnit baseUnit = iterator.next();
            boolean exists = false;
            for (Unit unmappableUnit : unmappableUnits) {
                if (unmappableUnit.getBaseUnit().equals(baseUnit)) {
                    exists = true;
                }
            }
            if (!exists) {
                uniqueSets.add(Unit.get(baseUnit.getDlmsCode()));
            }
        }
        return uniqueSets;
    }

    /**
     * Test the mappings on abbreviation match. CIM and our MDC should have the same abbreviation for the units ...
     * This way all mappings should be checked for correctness.
     */
    @Test
    public void knownMappingsUnitAbbreviationTest() {
        for (final Unit mdcUnit : getAllMappableMDCUnits()) {
            Pair<ReadingTypeUnit, MetricMultiplier> scaledCIMUnit = ReadingTypeUnitMapping.getScaledCIMUnitFor(mdcUnit);
            assertThat(scaledCIMUnit.getFirst().getUnit())
                    .describedAs("Symbols should be exactly the same.")
                    .is(new Condition<Object>() {
                        @Override
                        public boolean matches(Object o) {
                            com.elster.jupiter.util.units.Unit unit = (com.elster.jupiter.util.units.Unit) o;
                            BaseUnit baseUnit = mdcUnit.getBaseUnit();
                            String symbol = unit.getSymbol();
                            String asciiSymbol = unit.getAsciiSymbol();
                            if (baseUnit.getDlmsCode() == BaseUnit.NORMALCUBICMETER) {
                                return asciiSymbol.equals("m3(compensated)");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.NORMALCUBICMETERPERHOUR) {
                                return asciiSymbol.equals("m3(compensated)/h");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.LITER) { // MDC litres is equal to 'l', CIM litres is equal to 'L'
                                return asciiSymbol.equalsIgnoreCase(baseUnit.toString());
                            } else if (baseUnit.getDlmsCode() == BaseUnit.KILOGRAM) { // kilogram should actually be gram ...
                                return asciiSymbol.equals("g");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.VOLTAMPEREREACTIVE) {   // various notations are "accepted" for 'var'
                                return asciiSymbol.equalsIgnoreCase(baseUnit.toString());
                            } else if (baseUnit.getDlmsCode() == BaseUnit.VOLTAMPEREREACTIVEHOUR) { // various notations are "accepted" for 'varh'
                                return asciiSymbol.equalsIgnoreCase(baseUnit.toString());
                            } else if (baseUnit.getDlmsCode() == BaseUnit.OHM) {
                                return asciiSymbol.equalsIgnoreCase(baseUnit.toString());
                            } else if (baseUnit.getDlmsCode() == BaseUnit.OHMSQUAREMETERPERMETER) {
                                return asciiSymbol.equals("ohmm");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.SIEMENS) {
                                return asciiSymbol.equals("S");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.NOTAVAILABLE) {
                                return asciiSymbol.isEmpty();
                            } else if (baseUnit.getDlmsCode() == BaseUnit.MOLPERCENT) {
                                return asciiSymbol.equals("mol");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.CUBICFEET) {
                                return asciiSymbol.equals("ft3");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.CUBICFEETPERHOUR) {
                                return asciiSymbol.equals("ft3/h");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.POUNDPERSQUAREINCH) {
                                return asciiSymbol.equals("ps/A");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.PERCENT) {
                                return asciiSymbol.isEmpty();
                            } else if (baseUnit.getDlmsCode() == BaseUnit.PARTSPERMILLION) {
                                return asciiSymbol.isEmpty();
                            } else if (baseUnit.getDlmsCode() == BaseUnit.US_GALLON) {
                                return asciiSymbol.equals("USGal");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.IMP_GALLON) {
                                return asciiSymbol.equals("ImperialGal");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.US_GALLONPERHOUR) {
                                return asciiSymbol.equals("USGal/h");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.DECIBELPOWERRATIO) {
                                return asciiSymbol.equals("Bm");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.IMP_GALLONPERHOUR) {
                                return asciiSymbol.equals("ImperialGal/h");
                            } else if (baseUnit.getDlmsCode() == BaseUnit.POUND || baseUnit.getDlmsCode() == BaseUnit.USD || baseUnit.getDlmsCode() == BaseUnit.EURO) {
                                return asciiSymbol.isEmpty();
                            } else {
                                if (symbol.equals(baseUnit.toString())) {
                                    return true;
                                } else {
                                    if (asciiSymbol.equals(baseUnit.toString())) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    });
        }
    }

}