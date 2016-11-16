package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.google.common.collect.Range;

import java.math.BigDecimal;

/**
 * Provides factory services for {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * as defined by the universal protocol layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (16:34)
 */
public final class UPLPropertySpecFactory {

    public static PropertySpec integer(String name, boolean required) {
        return new IntegerPropertySpec(name, required);
    }

    public static PropertySpec integer(String name, boolean required, Range<Integer> validRange) {
        return new IntegerPropertySpec(name, required, validRange);
    }

    public static PropertySpec integer(String name, boolean required, Integer... validValues) {
        return new IntegerPropertySpec(name, required, validValues);
    }

    public static PropertySpec longValue(String name, boolean required) {
        return new LongPropertySpec(name, required);
    }

    public static PropertySpec longValue(String name, boolean required, Range<Long> validRange) {
        return new LongPropertySpec(name, required, validRange);
    }

    public static PropertySpec longValue(String name, boolean required, Long... validValues) {
        return new LongPropertySpec(name, required, validValues);
    }

    public static PropertySpec<BigDecimal> bigDecimal(String name, boolean required) {
        return new BigDecimalPropertySpec(name, required);
    }

    public static PropertySpec<BigDecimal> bigDecimal(String name, boolean required, BigDecimal defaultValue, BigDecimal... possibleValues) {
        BigDecimalPropertySpec bigDecimalPropertySpec = new BigDecimalPropertySpec(name, required, possibleValues);
        bigDecimalPropertySpec.setDefaultValue(defaultValue);
        return bigDecimalPropertySpec;
    }

    public static PropertySpec<String> string(String name, boolean required) {
        return new StringPropertySpec(name, required);
    }

    public static PropertySpec<String> string(String name, boolean required, String... possibleValues){
        return new StringPropertySpec(name, required, possibleValues);
    }

    public static PropertySpec<String> string(String name, boolean required, String defaultValue, String... possibleValues){
        StringPropertySpec stringPropertySpec = new StringPropertySpec(name, required, possibleValues);
        stringPropertySpec.setDefaultValue(defaultValue);
        return stringPropertySpec;
    }

    public static PropertySpec stringOfMaxLength(String name, boolean required, int maxLength) {
        StringPropertySpec spec = new StringPropertySpec(name, required);
        spec.setMaximumLength(maxLength);
        return spec;
    }

    public static PropertySpec stringOfExactLength(String name, boolean required, int length) {
        StringPropertySpec spec = new StringPropertySpec(name, required);
        spec.setExactLength(length);
        return spec;
    }

    public static PropertySpec hexString(String name, boolean required) {
        return new HexStringPropertySpec(name, required);
    }

    public static PropertySpec character(String name, boolean required) {
        return new CharPropertySpec(name, required);
    }

    public static PropertySpec character(String name, boolean required, String possibleValues) {
        return new CharPropertySpec(name, required, possibleValues);
    }

    // Hide utility class constructor
    private UPLPropertySpecFactory() {}
}