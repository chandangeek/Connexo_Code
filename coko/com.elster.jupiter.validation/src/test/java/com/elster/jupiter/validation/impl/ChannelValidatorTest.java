package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidatorTest {

    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2014, 1, 14, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_TIME = ZonedDateTime.of(2014, 1, 14, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    public static final Range<Instant> RANGE = Range.closedOpen(START_TIME.toInstant(), END_TIME.toInstant());

    private ReadingQualityType readingQualityType;

    @Mock
    private Channel channel;
    @Mock
    private IValidationRule rule;
    @Mock
    private ReadingType readingType;
    @Mock
    private Validator validator;
    @Mock
    private ReadingQualityRecord readingQualityRecord, newReadingQualityRecord;
    @Mock
    private IntervalReadingRecord readingRecord;


    @Before
    public void setUp() {
        doReturn(ImmutableSet.of(readingType)).when(rule).getReadingTypes();
        doReturn(Arrays.asList(readingType)).when(channel).getReadingTypes();
        doReturn(validator).when(rule).createNewValidator();
        doReturn(Arrays.asList(readingQualityRecord)).when(channel).findReadingQuality(RANGE);
        doReturn(START_TIME.toInstant()).when(readingQualityRecord).getReadingTimestamp();
        doReturn(false).when(readingQualityRecord).isActual();
        doReturn(true).when(channel).isRegular();
        doReturn(Arrays.asList(readingRecord)).when(channel).getIntervalReadings(RANGE);
        doReturn(newReadingQualityRecord).when(channel).createReadingQuality(any(), eq(readingType), eq(readingRecord));
        doReturn(START_TIME.toInstant()).when(newReadingQualityRecord).getReadingTimestamp();
        doReturn(START_TIME.toInstant()).when(readingRecord).getTimeStamp();
        doReturn(readingType).when(readingQualityRecord).getReadingType();
        doReturn(readingType).when(newReadingQualityRecord).getReadingType();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidateRule() throws Exception {
        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.BATTERYLOW));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfExists() throws Exception {
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpPreExistingQualityType(validationQuality);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfExistsNonActual() throws Exception {
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpPreExistingQualityType(validationQuality);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
        verify(readingQualityRecord).makeActual();
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfEstimated() throws Exception {
        doReturn(true).when(readingQualityRecord).isActual();
        doReturn(true).when(readingQualityRecord).hasEstimatedCategory();

        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfConfirmed() throws Exception {
        doReturn(true).when(readingQualityRecord).isActual();
        doReturn(true).when(readingQualityRecord).isConfirmed();

        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    private void setUpPreExistingQualityType(ReadingQualityType type) {
        readingQualityType = type;
        doReturn(readingQualityType).when(readingQualityRecord).getType();
    }

    private void setUpNewQualityType(ReadingQualityType type) {
        doReturn(type).when(rule).getReadingQualityType();
        doReturn(type).when(newReadingQualityRecord).getType();
    }
}