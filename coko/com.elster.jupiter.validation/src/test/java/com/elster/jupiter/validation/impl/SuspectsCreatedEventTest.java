/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SuspectsCreatedEventTest {
    private static final Instant INSTANT1 = Instant.EPOCH;
    private static final Instant INSTANT2 = Instant.ofEpochMilli(1594139395359L);
    private static final long CHANNELS_CONTAINER_ID = 357;
    private static final long CHANNEL1_ID = 476;
    private static final long CHANNEL2_ID = 152;

    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private ReadingQualityRecord rqr1, rqr2, rqr3;

    @Before
    public void setUp() {
        when(channelsContainer.getId()).thenReturn(CHANNELS_CONTAINER_ID);
        when(channel1.getId()).thenReturn(CHANNEL1_ID);
        when(channel2.getId()).thenReturn(CHANNEL2_ID);
        when(rqr1.getChannel()).thenReturn(channel1);
        when(rqr1.getReadingTimestamp()).thenReturn(INSTANT2);
        when(rqr2.getChannel()).thenReturn(channel2);
        when(rqr2.getReadingTimestamp()).thenReturn(INSTANT1);
        when(rqr3.getChannel()).thenReturn(channel1);
        when(rqr3.getReadingTimestamp()).thenReturn(INSTANT1);
    }

    @Test
    public void testCreate() throws Exception {
        SuspectsCreatedEvent event = SuspectsCreatedEvent.create(channelsContainer, Arrays.asList(rqr1, rqr2, rqr3));
        assertThat(event.getChannelsContainerId()).isEqualTo(CHANNELS_CONTAINER_ID);
        assertThat(event.getSuspectedScope()).containsOnly(
                MapEntry.entry(CHANNEL1_ID, Range.closed(INSTANT1, INSTANT2)),
                MapEntry.entry(CHANNEL2_ID, Range.singleton(INSTANT1)));
    }

    @Test
    public void testNoSuspect() throws Exception {
        SuspectsCreatedEvent event = SuspectsCreatedEvent.create(channelsContainer, Collections.emptyList());
        assertThat(event.getChannelsContainerId()).isEqualTo(CHANNELS_CONTAINER_ID);
        assertThat(event.getSuspectedScope()).isEmpty();
    }
}
