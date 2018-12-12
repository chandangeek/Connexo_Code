/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitTest {

    private static final BigDecimal VALUE = new BigDecimal(14.5);

    @Test
    public void testAmount() {
        Quantity amount = Unit.AMPERE.amount(VALUE);

        assertThat(amount.getUnit()).isEqualTo(Unit.AMPERE);
    }

    @Test
    public void testUnitForSymbol() {
        for (Unit unit : Unit.values()) {
            assertThat(Unit.unitForSymbol(unit.getSymbol())).isEqualTo(unit);
        }
    }

    @Test
    public void testGetByAsciiSymbol() {
    	Set<String> asciiSymbols = new HashSet<>();
        for (Unit unit : Unit.values()) {
        	asciiSymbols.add(unit.getAsciiSymbol());
        	assertThat(Unit.get(unit.getAsciiSymbol())).isEqualTo(unit);
        }
        assertThat(asciiSymbols).hasSize(Unit.values().length);
    }
    
    @Test
    public void testName() {
    	Set<String> set = new HashSet<>();
    	for (Unit unit : Unit.values()) {
    		set.add(unit.getName());
    	}
    	assertThat(set).hasSize(Unit.values().length);
    }

    @Test
    public void testSymbol() {
    	Set<String> set = new HashSet<>();
    	for (Unit unit : Unit.values()) {
    		set.add(unit.getSymbol());
    	}
    	assertThat(set).hasSize(Unit.values().length);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnitForSymbolNotExists() {
        Unit.unitForSymbol("NotAUnitSymbol");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByAsciiSymbolNotExists() {
        Unit.get("NotAUnitSymbol");
    }

    @Test
    public void testDimensionLessTrue() {
        assertThat(Unit.BOOLEAN.isDimensionLess()).isTrue();
    }

    @Test
    public void testDimensionLessFalse() {
        assertThat(Unit.KILOGRAM.isDimensionLess()).isFalse();
    }

    @Test
    public void testToString() {
        for (Unit unit : Unit.values()) {
            assertThat(unit.toString()).isEqualTo(unit.getSymbol());
        }
    }

    @Test
    public void testIsCoherentSiUnitTrue() {
        assertThat(Unit.KILOGRAM.isCoherentSiUnit()).isTrue();
    }

    @Test
    public void testIsCoherentSiUnitFalse() {
        assertThat(Unit.VOLT_SQUARED_HOUR.isCoherentSiUnit()).isFalse();
    }

    @Test
    public void testSiValue() {
        assertThat(Unit.FOOT.siValue(BigDecimal.valueOf(20)).getValue()).isEqualTo(new BigDecimal("6.096"));
    }

    @Test
    public void testGetSiUnit() {
        assertThat(Unit.getSIUnit(Dimension.SPEED)).isEqualTo(Unit.METER_PER_SECOND);
    }

    @Test
    public void testGetSiFactors() {
        assertThat(Unit.DEGREES_CELSIUS.getSiDelta()).isEqualTo(BigDecimal.valueOf(27315, 2));
        assertThat(Unit.FOOT.getSiMultiplier()).isEqualTo(BigDecimal.valueOf(3048, 4));
        assertThat(Unit.FOOT.getSiDivisor()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void scaler() {
    	assertThat(BigDecimal.valueOf(1602176L,25).doubleValue()).isEqualTo(1.602176e-19);
    }
}
