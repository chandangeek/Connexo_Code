/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ResultType;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DeviceLoadProfile} implementation
 *
 * @author gna
 * @since 4/04/12 - 13:55
 */
public class DeviceLoadProfileTest {

    @Test
    public void defaultSupportedNonIssueTest() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);

        assertThat(ResultType.Supported).isEqualTo(deviceLoadProfile.getResultType());
        assertThat(deviceLoadProfile.getIssues()).isEmpty();
    }

    @Test
    public void collectedDataNullTest() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);

        assertThat(deviceLoadProfile.getCollectedIntervalData()).isEmpty();
        assertThat(deviceLoadProfile.getChannelInfo()).isEmpty();
    }

    @Test
    public void collectedDataTest() {
        IntervalData intervalData = mock(IntervalData.class);
        when(intervalData.getEndTime()).thenReturn(new Date());
        ChannelInfo channelInfo = mock(ChannelInfo.class);

        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        deviceLoadProfile.setCollectedData(Arrays.asList(intervalData), Arrays.asList(channelInfo));

        assertThat(deviceLoadProfile.getCollectedIntervalData()).hasSize(1);
        assertThat(deviceLoadProfile.getChannelInfo()).hasSize(1);

        assertThat(deviceLoadProfile.getCollectedIntervalData().get(0)).isEqualTo(intervalData);
        assertThat(deviceLoadProfile.getChannelInfo().get(0)).isEqualTo(channelInfo);
    }

    @Test(expected = CodingException.class)
    public void collectedNullIntervalListTest() {
        ChannelInfo channelInfo = mock(ChannelInfo.class);

        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        deviceLoadProfile.setCollectedData(null, Arrays.asList(channelInfo));
    }

    @Test(expected = CodingException.class)
    public void collectedNullChannelInfoTest() {
        IntervalData intervalData = mock(IntervalData.class);

        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        deviceLoadProfile.setCollectedData(Arrays.asList(intervalData), null);
    }

    @Test
    public void dontStoreDefaultOlderValuesTest(){
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        assertThat(deviceLoadProfile.doStoreOlderValues()).isFalse();
    }

    @Test
    public void setStoreOlderValuesTest(){
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        deviceLoadProfile.setDoStoreOlderValues(true);
        assertThat(deviceLoadProfile.doStoreOlderValues()).isTrue();
    }

}