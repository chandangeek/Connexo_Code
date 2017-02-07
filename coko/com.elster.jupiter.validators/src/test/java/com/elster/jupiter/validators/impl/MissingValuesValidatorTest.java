/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.MockitoRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class MissingValuesValidatorTest {

    @RunWith(Parameterized.class)
    public static class MissingValuesValidatorParameterizedTest {

        @Rule
        public TestRule speakingMaltese = Using.localeOfMalta();

        @Rule
        public TestRule inMcMurdo = Using.timeZoneOfMcMurdo();

        @Rule
        public TestRule usingMocks = MockitoRule.initMocks(this);

        private Instant base;
        private Instant start;
        private Instant startPlus7;
        private Instant startPlus10;
        private Instant startPlus20;
        private Instant startPlus30;
        private Instant startPlus37;
        private Instant startPlus40;
        private Instant startPlus50;
        private Instant end;

        @Mock
        private Thesaurus thesaurus;
        @Mock
        private PropertySpecService propertySpecService;
        @Mock
        private Channel channel;
        @Mock
        private ChannelsContainer channelsContainer;
        @Mock
        private ReadingType readingType, bulkReadingType;
        @Mock
        private IntervalReadingRecord intervalReading;

        private MissingValuesValidator validator;

        public MissingValuesValidatorParameterizedTest(Instant base) {
            this.base = base;
        }

        @Parameterized.Parameters
        public static List<Object[]> params() {
            return Arrays.asList(
                    new Instant[]{LocalDateTime.of(1992, 1, 14, 16, 0).toInstant(ZoneOffset.UTC)}, // Winter time
                    new Object[]{LocalDateTime.of(1992, 8, 14, 16, 0).toInstant(ZoneOffset.UTC)},  // Summer time
                    new Object[]{LocalDateTime.of(2013, 4, 7, 2, 50).toInstant(ZoneOffset.UTC)},  // Winter -> Summer
                    new Object[]{LocalDateTime.of(2013, 9, 29, 1, 50).toInstant(ZoneOffset.UTC)}  // Summer -> Winter
            );
        }

        @Before
        public void setUp() {
            when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE10);
            when(channel.isRegular()).thenReturn(true);
            start = base;
            startPlus7 = base.plus(7, ChronoUnit.MINUTES);
            startPlus10 = base.plus(10, ChronoUnit.MINUTES);
            startPlus20 = base.plus(20, ChronoUnit.MINUTES);
            startPlus30 = base.plus(30, ChronoUnit.MINUTES);
            startPlus37 = base.plus(37, ChronoUnit.MINUTES);
            startPlus40 = base.plus(40, ChronoUnit.MINUTES);
            startPlus50 = base.plus(50, ChronoUnit.MINUTES);
            end = base.plus(60, ChronoUnit.MINUTES);

            when(channel.getChannelsContainer()).thenReturn(channelsContainer);
            when(channelsContainer.getStart()).thenReturn(start);
            when(intervalReading.getQuantity(readingType)).thenReturn(Quantity.create(BigDecimal.ONE, "Wh"));
            when(intervalReading.getValue()).thenReturn(BigDecimal.ONE);
            when(readingType.getBulkReadingType()).thenReturn(Optional.of(bulkReadingType));
            when(readingType.isCumulative()).thenReturn(false);
            doReturn(Arrays.asList(readingType, bulkReadingType)).when(channel).getReadingTypes();
            validator = new MissingValuesValidator(thesaurus, propertySpecService);
        }

        @After
        public void tearDown() {
        }

        @Test
        public void testValidateNoneMissing() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30, startPlus40, startPlus50, end));
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());

            validator.init(channel, readingType, Range.openClosed(start, end));

            for (Instant date : new Instant[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
                when(intervalReading.getTimeStamp()).thenReturn(date);
                assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
            }

            assertThat(validator.finish()).isEmpty();
        }

        @Test
        public void testValidateNoneMissingEvenIfFirstReadingEverMissesDelta() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30, startPlus40, startPlus50, end));
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());

            validator.init(channel, readingType, Range.closed(start, end));

            for (Instant date : new Instant[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
                when(intervalReading.getTimeStamp()).thenReturn(date);
                if (date.equals(start)) {
                    when(intervalReading.getQuantity(readingType)).thenReturn(null);
                } else {
                    when(intervalReading.getQuantity(readingType)).thenReturn(Quantity.create(BigDecimal.ONE, "Wh"));
                }
                assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
            }

            assertThat(validator.finish()).isEmpty();
        }

        @Test
        public void testValidateOneMissingYetThereIsAReadingForOtherReadingTypes() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30, startPlus40, startPlus50, end));
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());

            validator.init(channel, readingType, Range.openClosed(start, end));

            for (Instant date : new Instant[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
                when(intervalReading.getTimeStamp()).thenReturn(date);
                if (date.equals(startPlus30)) {
                    when(intervalReading.getQuantity(readingType)).thenReturn(null);
                } else {
                    when(intervalReading.getQuantity(readingType)).thenReturn(Quantity.create(BigDecimal.ONE, "Wh"));
                }
                assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
            }

            assertThat(validator.finish()).contains(MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
        }

        @Test
        public void testValidateSomeMissing() {
            when(channel.toList(any())).thenReturn(Arrays.asList(start, startPlus10, startPlus20, startPlus30, startPlus40, startPlus50, end));
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());

            validator.init(channel, readingType, Range.closed(start, end));

            for (Instant date : new Instant[]{startPlus10, startPlus20, startPlus50, startPlus40, end}) {
                when(intervalReading.getTimeStamp()).thenReturn(date);
                assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
            }

            assertThat(validator.finish()).contains(MapEntry.entry(start, ValidationResult.SUSPECT), MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
        }

        @Test
        public void testValidateAllMissing() {
            when(channel.toList(any())).thenReturn(Arrays.asList(start, startPlus10, startPlus20, startPlus30, startPlus40, startPlus50, end));
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());

            validator.init(channel, readingType, Range.closed(start, end));

            assertThat(validator.finish()).contains(
                    MapEntry.entry(start, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus30, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus40, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus50, ValidationResult.SUSPECT),
                    MapEntry.entry(end, ValidationResult.SUSPECT)
            );
        }

        @Test
        public void testValidateForIrregularEndTime() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30));

            validator.init(channel, readingType, Range.closed(startPlus10, startPlus37));

            assertThat(validator.finish()).contains(
                    MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
            );
        }

        @Test
        public void testValidateForIrregularStartTime() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30));

            validator.init(channel, readingType, Range.closed(startPlus7, startPlus30));

            assertThat(validator.finish()).contains(
                    MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
            );
        }

        @Test
        public void testValidateForIrregularStartAndEndTime() {
            when(channel.toList(any())).thenReturn(Arrays.asList(startPlus10, startPlus20, startPlus30));

            validator.init(channel, readingType, Range.closed(startPlus7, startPlus37));

            assertThat(validator.finish()).contains(
                    MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                    MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
            );
        }

        @Test
        public void testNoMissingsReportedOnIrregularChannels() {
            when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
            when(channel.isRegular()).thenReturn(false);
            when(channel.getBulkQuantityReadingType()).thenReturn(Optional.empty());
            when(channel.toList(any())).thenReturn(Collections.emptyList());

            validator.init(channel, readingType, Range.closed(start, end));

            assertThat(validator.finish()).isEmpty();
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class MissingValuesValidatorSingleTest {

        @Mock
        private Thesaurus thesaurus;
        @Mock
        private PropertySpecService propertySpecService;

        private MissingValuesValidator validator;

        @Before
        public void setUp() {
            validator = new MissingValuesValidator(thesaurus, propertySpecService);
        }

        @Test
        public void testGetSupportedApplications() {
            assertThat(validator.getSupportedQualityCodeSystems()).containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
        }
    }
}
