/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelValidationImplTest extends EqualsContractTest {

    public static final long ID = 15L;

    private ChannelValidationImpl a;

    @Mock
    private Channel channel, channel1;
    @Mock
    private ReadingQualityWithTypeFetcher fetcher;
    @Mock
    private ChannelsContainerValidation channelsContainerValidation, channelsContainerValidation1;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ChannelsContainer channelsContainer, channelsContainer1;
    @Mock
    private ReadingQualityRecord readingQuality;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            setUp();
            a = new ChannelValidationImpl().init(channelsContainerValidation, channel);
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
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getId()).thenReturn(1L);
        when(channel1.getId()).thenReturn(2L);
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(channelsContainerValidation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainerValidation1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getStart()).thenReturn(Year.of(2013).atMonth(Month.JANUARY).atDay(1).atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelValidationImpl channelValidation = new ChannelValidationImpl().init(channelsContainerValidation, channel);
        return channelValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new ChannelValidationImpl().init(channelsContainerValidation1, channel),
                new ChannelValidationImpl().init(channelsContainerValidation, channel1),
                new ChannelValidationImpl().init(channelsContainerValidation1, channel1)
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
    public void testLastChecked() {
        when(fetcher.ofQualitySystem(any(QualityCodeSystem.class))).thenReturn(fetcher);
        when(fetcher.inTimeInterval(any(Range.class))).thenReturn(fetcher);
        when(fetcher.ofAnyQualityIndexInCategories(anySetOf(QualityCodeCategory.class))).thenReturn(fetcher);
        when(fetcher.collect()).thenReturn(ImmutableList.of(readingQuality));
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(channelsContainerValidation.getRuleSet()).thenReturn(ruleSet);
        when(ruleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        ChannelValidationImpl channelValidation = new ChannelValidationImpl().init(channelsContainerValidation, channel);
        assertThat(channelValidation.getLastChecked()).isNotNull();
        assertThat(channelValidation.getLastChecked()).isEqualTo(channelsContainer.getStart());
        ZonedDateTime dateTime = Year.of(2014).atMonth(Month.JANUARY).atDay(1).atStartOfDay(ZoneId.systemDefault());
        channelValidation.updateLastChecked(dateTime.toInstant());
        Instant instant = dateTime.minusMonths(1).toInstant();
        channelValidation.updateLastChecked(instant);
        verify(fetcher).ofQualitySystem(QualityCodeSystem.MDM);
        verify(fetcher).inTimeInterval(Range.greaterThan(instant));
        verify(fetcher).ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION));
        verify(readingQuality).delete();
    }
}
