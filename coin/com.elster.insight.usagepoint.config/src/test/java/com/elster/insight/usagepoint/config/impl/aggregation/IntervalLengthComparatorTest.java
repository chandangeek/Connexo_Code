package com.elster.insight.usagepoint.config.impl.aggregation;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the {@link IntervalLengthComparator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (15:17)
 */
public class IntervalLengthComparatorTest {

    @Test
    public void eachCompareToNotSupported() {
        IntervalLengthComparator comparator = new IntervalLengthComparator();

        List<IntervalLength> notSmallerThanNotSupported = allButNotSupported()
                // Business method
                .filter(each -> comparator.compare(each, IntervalLength.NOT_SUPPORTED) != -1)
                .collect(Collectors.toList());

        // Asserts
        if (!notSmallerThanNotSupported.isEmpty()) {
            fail(notSmallerThanNotSupported.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " are not smaller then " + IntervalLength.NOT_SUPPORTED.name());
        }
    }

    @Test
    public void notSupportedCompareToEach() {
        IntervalLengthComparator comparator = new IntervalLengthComparator();

        List<IntervalLength> notSmallerThanNotSupported = allButNotSupported()
                // Business method
                .filter(each -> comparator.compare(IntervalLength.NOT_SUPPORTED, each) != 1)
                .collect(Collectors.toList());

        // Asserts
        if (!notSmallerThanNotSupported.isEmpty()) {
            fail(notSmallerThanNotSupported.stream().map(IntervalLength::name).collect(Collectors.joining(", ")) + " are not smaller then " + IntervalLength.NOT_SUPPORTED.name());
        }
    }

    private Stream<IntervalLength> allButNotSupported() {
        return EnumSet.complementOf(EnumSet.of(IntervalLength.NOT_SUPPORTED)).stream();
    }

}