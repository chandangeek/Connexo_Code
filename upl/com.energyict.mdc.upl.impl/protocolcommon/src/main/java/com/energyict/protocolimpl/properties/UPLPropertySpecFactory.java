package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;

import com.google.common.collect.Range;

import java.util.function.Supplier;
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

    // Hide utility class constructor
    private UPLPropertySpecFactory() {
    }
}