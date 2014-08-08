package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.rules.MockitoRule;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    public DateTime base;
    public Date start;
    public Date startPlus7;
    public Date startPlus10;
    public Date startPlus20;
    public Date startPlus30;
    public Date startPlus37;
    public Date startPlus40;
    public Date startPlus50;
    public Date end;

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

    public MissingValuesValidatorTest(DateTime base) {
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
        start = base.toDate();
        startPlus7 = base.plusMinutes(7).toDate();
        startPlus10 = base.plusMinutes(10).toDate();
        startPlus20 = base.plusMinutes(20).toDate();
        startPlus30 = base.plusMinutes(30).toDate();
        startPlus37 = base.plusMinutes(37).toDate();
        startPlus40 = base.plusMinutes(40).toDate();
        startPlus50 = base.plusMinutes(50).toDate();
        end = base.plusMinutes(60).toDate();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidateNoneMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, new Interval(start, end));

        for (Date date : new Date[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.PASS);
        }

        assertThat(validator.finish()).isEmpty();
    }

    @Test
    public void testValidateSomeMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, new Interval(start, end));

        for (Date date : new Date[]{startPlus10, startPlus20, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.PASS);
        }

        assertThat(validator.finish()).contains(MapEntry.entry(start, ValidationResult.SUSPECT), MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
    }

    @Test
    public void testValidateNoneMissingNoEndDate() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Interval.startAt(start));

        for (Date date : new Date[]{start, startPlus10, startPlus20, startPlus30, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.PASS);
        }

        assertThat(validator.finish()).isEmpty();
    }

    @Test
    public void testValidateSomeMissingNoEndDate() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, Interval.startAt(start));

        for (Date date : new Date[]{startPlus10, startPlus20, startPlus50, startPlus40, end}) {
            when(intervalReading.getTimeStamp()).thenReturn(date);
            assertThat(validator.validate(intervalReading)).isEqualTo(ValidationResult.PASS);
        }

        assertThat(validator.finish()).contains(MapEntry.entry(start, ValidationResult.SUSPECT), MapEntry.entry(startPlus30, ValidationResult.SUSPECT));
    }

    @Test
    public void testValidateAllMissing() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, new Interval(start, end));

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

        validator.init(channel, readingType, new Interval(startPlus10, startPlus37));

        assertThat(validator.finish()).contains(
                MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
        );
    }

    @Test
    public void testValidateForIrregularStartTime() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, new Interval(startPlus7, startPlus30));

        assertThat(validator.finish()).contains(
                MapEntry.entry(startPlus10, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus20, ValidationResult.SUSPECT),
                MapEntry.entry(startPlus30, ValidationResult.SUSPECT)
        );
    }

    @Test
    public void testValidateForIrregularStartAndEndTime() {

        MissingValuesValidator validator = new MissingValuesValidator(thesaurus, propertySpecService);

        validator.init(channel, readingType, new Interval(startPlus7, startPlus37));

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

        validator.init(channel, readingType, new Interval(start, end));

        assertThat(validator.finish()).isEmpty();
    }

}