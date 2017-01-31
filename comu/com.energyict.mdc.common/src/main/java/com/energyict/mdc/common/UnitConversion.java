/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class UnitConversion {

    private final BigDecimal multiplier;
    private final BigDecimal constant;

    private static final Map<String, UnitConversion> conversionRules = new HashMap<String, UnitConversion>();

    static {
        conversionRules.put(BaseUnit.DEGREE_CELSIUS + "-" + BaseUnit.KELVIN,
                new UnitConversion(new BigDecimal("1"), new BigDecimal("273.15")));
        conversionRules.put(BaseUnit.CUBICMETER + "-" + BaseUnit.LITER,
                new UnitConversion(new BigDecimal("1000"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.IMP_GALLON + "-" + BaseUnit.LITER,
                new UnitConversion(new BigDecimal("4.54609"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.US_GALLON + "-" + BaseUnit.LITER,
                new UnitConversion(new BigDecimal("3.7854"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.CUBICMETER + "-" + BaseUnit.CUBICFEET,
                new UnitConversion(new BigDecimal("35.3146667"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.CUBICMETER + "-" + BaseUnit.CUBICINCH,
                new UnitConversion(new BigDecimal("61023.7441"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.CUBICYARD + "-" + BaseUnit.CUBICMETER,
                new UnitConversion(new BigDecimal("0.764554857984"), new BigDecimal("0")));
        conversionRules.put(BaseUnit.ACREFEET + "-" + BaseUnit.CUBICMETER,
                new UnitConversion(new BigDecimal("1233.48183754752"), new BigDecimal("0")));
    }

    private UnitConversion(BigDecimal multiplier, BigDecimal constant) {
        this.multiplier = multiplier;
        this.constant = constant;
    }

    public static Quantity convertQuantity(Quantity quantity, Unit destinationUnit) {
        // Check for conversion to same Unit
        if (quantity.getBaseUnit().equals(destinationUnit.getBaseUnit())) {
            return rescaleQuantity(quantity, destinationUnit);
        } else {
            // 1. Scale the SourceQuantity to it's BaseUnit (scale 0)
            quantity = new Quantity(quantity.getAmount().movePointRight(quantity.getUnit().getScale()),
                    Unit.get(quantity.getUnit().getDlmsCode(),
                            0));

            // 2. Do the conversion
            BigDecimal convertedAmount = convertAmount(quantity, destinationUnit);
            Quantity convertedQuantity = new Quantity(convertedAmount, destinationUnit.getBaseUnit().getDlmsCode(), 0);

            // 3. Scale the converted Quantity to the required scale
            convertedQuantity = new Quantity(convertedQuantity.getAmount().movePointLeft(destinationUnit.getScale()), destinationUnit);
            return convertedQuantity;
        }
    }

    private static Quantity rescaleQuantity(final Quantity quantity, final Unit destinationUnit) {
        final int scaleDifference = quantity.getUnit().getScale() - destinationUnit.getScale();
        if (scaleDifference == 0) {
            return quantity;
        } else {
            return new Quantity(quantity.getAmount().movePointRight(scaleDifference), destinationUnit);
        }
    }

    private static UnitConversion findUnitConversion(BaseUnit sourceUnit, BaseUnit destinationUnit) {
        UnitConversion conversionObj;
        conversionObj = conversionRules.get(sourceUnit.getDlmsCode() + "-" + destinationUnit.getDlmsCode());
        return conversionObj;
    }

    private static BigDecimal convertAmount(Quantity quantity, Unit destinationUnit) {
        boolean inversedConversion = false;
        UnitConversion unitConversion = findUnitConversion(quantity.getBaseUnit(), destinationUnit.getBaseUnit());
        if (unitConversion == null) {
            inversedConversion = true;
            unitConversion = findUnitConversion(destinationUnit.getBaseUnit(), quantity.getBaseUnit());
        }
        if (unitConversion == null) {
            throw new ArithmeticException("Unit mismatch: " + quantity.getUnit().toString() + " cannot be converted to " + destinationUnit.toString() + ".");
        }

        int precision = quantity.getAmount().precision() + unitConversion.multiplier.precision();
        // If invertConversionRule is true, we need to invert the operator
        BigDecimal result = inversedConversion
                ? quantity.getAmount().divide(unitConversion.multiplier, precision, BigDecimal.ROUND_HALF_UP).subtract(unitConversion.constant)
                : quantity.getAmount().multiply(unitConversion.multiplier).add(unitConversion.constant);

        return result.stripTrailingZeros();
    }

}
