/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelsContainerValidationImplTest {

    private static final Instant DATE1 = ZonedDateTime.of(2012, 11, 19, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE3 = ZonedDateTime.of(2012, 12, 24, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE4 = ZonedDateTime.of(2012, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final long FIRST_CHANNEL_ID = 1001L;
    private static final long SECOND_CHANNEL_ID = 1002L;

    ChannelsContainerValidationImpl channelsContainerValidation;

    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private IValidationRuleSet validationRuleSet;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel1, channel2;
    @Mock
    private IValidationRule rule1, rule2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private Clock clock;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataMapper<ChannelValidation> channelValidationFactory;
    @Mock
    private QueryExecutor<ChannelValidation> channelValidationQuery;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private Validator validator;
    @Mock
    private IntervalReadingRecord intervalReadingRecord;
    @Mock
    private IValidationRuleSetVersion ruleSetVersion1, ruleSetVersion2;


    @Before
    public void setUp() {
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.query(ChannelValidation.class, ChannelsContainerValidation.class)).thenReturn(channelValidationQuery);
        when((Object) dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(invocationOnMock -> new ChannelValidationImpl());
        when(channelValidationQuery.select(any())).thenReturn(Collections.emptyList());
        when(clock.instant()).thenReturn(DATE3);
        when(meteringService.findChannel(FIRST_CHANNEL_ID)).thenReturn(Optional.of(channel1));
        when(meteringService.findChannel(SECOND_CHANNEL_ID)).thenReturn(Optional.of(channel2));
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(FIRST_CHANNEL_ID);
        doReturn(Arrays.asList(readingType1)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(SECOND_CHANNEL_ID);
        doReturn(Arrays.asList(readingType2)).when(channel2).getReadingTypes();
        when(channel1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationRuleSet.getRules()).thenReturn(Arrays.asList(rule1, rule2));
        when(validationRuleSet.getRuleSetVersions()).thenReturn(Arrays.asList(ruleSetVersion1, ruleSetVersion2));
        when(ruleSetVersion1.getNotNullStartDate()).thenReturn(Instant.EPOCH);
        when(ruleSetVersion1.getNotNullEndDate()).thenReturn(DATE1);
        when(ruleSetVersion2.getNotNullStartDate()).thenReturn(DATE4.plusSeconds(1));
        when(ruleSetVersion2.getNotNullEndDate()).thenReturn(Instant.MAX);
        when(ruleSetVersion2.getRules()).thenReturn(Arrays.asList(rule1, rule2));
        when(rule1.isActive()).thenReturn(true);
        when(rule2.isActive()).thenReturn(true);
        when(rule1.createNewValidator()).thenReturn(validator);
        when(rule2.createNewValidator()).thenReturn(validator);

        when(channel1.getMainReadingType()).thenReturn(readingType1);
        when(channel2.getMainReadingType()).thenReturn(readingType2);
        when(readingType1.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType2.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(channelsContainer.getRange()).thenReturn(Range.atLeast(DATE1));
        when(channelsContainer.getStart()).thenReturn(DATE1);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());

        channelsContainerValidation = new ChannelsContainerValidationImpl(dataModel, clock).init(channelsContainer);
        channelsContainerValidation.setRuleSet(validationRuleSet);
        channelsContainerValidation.save();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidateWithoutChannels() throws Exception {
        when(channelsContainer.getChannels()).thenReturn(Collections.<Channel>emptyList());

        channelsContainerValidation.validate();

        assertThat(channelsContainerValidation.getLastRun()).isEqualTo(DATE3);
    }

    @Test
    public void testValidateNoRulesApply() throws Exception {
        channelsContainerValidation.validate();

        assertThat(channelsContainerValidation.getChannelValidations()).isEmpty();
    }

    @Test
    public void testValidateOneRuleAppliesToOneChannel() throws Exception {
        doReturn(Collections.singleton(readingType1)).when(rule1).getReadingTypes();
        when(channel1.getLastDateTime()).thenReturn(DATE4);
        when(channel1.isRegular()).thenReturn(true);
        when(channel1.getIntervalReadings(Range.openClosed(DATE1, DATE4))).thenReturn(Arrays.asList(intervalReadingRecord));
        when(intervalReadingRecord.filter(any())).thenReturn(intervalReadingRecord);
        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE4);
        when(validator.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.VALID);

        channelsContainerValidation.validate();

        assertThat(channelsContainerValidation.getChannelValidations()).hasSize(1);
        ChannelValidation channelValidation = channelsContainerValidation.getChannelValidations().iterator().next();
        assertThat(channelValidation.getChannel()).isEqualTo(channel1);
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);

    }

    @Test
    public void testValidateBothRulesApplyToBothChannels() throws Exception {
        doReturn(new HashSet<>(Arrays.asList(readingType1, readingType2))).when(rule1).getReadingTypes();
        doReturn(new HashSet<>(Arrays.asList(readingType1, readingType2))).when(rule2).getReadingTypes();
        when(channel1.getLastDateTime()).thenReturn(DATE4);
        when(channel2.getLastDateTime()).thenReturn(DATE4);

        channelsContainerValidation.validate();

        assertThat(channelsContainerValidation.getChannelValidations()).hasSize(2);
        Iterator<ChannelValidation> iterator = channelsContainerValidation.getChannelValidations().iterator();
        ChannelValidation channelValidation = iterator.next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
        channelValidation = iterator.next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
    }

    @Test
    public void testSetChannelsContainerValidationStatus() throws Exception {
        assertThat(channelsContainerValidation.isActive()).isEqualTo(true);
        channelsContainerValidation.deactivate();
        assertThat(channelsContainerValidation.isActive()).isEqualTo(false);
        channelsContainerValidation.activate();
        assertThat(channelsContainerValidation.isActive()).isEqualTo(true);
    }

}
