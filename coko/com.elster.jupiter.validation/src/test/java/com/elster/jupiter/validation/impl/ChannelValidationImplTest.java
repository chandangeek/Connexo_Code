package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidationImplTest extends EqualsContractTest {

    public static final long ID = 15L;

    private ChannelValidationImpl a;

    @Mock
    private Channel channel, channel1;
    @Mock
    private IMeterActivationValidation meterActivationValidation, meterActivationValidation1;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeterActivation meterActivation, meterActivation1;
    @Mock
    private ReadingQualityRecord readingQuality;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            setUp();
            a = new ChannelValidationImpl().init(meterActivationValidation, channel);
        }
        return a;
    }

    private void setUp() {
        when(dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new ChannelValidationImpl();
            }
        });
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getId()).thenReturn(1L);
        when(channel1.getId()).thenReturn(2L);
        when(meterActivationValidation.getMeterActivation()).thenReturn(meterActivation);
        when(meterActivationValidation1.getMeterActivation()).thenReturn(meterActivation);
        when(meterActivation.getStart()).thenReturn(Year.of(2013).atMonth(Month.JANUARY).atDay(1).atTime(14,0).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelValidationImpl channelValidation = new ChannelValidationImpl().init(meterActivationValidation, channel);
        return channelValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new ChannelValidationImpl().init(meterActivationValidation1, channel),
                new ChannelValidationImpl().init(meterActivationValidation, channel1),
                new ChannelValidationImpl().init(meterActivationValidation1, channel1)
                );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
    
    @Test
    public void lastCheckedTest() {
    	when(channel.findReadingQuality(any(Range.class))).thenReturn(ImmutableList.of(readingQuality));
    	when(readingQuality.hasReasonabilityCategory()).thenReturn(true);
    	ChannelValidationImpl channelValidation = new ChannelValidationImpl().init(meterActivationValidation, channel);
    	assertThat(channelValidation.getLastChecked()).isNotNull();
    	assertThat(channelValidation.getLastChecked()).isEqualTo(meterActivation.getStart());
    	ZonedDateTime dateTime = Year.of(2014).atMonth(Month.JANUARY).atDay(1).atStartOfDay(ZoneId.systemDefault());
    	channelValidation.updateLastChecked(dateTime.toInstant());
    	Instant instant = dateTime.minusMonths(1).toInstant();
    	channelValidation.updateLastChecked(instant);
    	verify(channel).findReadingQuality(Range.greaterThan(instant));
    	verify(readingQuality).delete();    	
    }
}