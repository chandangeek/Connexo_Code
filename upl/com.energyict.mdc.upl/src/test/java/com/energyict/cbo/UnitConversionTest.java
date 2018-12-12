/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cbo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitConversionTest {

    @Test
    public void convertDegreesCelsiusToKelvin() throws Exception {
        // Forward conversion
        Quantity sourceQuantity = new Quantity("12.3", BaseUnit.DEGREE_CELSIUS, 3);
        Unit destinationUnit = Unit.get(BaseUnit.KELVIN, 3);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("12.57315", BaseUnit.KELVIN, 3), destinationQuantity);
    }

    @Test
    public void convertKelvinToDegreesCelsius() throws Exception {
        // backward conversion
        Quantity sourceQuantity = new Quantity("274.16", BaseUnit.KELVIN, 0);
        Unit destinationUnit = Unit.get(BaseUnit.DEGREE_CELSIUS, 0);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("1.01", BaseUnit.DEGREE_CELSIUS, 0), destinationQuantity);
    }

    @Test
    public void convertLitersToCubicMeter() throws Exception {
        Quantity sourceQuantity = new Quantity("1.23456", BaseUnit.LITER, 6);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICMETER, 3);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("1.23456", BaseUnit.CUBICMETER, 3), destinationQuantity);
    }

    @Test(expected = ArithmeticException.class)
    public void convertLitersToAmpere() throws Exception {
        // Conversion to illegal unit (no conversion rule exists)
        Quantity sourceQuantity = new Quantity("1.23456", BaseUnit.LITER, 6);
        Unit destinationUnit = Unit.get(BaseUnit.AMPERE);
        Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
    }

    @Test
    public void convertLitersToLitersWithDifferentScale() throws Exception {
        // conversion to same unit - just conversion of scale
        Quantity sourceQuantity = new Quantity("1.23456", BaseUnit.LITER, 6);
        Unit destinationUnit = Unit.get(BaseUnit.LITER, 3);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("1234.56", BaseUnit.LITER, 3), destinationQuantity);
    }

    @Test
    public void convertCubicMeterToAcreFeet() throws Exception {
        Quantity sourceQuantity = new Quantity("205.123", BaseUnit.CUBICMETER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.ACREFEET, 0);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("0.16629592244976822998", BaseUnit.ACREFEET, 0), destinationQuantity);
    }

    @Test
    public void convertLiterToCubicMeterWithHighPrecision() {
        Quantity sourceQuantity = new Quantity("0.01", BaseUnit.LITER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICMETER, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("0.00001", BaseUnit.CUBICMETER, 0), result);
    }

    @Test
    public void convertCubicMeterToLiter() {
        Quantity sourceQuantity = new Quantity("2.1234567890", BaseUnit.CUBICMETER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.LITER, 0);
        Quantity destinationQuantity = Quantity.UnitConversion.convertQuantity(sourceQuantity, destinationUnit);
        assertEquals(new Quantity("2123.456789", BaseUnit.LITER, 0), destinationQuantity);
    }

    @Test
    public void convertGallonsToLiters() {
        Quantity quantity = new Quantity("100", BaseUnit.IMP_GALLON, 0);
        Unit destinationUnit = Unit.get(BaseUnit.LITER, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("454.609", BaseUnit.LITER, 0), result);
    }

    @Test
    public void convertLitersToGallons() {
        Quantity quantity = new Quantity("100", BaseUnit.LITER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.IMP_GALLON, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("21.99692483", BaseUnit.IMP_GALLON, 0), result);
    }

    @Test
    public void convertCubicInchToCubicMeter() {
        Quantity quantity = new Quantity("10000", BaseUnit.CUBICINCH, 0);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICMETER, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("0.16387063998585", BaseUnit.CUBICMETER, 0), result);
    }

    @Test
    public void convertCubicMeterToCubicInch() {
        Quantity quantity = new Quantity("1", BaseUnit.CUBICMETER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICINCH, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("61023.7441", BaseUnit.CUBICINCH, 0), result);
    }

    @Test
    public void convertCubicYardToCubicMeter() {
        Quantity quantity = new Quantity("1", BaseUnit.CUBICYARD, 0);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICMETER, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("0.764554857984", BaseUnit.CUBICMETER, 0), result);
    }

    @Test
    public void convertCubicMeterToCubicYard() {
        Quantity quantity = new Quantity("1", BaseUnit.CUBICMETER, 0);
        Unit destinationUnit = Unit.get(BaseUnit.CUBICYARD, 0);
        Quantity result = Quantity.UnitConversion.convertQuantity(quantity, destinationUnit);
        assertEquals(new Quantity("1.3079506193144", BaseUnit.CUBICYARD, 0), result);
    }
}