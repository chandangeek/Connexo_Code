package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
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

    public static void addIntegerValues(PropertySpecBuilder<Integer> builder, Range<Integer> range) {
        builder.addValues(toIntegerStream(range).collect(Collectors.toList()));
    }

    public static void addLongValues(PropertySpecBuilder<Long> builder, Range<Long> range) {
        builder.addValues(toLongStream(range).collect(Collectors.toList()));
    }

    public static <T> PropertySpecBuilder<T> specBuilder(String name, boolean required, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        PropertySpecBuilder<T> builder = optionsSupplier.get().named(name, name).describedAs("Description for " + name);
        if (required) {
            builder.markRequired();
        }
        return builder;
    }

    public static <T> PropertySpecBuilder<T> specBuilder(String name, boolean required, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        PropertySpecBuilder<T> builder = optionsSupplier.get().named(name, translationKey).describedAs(new DescriptionTranslationKey(translationKey));
        if (required) {
            builder.markRequired();
        }
        return builder;
    }

    public static PropertySpec integer(String name, boolean required) {
        return integerSpecBuilder(name, required).finish();
    }

    public static PropertySpec integer(String name, boolean required, Range<Integer> validRange) {
        PropertySpecBuilder<Integer> builder = integerSpecBuilder(name, required);
        builder.addValues(toIntegerStream(validRange).collect(Collectors.toList()));
        return builder.finish();
    }

    public static PropertySpec integer(String name, boolean required, Integer... validValues) {
        PropertySpecBuilder<Integer> builder = integerSpecBuilder(name, required);
        builder.addValues(Arrays.asList(validValues));
        return builder.finish();
    }

    private static PropertySpecBuilder<Integer> integerSpecBuilder(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().integerSpec());
    }

    public static PropertySpec longValue(String name, boolean required) {
        return longSpecBuilder(name, required).finish();
    }

    public static PropertySpec longValue(String name, boolean required, Range<Long> validRange) {
        PropertySpecBuilder<Long> builder = longSpecBuilder(name, required);
        builder.addValues(toLongStream(validRange).collect(Collectors.toList()));
        return builder.finish();
    }

    public static PropertySpec longValue(String name, boolean required, Long... validValues) {
        PropertySpecBuilder<Long> builder = longSpecBuilder(name, required);
        builder.addValues(Arrays.asList(validValues));
        return builder.finish();
    }

    private static PropertySpecBuilder<Long> longSpecBuilder(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().longSpec());
    }

    public static PropertySpec booleanValue(String name, boolean required) {
        return booleanSpecBuilder(name, required).finish();
    }

    public static PropertySpec booleanValue(String name, boolean required, boolean defaultValue) {
        return booleanSpecBuilder(name, required).setDefaultValue(defaultValue).finish();
    }

    private static PropertySpecBuilder<Boolean> booleanSpecBuilder(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().booleanSpec());
    }

    public static PropertySpec bigDecimal(String name, boolean required) {
        return bigDecimalSpecBuilder(name, required).finish();
    }

    public static PropertySpec positiveBigDecimal(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().positiveBigDecimalSpec()).finish();
    }

    public static PropertySpec bigDecimal(String name, boolean required, BigDecimal defaultValue, BigDecimal... possibleValues) {
        PropertySpecBuilder<BigDecimal> builder = bigDecimalSpecBuilder(name, required);
        builder.addValues(Arrays.asList(possibleValues));
        if (possibleValues.length > 0) {
            builder.markExhaustive();
        }
        builder.setDefaultValue(defaultValue);
        return builder.finish();
    }

    private static PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().bigDecimalSpec());
    }

    public static PropertySpec string(String name, boolean required) {
        return new StringPropertySpec(name, required);
    }

    public static PropertySpec string(String name, boolean required, String... possibleValues) {
        return string(name, required, Optional.empty(), possibleValues);
    }

    public static PropertySpec stringWithDefault(String name, boolean required, String defaultValue, String... possibleValues) {
        return string(name, required, Optional.of(defaultValue), possibleValues);
    }

    private static PropertySpec string(String name, boolean required, Optional<String> defaultValue, String... possibleValues) {
        PropertySpecBuilder<String> builder = stringSpecBuilder(name, required);
        builder.addValues(Arrays.asList(possibleValues));
        if (possibleValues.length > 0) {
            builder.markExhaustive();
        }
        defaultValue.ifPresent(builder::setDefaultValue);
        return builder.finish();
    }

    private static PropertySpecBuilder<String> stringSpecBuilder(String name, boolean required) {
        return specBuilder(name, required, () -> Services.propertySpecService().stringSpec());
    }

    public static PropertySpec stringOfMaxLength(String name, boolean required, int maxLength) {
        return specBuilder(name, required, () -> Services.propertySpecService().stringSpecOfMaximumLength(maxLength)).finish();
    }

    public static PropertySpec stringOfExactLength(String name, boolean required, int length) {
        return specBuilder(name, required, () -> Services.propertySpecService().stringSpecOfExactLength(length)).finish();
    }

    public static PropertySpec hexString(String name, boolean required) {
        return new HexStringPropertySpec(name, required);
    }

    public static PropertySpec hexString(String name, boolean required, HexString hexString) {
        PropertySpecBuilder<HexString> hexStringPropertySpecBuilder = Services.propertySpecService().hexStringSpec().named(name, name).describedAs(name);
        if(required){
            hexStringPropertySpecBuilder.markRequired();
        }
        hexStringPropertySpecBuilder.setDefaultValue(hexString);
        return hexStringPropertySpecBuilder.finish();
    }

    public static PropertySpec hexStringSpecOfExactLength(String name, boolean required, int length) {
        PropertySpecBuilder<HexString> hexStringPropertySpecBuilder = Services.propertySpecService().hexStringSpecOfExactLength(length).named(name, name).describedAs(name);
        if(required){
            hexStringPropertySpecBuilder.markRequired();
        }
        return hexStringPropertySpecBuilder.finish();
    }

    public static PropertySpec character(String name, boolean required) {
        return new CharPropertySpec(name, required);
    }

    public static PropertySpec character(String name, boolean required, String possibleValues) {
        return new CharPropertySpec(name, required, possibleValues);
    }

    private static Stream<Integer> toIntegerStream(Range<Integer> range) {
        Stream.Builder<Integer> builder = Stream.builder();
        Integer current = firstIntegerMemberCandidate(range);
        while (range.contains(current)) {
            builder.add(current);
            current = current + 1;
        }
        return builder.build();
    }

    private static Integer firstIntegerMemberCandidate(Range<Integer> range) {
        if (range.lowerBoundType().equals(OPEN)) {
            return range.lowerEndpoint() + 1;
        } else {
            return range.lowerEndpoint();
        }
    }

    private static Stream<Long> toLongStream(Range<Long> range) {
        Stream.Builder<Long> builder = Stream.builder();
        Long current = firstLongMemberCandidate(range);
        while (range.contains(current)) {
            builder.add(current);
            current = current + 1;
        }
        return builder.build();
    }

    private static Long firstLongMemberCandidate(Range<Long> range) {
        if (range.lowerBoundType().equals(OPEN)) {
            return range.lowerEndpoint() + 1;
        } else {
            return range.lowerEndpoint();
        }
    }

    public static PropertySpec duration(String name, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = specBuilder(name, required, () -> Services.propertySpecService().durationSpec());
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        if (required) {
            durationPropertySpecBuilder.markRequired();
        }

        return durationPropertySpecBuilder.finish();
    }

    public static PropertySpec temporalAmount(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().temporalAmountSpec()).finish();
    }

    public static PropertySpec dateTime(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().dateTimeSpec()).finish();
    }

    public static PropertySpec date(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().dateSpec()).finish();
    }

    public static PropertySpec time(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().timeSpec()).finish();
    }

    public static PropertySpec timeZone(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().timeZoneSpec()).finish();
    }

    public static PropertySpec password(String name) {
        return specBuilder(name, false, () -> Services.propertySpecService().passwordSpec()).finish();
    }

    public static PropertySpec reference(String name, Class apiClass) {
        return specBuilder(name, false, () -> Services.propertySpecService().referenceSpec(apiClass.getName())).finish();
    }

    public static PropertySpec encryptedString(String name, boolean required) {
        PropertySpecBuilder<String> encryptedStringPropertySpecBuilder = Services.propertySpecService().encryptedStringSpec().named(name, name).describedAs(name);
        if(required){
            encryptedStringPropertySpecBuilder.markRequired();
        }
        return encryptedStringPropertySpecBuilder.finish();
    }

    // Hide utility class constructor
    private UPLPropertySpecFactory() {
    }
}