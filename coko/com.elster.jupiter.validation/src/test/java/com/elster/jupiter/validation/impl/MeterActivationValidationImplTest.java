package com.elster.jupiter.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.collect.Range;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationValidationImplTest {

    private static final Instant DATE1 = ZonedDateTime.of(2012, 11, 19, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE3 = ZonedDateTime.of(2012, 12, 24, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DATE4 = ZonedDateTime.of(2012, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final long FIRST_CHANNEL_ID = 1001L;
    private static final long SECOND_CHANNEL_ID = 1002L;

    MeterActivationValidationImpl meterActivationValidation;

    @Mock
    private MeterActivation meterActivation;
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
    private DataMapper<IChannelValidation> channelValidationFactory;
    @Mock
    private QueryExecutor<IChannelValidation> channelValidationQuery;
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
        when(dataModel.mapper(IChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.query(IChannelValidation.class, IMeterActivationValidation.class)).thenReturn(channelValidationQuery);
        when((Object) dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ChannelValidationImpl();
            }
        });
        when(channelValidationQuery.select(any())).thenReturn(Collections.emptyList());
        when(clock.instant()).thenReturn(DATE3);
        when(meteringService.findChannel(FIRST_CHANNEL_ID)).thenReturn(Optional.of(channel1));
        when(meteringService.findChannel(SECOND_CHANNEL_ID)).thenReturn(Optional.of(channel2));
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(FIRST_CHANNEL_ID);
        doReturn(Arrays.asList(readingType1)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(SECOND_CHANNEL_ID);
        doReturn(Arrays.asList(readingType2)).when(channel2).getReadingTypes();
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
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
        when(meterActivation.getRange()).thenReturn(Range.atLeast(DATE1));
        when(meterActivation.getStart()).thenReturn(DATE1);

        meterActivationValidation = new MeterActivationValidationImpl(dataModel, clock).init(meterActivation);
        meterActivationValidation.setRuleSet(validationRuleSet);
        meterActivationValidation.save();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidateWithoutChannels() throws Exception {
        when(meterActivation.getChannels()).thenReturn(Collections.<Channel>emptyList());

        meterActivationValidation.validate();

        assertThat(meterActivationValidation.getLastRun()).isEqualTo(DATE3);
    }

    @Test
    public void testValidateNoRulesApply() throws Exception {
        meterActivationValidation.validate();

        assertThat(meterActivationValidation.getChannelValidations()).isEmpty();
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

        meterActivationValidation.validate();

        assertThat(meterActivationValidation.getChannelValidations()).hasSize(1);
        IChannelValidation channelValidation = meterActivationValidation.getChannelValidations().iterator().next();
        assertThat(channelValidation.getChannel()).isEqualTo(channel1);
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);

    }

    @Test
    public void testValidateBothRulesApplyToBothChannels() throws Exception {
        doReturn(new HashSet<>(Arrays.asList(readingType1, readingType2))).when(rule1).getReadingTypes();
        doReturn(new HashSet<>(Arrays.asList(readingType1, readingType2))).when(rule2).getReadingTypes();
        when(channel1.getLastDateTime()).thenReturn(DATE4);
        when(channel2.getLastDateTime()).thenReturn(DATE4);

        meterActivationValidation.validate();

        assertThat(meterActivationValidation.getChannelValidations()).hasSize(2);
        Iterator<IChannelValidation> iterator = meterActivationValidation.getChannelValidations().iterator();
        IChannelValidation channelValidation = iterator.next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
        channelValidation = iterator.next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
    }

    @Test
    public void testSetMeterActivationValidationStatus() throws Exception {
        assertThat(meterActivationValidation.isActive()).isEqualTo(true);
        meterActivationValidation.deactivate();
        assertThat(meterActivationValidation.isActive()).isEqualTo(false);
        meterActivationValidation.activate();
        assertThat(meterActivationValidation.isActive()).isEqualTo(true);
    }

}
