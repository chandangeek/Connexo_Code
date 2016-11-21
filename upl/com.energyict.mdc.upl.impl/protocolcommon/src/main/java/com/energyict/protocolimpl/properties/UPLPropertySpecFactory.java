package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.BoundType.OPEN;

/**
 * Provides convenience methods for the {@link com.energyict.mdc.upl.properties.PropertySpecService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (16:34)
 */
public final class UPLPropertySpecFactory {

    public static PropertySpec<Integer> integer(String name, boolean required) {
        return integerSpecBuilder(name, required).finish();
    }

    public static PropertySpec<Integer> integer(String name, boolean required, Range<Integer> validRange) {
        PropertySpecBuilder<Integer> builder = integerSpecBuilder(name, required);
        builder.addValues(toStream(validRange).collect(Collectors.toList()));
        return builder.finish();
    }

    public static PropertySpec<Integer> integer(String name, boolean required, Integer... validValues) {
        PropertySpecBuilder<Integer> builder = integerSpecBuilder(name, required);
        builder.addValues(Arrays.asList(validValues));
        return builder.finish();
    }

    private static PropertySpecBuilder<Integer> integerSpecBuilder(String name, boolean required) {
        PropertySpecBuilder<Integer> builder = Services.propertySpecService().integerSpec().named(name, name).describedAs("Description for " + name);
        if (required) {
            builder.markRequired();
        }
        return builder;
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

    private static Stream<Integer> toStream(Range<Integer> range) {
        Stream.Builder<Integer> builder = Stream.builder();
        Integer current = firstMemberCandidate(range);
        while (range.contains(current)) {
            builder.add(current);
            current = current + 1;
        }
        return builder.build();
    }

    private static Integer firstMemberCandidate(Range<Integer> range) {
        if (range.lowerBoundType().equals(OPEN)) {
            return range.lowerEndpoint() + 1;
        } else {
            return range.lowerEndpoint();
        }
    }

    // Hide utility class constructor
    private UPLPropertySpecFactory() {}
}