package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.MockitoRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.Range;
import org.assertj.core.data.MapEntry;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MissingValuesValidatorTest {

    @Rule
    public TestRule speakingMaltese = Using.localeOfMalta();

    @Rule
    public TestRule inMcMurdo = Using.timeZoneOfMcMurdo();

    @Rule
    public TestRule usingMocks = MockitoRule.initMocks(this);

    public Instant base;
    public Instant start;
    public Instant startPlus7;
    public Instant startPlus10;
    public Instant startPlus20;
    public Instant startPlus30;
    public Instant startPlus37;
    public Instant startPlus40;
    public Instant startPlus50;
    public Instant end;

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;
    @Mock
    private IntervalReadingRecord intervalReading;

    public MissingValuesValidatorTest(Instant base) {
        this.base = base;
    }

    @Parameterized.Parameters
    public static List<Object[]> params() {
        return Arrays.asList(
                new Object[]{new DateTime(1992, 1, 14, 16, 0)}, // Winter time
                new Object[]{new DateTime(1992, 8, 14, 16, 0)},  // Summer time
                new Object[]{new DateTime(2013, 4, 7, 2, 50)},  // Winter -> Summer
                new Object[]{new DateTime(2013, 9, 29, 1, 50)}  // Summer -> Winter
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
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidateNoneMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.openClosed(start, end));

        for (Instant date : new Instant[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
        }

        assertThat(validator.finish()).isEmpty();
    }

    @Test
    public void testValidateSomeMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.closed(start, end));

        for (Instant date : new Instant[]{startPlus10, startPlus20, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
        }

        assertThat(validator.finish()).contains(MapEntry.entry(start, ValidationResult.SUSPECT), MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
    }

    @Test
    public void testValidateNoneMissingNoEndDate() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.atLeast(start));

        for (Instant date : new Instant[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
        }

        assertThat(validator.finish()).isEmpty();
    }

    @Test
    public void testValidateSomeMissingNoEndDate() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.atLeast(start));

        for (Instant date : new Instant[]{startPlus10, startPlus20, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);
        }

        assertThat(validator.finish()).contains(MapEntry.entry(start, ValidationResult.SUSPECT), MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
    }

    @Test
    public void testValidateAllMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

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

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.closed(startPlus10, startPlus37));

        assertThat(validator.finish()).contains(
                MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
        );
    }

    @Test
    public void testValidateForIrregularStartTime() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.closed(startPlus7, startPlus30));

        assertThat(validator.finish()).contains(
                MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
        );
    }

    @Test
    public void testValidateForIrregularStartAndEndTime() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

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

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Range.closed(start, end));

        assertThat(validator.finish()).isEmpty();
    }

    @Test
    public void testPossibleIntOverflow() {
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);
        validator.init(channel, readingType, Range.atLeast(start));

        when(intervalReading.getTimeStamp()).thenReturn(start.plus(400, ChronoUnit.DAYS));
        assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.VALID);

        Map<Instant, ValidationResult> finish = validator.finish();

        assertThat(finish).isNotEmpty();
        assertThat(finish.keySet().stream().noneMatch(d -> d.compareTo(start) < 0)).isTrue();
    }

}