/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomPropertySetServiceImplRangeCoverage {


    private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static Instant instant(String value) {
        return LocalDate.from(DATE_FORMAT.parse(value)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }

    static Instant I01 = instant("20170201000000");
    static Instant I02 = instant("20170202000000");
    static Instant I03 = instant("20170203000000");
    static Instant I04 = instant("20170204000000");
    static Instant I05 = instant("20170205000000");
    static Instant I06 = instant("20170206000000");
    static Instant I07 = instant("20170207000000");
    static Instant I08 = instant("20170208000000");
    static Instant I09 = instant("20170209000000");
    static Instant I10 = instant("20170210000000");

    private boolean fullyCovered;
    private Range<Instant> targetRange;
    private List<Range<Instant>> ranges;

    CustomPropertySetServiceImplRangeCoverage(boolean fullyCovered, Range<Instant> targetRange, List<Range<Instant>> ranges) {
        this.fullyCovered = fullyCovered;
        this.targetRange = targetRange;
        this.ranges = ranges;
    }

    public boolean isFullyCovered() {
        return fullyCovered;
    }

    public Range<Instant> getTargetRange() {
        return targetRange;
    }

    public List<Range<Instant>> getRanges() {
        return ranges;
    }

    static List<CustomPropertySetServiceImplRangeCoverage> configurations() {
        return Arrays.asList(
                supposedCovered()
                        .withTargetRange(Range.atLeast(I02))
                        .addRange(Range.closed(I01, I03))
                        .addRange(Range.atLeast(I03))
                        .build(),
                supposedCovered()
                        .withTargetRange(Range.atLeast(I02))
                        .addRange(Range.atLeast(I02))
                        .build(),
                supposedCovered()
                        .withTargetRange(Range.closed(I05,I07))
                        .addRange(Range.closed(I01,I02))
                        .addRange(Range.closed(I02,I03))
                        .addRange(Range.closed(I04,I05))
                        .addRange(Range.closed(I05,I06))
                        .addRange(Range.closed(I06,I07))
                        .addRange(Range.atLeast(I07))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.closedOpen(I01, I04))
                        .addRange(Range.closed(I05, I06))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.closedOpen(I02, I04))
                        .addRange(Range.closed(I01, I02))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.closedOpen(I02, I04))
                        .addRange(Range.closed(I01, I03))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.closedOpen(I01, I04))
                        .addRange(Range.closed(I01, I02))
                        .addRange(Range.closed(I03, I04))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.atLeast(I05))
                        .addRange(Range.closed(I01, I02))
                        .addRange(Range.atLeast(I06))
                        .build(),
                supposedNotCovered()
                        .withTargetRange(Range.atLeast(I05))
                        .build()
        );
    }

    private static Builder supposedCovered() {
        return new Builder(true);
    }

    private static Builder supposedNotCovered() {
        return new Builder(false);
    }

    static class Builder {
        private boolean fullyCovered;
        private Range<Instant> targetRange;
        private List<Range<Instant>> ranges;

        Builder(boolean fullyCovered) {
            this.fullyCovered = fullyCovered;
            ranges = new ArrayList<>();
        }

        Builder withTargetRange(Range<Instant> targetRange) {
            this.targetRange = targetRange;
            return this;
        }

        Builder addRange(Range<Instant> range) {
            ranges.add(range);
            return this;
        }

        CustomPropertySetServiceImplRangeCoverage build() {
            return new CustomPropertySetServiceImplRangeCoverage(fullyCovered, targetRange, ranges);
        }
    }
}
