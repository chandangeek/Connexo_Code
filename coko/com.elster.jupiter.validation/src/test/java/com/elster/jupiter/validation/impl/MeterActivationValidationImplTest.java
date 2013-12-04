package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationValidationImplTest {

    private static final Date DATE1 = new DateMidnight(2012, 11, 19).toDate();
    private static final Date DATE2 = new DateMidnight(2012, 12, 19).toDate();
    private static final Date DATE3 = new DateMidnight(2012, 12, 24).toDate();
    private static final Date DATE4 = new DateMidnight(2012, 12, 1).toDate();
    private static final Interval INTERVAL = new Interval(DATE1, DATE2);
    private static final long FIRST_CHANNEL_ID = 1001L;
    private static final long SECOND_CHANNEL_ID = 1002L;

    MeterActivationValidationImpl meterActivationValidation;

    @Mock
    private MeterActivation meterActivation;
    @Mock
    private IValidationRuleSet validationRuleSet;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private IValidationRule rule1, rule2;

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getClock().now()).thenReturn(DATE3);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(FIRST_CHANNEL_ID);
        when(channel2.getId()).thenReturn(SECOND_CHANNEL_ID);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(serviceLocator.getMeteringService().findChannel(FIRST_CHANNEL_ID)).thenReturn(Optional.of(channel1));
        when(serviceLocator.getMeteringService().findChannel(SECOND_CHANNEL_ID)).thenReturn(Optional.of(channel2));
        when(validationRuleSet.getRules()).thenReturn(Arrays.asList(rule1, rule2));

        meterActivationValidation = new MeterActivationValidationImpl(meterActivation);
        meterActivationValidation.setRuleSet(validationRuleSet);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testValidateWithoutChannels() throws Exception {
        when(meterActivation.getChannels()).thenReturn(Collections.<Channel>emptyList());

        meterActivationValidation.validate(INTERVAL);

        assertThat(meterActivationValidation.getLastRun()).isEqualTo(DATE3);
    }

    @Test
    public void testValidateNoRulesApply() throws Exception {
        meterActivationValidation.validate(INTERVAL);

        assertThat(meterActivationValidation.getChannelValidations()).isEmpty();
    }

    @Test
    public void testValidateOneRuleAppliesToOneChannel() throws Exception {
        when(rule1.validateChannel(channel1, INTERVAL)).thenReturn(DATE4);

        meterActivationValidation.validate(INTERVAL);

        assertThat(meterActivationValidation.getChannelValidations()).hasSize(1);
        ChannelValidation channelValidation = meterActivationValidation.getChannelValidations().iterator().next();
        assertThat(channelValidation.getChannel()).isEqualTo(channel1);
    }

    @Test
    public void testValidateBothRulesApplyToBothChannels() throws Exception {
        when(rule1.validateChannel(channel1, INTERVAL)).thenReturn(DATE4);
        when(rule1.validateChannel(channel2, INTERVAL)).thenReturn(DATE2);
        when(rule2.validateChannel(channel1, INTERVAL)).thenReturn(DATE2);
        when(rule2.validateChannel(channel2, INTERVAL)).thenReturn(DATE4);

        meterActivationValidation.validate(INTERVAL);

        assertThat(meterActivationValidation.getChannelValidations()).hasSize(2);
        ChannelValidation channelValidation = meterActivationValidation.getChannelValidations().iterator().next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
        channelValidation = meterActivationValidation.getChannelValidations().iterator().next();
        assertThat(channelValidation.getLastChecked()).isEqualTo(DATE4);
    }

}
