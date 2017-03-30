/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidatorTest {

    private static final ZonedDateTime START_TIME = ZonedDateTime.of(2014, 1, 14, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_TIME = ZonedDateTime.of(2014, 1, 14, 14, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final Range<Instant> RANGE = Range.closedOpen(START_TIME.toInstant(), END_TIME.toInstant());

    @Mock
    private Channel channel;
    @Mock
    private IValidationRule rule;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ReadingType readingType;
    @Mock
    private Validator validator;
    @Mock
    private ReadingQualityRecord readingQualityRecord, newReadingQualityRecord;
    @Mock
    private IntervalReadingRecord readingRecord;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingQualityWithTypeFetcher fetcher;

    @Before
    public void setUp() {
        doReturn(ImmutableSet.of(readingType)).when(rule).getReadingTypes();
        doReturn(Collections.singletonList(readingType)).when(channel).getReadingTypes();
        doReturn(validator).when(rule).createNewValidator();
        doReturn(ValidationAction.FAIL).when(rule).getAction();
        doReturn(ruleSet).when(rule).getRuleSet();
        doReturn(QualityCodeSystem.MDC).when(ruleSet).getQualityCodeSystem();
        doReturn(fetcher).when(channel).findReadingQualities();
        when(fetcher.inTimeInterval(RANGE).collect()).thenReturn(Collections.singletonList(readingQualityRecord));
        doReturn(START_TIME.toInstant()).when(readingQualityRecord).getReadingTimestamp();
        doReturn(false).when(readingQualityRecord).isActual();
        doReturn(true).when(channel).isRegular();
        doReturn(Collections.singletonList(readingRecord)).when(channel).getIntervalReadings(RANGE);
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
        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.BATTERYLOW));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel).createReadingQuality(validationQuality, readingType, readingRecord);
        verify(channel).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, readingRecord);
    }

    @Test
    public void testMdmValidateRule() throws Exception {
        doReturn(QualityCodeSystem.MDM).when(ruleSet).getQualityCodeSystem();
        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel).createReadingQuality(validationQuality, readingType, readingRecord);
        verify(channel).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, readingRecord);
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfExists() throws Exception {
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001);
        setUpPreExistingQualityType(validationQuality);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    @Test
    public void testValidateRuleNoQualityWrittenIfExistsNonActual() throws Exception {
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001);
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

        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001);
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

        setUpPreExistingQualityType(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED));
        ReadingQualityType validationQuality = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001);
        setUpNewQualityType(validationQuality);

        doReturn(ValidationResult.SUSPECT).when(validator).validate(readingRecord);

        ChannelValidator channelValidator = new ChannelValidator(channel, RANGE);

        channelValidator.validateRule(rule);

        verify(channel, never()).createReadingQuality(validationQuality, readingType, readingRecord);
    }

    private void setUpPreExistingQualityType(ReadingQualityType type) {
        doReturn(type).when(readingQualityRecord).getType();
    }

    private void setUpNewQualityType(ReadingQualityType type) {
        doReturn(type).when(rule).getReadingQualityType();
        doReturn(type).when(newReadingQualityRecord).getType();
    }
}
