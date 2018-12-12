/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link MatchingReadingTypeRequirementChannels} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (12:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class MatchingReadingTypeRequirementChannelsTest {

    private static final Range<Instant> TEST_RANGE = Range.atLeast(Instant.ofEpochMilli(-21082800000L));    // 1969, May 2nd 01:40 (CET) if you must know

    @Mock
    private ReadingTypeRequirement requirement;

    @Test
    public void testGetRange() {
        MatchingReadingTypeRequirementChannels testInstance = this.testInstance();

        // Business method
        Range<Instant> range = testInstance.getRange();

        // Asserts
        assertThat(range).isEqualTo(TEST_RANGE);
    }

    @Test
    public void getMatchingChannelsWithoutAdding() {
        MatchingReadingTypeRequirementChannels testInstance = this.testInstance();

        // Business method
        List<Channel> matchingChannels = testInstance.getMatchingChannels(this.requirement);

        // Asserts
        assertThat(matchingChannels).isEmpty();
    }

    @Test
    public void getMatchingChannelsAfterAddingOne() {
        MatchingReadingTypeRequirementChannels testInstance = this.testInstance();
        Channel channel = mock(Channel.class);
        testInstance.addMatch(this.requirement, channel);

        // Business method
        List<Channel> matchingChannels = testInstance.getMatchingChannels(this.requirement);

        // Asserts
        assertThat(matchingChannels).containsOnly(channel);
    }

    @Test
    public void getMatchingChannelsAfterAddingMultiple() {
        MatchingReadingTypeRequirementChannels testInstance = this.testInstance();
        Channel channel1 = mock(Channel.class);
        Channel channel2 = mock(Channel.class);
        Channel channel3 = mock(Channel.class);
        testInstance.addMatch(this.requirement, channel1);
        testInstance.addMatch(this.requirement, channel2);
        testInstance.addMatch(this.requirement, channel3);

        // Business method
        List<Channel> matchingChannels = testInstance.getMatchingChannels(this.requirement);

        // Asserts
        assertThat(matchingChannels).containsOnly(channel1, channel2, channel3);
    }

    @Test
    public void getMatchingChannelsAfterAddingOnMultipleRequirements() {
        MatchingReadingTypeRequirementChannels testInstance = this.testInstance();
        ReadingTypeRequirement otherRequirement = mock(ReadingTypeRequirement.class);
        Channel channel1 = mock(Channel.class);
        Channel channel2 = mock(Channel.class);
        Channel channel3 = mock(Channel.class);
        testInstance.addMatch(this.requirement, channel1);
        testInstance.addMatch(this.requirement, channel2);
        testInstance.addMatch(otherRequirement, channel3);

        // Business method
        List<Channel> matchingChannels = testInstance.getMatchingChannels(this.requirement);

        // Asserts
        assertThat(matchingChannels).containsOnly(channel1, channel2);
    }

    private MatchingReadingTypeRequirementChannels testInstance() {
        return new MatchingReadingTypeRequirementChannels(TEST_RANGE);
    }

}