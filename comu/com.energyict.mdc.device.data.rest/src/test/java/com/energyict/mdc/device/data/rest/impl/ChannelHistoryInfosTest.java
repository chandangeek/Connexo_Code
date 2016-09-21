package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.DataLoggerReference;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelHistoryInfosTest {

    private final String myDeviceName = "MyDeviceName";
    private final Long channelId = 6542L;

    @Test
    public void getNoSlavesLinkedYetTest() {
        assertThat(ChannelHistoryInfos.from(Collections.emptyList()).channelHistory).isEmpty();
    }

    @Test
    public void getWithOneActiveSlaveTest() {
        long startDate = 1467496800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        com.energyict.mdc.device.data.Channel originChannel = mockMdcChannel(channelId);

        DataLoggerChannelUsage dataLoggerChannelUsage = mockDataLoggerChannelUsage(startDate, null, channel1ReadingType, slaveChannel, originChannel);
        List<ChannelHistoryInfo> channelHistory = ChannelHistoryInfos.from(Collections.singletonList(dataLoggerChannelUsage)).channelHistory;
        assertThat(channelHistory).isNotEmpty();
        assertThat(channelHistory.get(0).startDate).isEqualTo(startDate);
        assertThat(channelHistory.get(0).deviceName).isEqualTo(myDeviceName);
        assertThat(channelHistory.get(0).channelId).isEqualTo(channelId);
        assertThat(channelHistory.get(0).endDate).isNull();
    }

    @Test
    public void getWithOneSlaveWhichIsNotLinkedTest() {
        long startDate = 1467496800000L;
        long endDate = 1467596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        com.energyict.mdc.device.data.Channel originChannel = mockMdcChannel(channelId);

        DataLoggerChannelUsage dataLoggerChannelUsage = mockDataLoggerChannelUsage(startDate, endDate, channel1ReadingType, slaveChannel, originChannel);
        List<ChannelHistoryInfo> channelHistory = ChannelHistoryInfos.from(Collections.singletonList(dataLoggerChannelUsage)).channelHistory;
        assertThat(channelHistory).hasSize(2);
        assertThat(channelHistory.get(0).startDate).isEqualTo(endDate);
        assertThat(channelHistory.get(0).deviceName).isNull();
        assertThat(channelHistory.get(0).channelId).isNull();
        assertThat(channelHistory.get(0).endDate).isNull();
        assertThat(channelHistory.get(1).startDate).isEqualTo(startDate);
        assertThat(channelHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(channelHistory.get(1).channelId).isEqualTo(channelId);
        assertThat(channelHistory.get(1).endDate).isEqualTo(endDate);
    }

    @Test
    public void getWithTwoConsecutiveSlavesTest() {
        long startDate = 1467496800000L;
        long endDateFirstStartDateNew = 1467596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        com.energyict.mdc.device.data.Channel originChannel1 = mockMdcChannel(channelId);
        long channelId2 = 123L;
        com.energyict.mdc.device.data.Channel originChannel2 = mockMdcChannel(channelId2);

        DataLoggerChannelUsage dataLoggerChannelUsage1 = mockDataLoggerChannelUsage(startDate, endDateFirstStartDateNew, channel1ReadingType, slaveChannel, originChannel1);
        DataLoggerChannelUsage dataLoggerChannelUsage2 = mockDataLoggerChannelUsage(endDateFirstStartDateNew, null, channel1ReadingType, slaveChannel, originChannel2);
        List<ChannelHistoryInfo> channelHistory = ChannelHistoryInfos.from(Arrays.asList(dataLoggerChannelUsage2, dataLoggerChannelUsage1)).channelHistory;
        assertThat(channelHistory).hasSize(2);
        assertThat(channelHistory.get(0).startDate).isEqualTo(endDateFirstStartDateNew);
        assertThat(channelHistory.get(0).deviceName).isEqualTo(myDeviceName);
        assertThat(channelHistory.get(0).channelId).isEqualTo(channelId2);
        assertThat(channelHistory.get(0).endDate).isNull();
        assertThat(channelHistory.get(1).startDate).isEqualTo(startDate);
        assertThat(channelHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(channelHistory.get(1).channelId).isEqualTo(this.channelId);
        assertThat(channelHistory.get(1).endDate).isEqualTo(endDateFirstStartDateNew);
    }

    @Test
    public void getWithTwoNoneConsecutiveNoneLinkedAnymoreSlavesTest() {
        long startDate1 = 1467496800000L;
        long endDate1 = 1467596800000L;
        long startDate2 = 1468496800000L;
        long endDate2 = 1469596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        com.energyict.mdc.device.data.Channel originChannel1 = mockMdcChannel(channelId);
        long channelId2 = 123L;
        com.energyict.mdc.device.data.Channel originChannel2 = mockMdcChannel(channelId2);

        DataLoggerChannelUsage dataLoggerChannelUsage1 = mockDataLoggerChannelUsage(startDate1, endDate1, channel1ReadingType, slaveChannel, originChannel1);
        DataLoggerChannelUsage dataLoggerChannelUsage2 = mockDataLoggerChannelUsage(startDate2, endDate2, channel1ReadingType, slaveChannel, originChannel2);
        List<ChannelHistoryInfo> channelHistory = ChannelHistoryInfos.from(Arrays.asList(dataLoggerChannelUsage2, dataLoggerChannelUsage1)).channelHistory;
        assertThat(channelHistory).hasSize(4);
        assertThat(channelHistory.get(0).startDate).isEqualTo(endDate2);
        assertThat(channelHistory.get(0).deviceName).isNull();
        assertThat(channelHistory.get(0).channelId).isNull();
        assertThat(channelHistory.get(0).endDate).isNull();
        assertThat(channelHistory.get(1).startDate).isEqualTo(startDate2);
        assertThat(channelHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(channelHistory.get(1).channelId).isEqualTo(channelId2);
        assertThat(channelHistory.get(1).endDate).isEqualTo(endDate2);
        assertThat(channelHistory.get(2).startDate).isEqualTo(endDate1);
        assertThat(channelHistory.get(2).deviceName).isNull();
        assertThat(channelHistory.get(2).channelId).isNull();
        assertThat(channelHistory.get(2).endDate).isEqualTo(startDate2);
        assertThat(channelHistory.get(3).startDate).isEqualTo(startDate1);
        assertThat(channelHistory.get(3).deviceName).isEqualTo(this.myDeviceName);
        assertThat(channelHistory.get(3).channelId).isEqualTo(this.channelId);
        assertThat(channelHistory.get(3).endDate).isEqualTo(endDate1);
    }

    private com.energyict.mdc.device.data.Channel mockMdcChannel(Long channelId) {
        com.energyict.mdc.device.data.Channel originChannel = mock(com.energyict.mdc.device.data.Channel.class);
        when(originChannel.getId()).thenReturn(channelId);
        return originChannel;
    }

    private DataLoggerChannelUsage mockDataLoggerChannelUsage(Long startDate, Long endDate, ReadingType channel1ReadingType, Channel slaveChannel, com.energyict.mdc.device.data.Channel originChannel) {
        DataLoggerReference dataLoggerReference = getDataLoggerReference(originChannel, channel1ReadingType);
        DataLoggerChannelUsage dataLoggerChannelUsage = mock(DataLoggerChannelUsage.class);
        when(dataLoggerChannelUsage.getSlaveChannel()).thenReturn(slaveChannel);
        when(dataLoggerChannelUsage.getDataLoggerReference()).thenReturn(dataLoggerReference);
        if (endDate != null) {
            when(dataLoggerChannelUsage.getRange()).thenReturn(Range.open(Instant.ofEpochMilli(startDate), Instant.ofEpochMilli(endDate)));
        } else {
            when(dataLoggerChannelUsage.getRange()).thenReturn(Range.atLeast(Instant.ofEpochMilli(startDate)));
        }
        return dataLoggerChannelUsage;
    }

    private Channel mockSlaveChannel(ReadingType channelReadingType) {
        Channel slaveChannel = mock(Channel.class);
        doReturn(Collections.singletonList(channelReadingType)).when(slaveChannel).getReadingTypes();
        return slaveChannel;
    }

    private DataLoggerReference getDataLoggerReference(com.energyict.mdc.device.data.Channel originChannel, ReadingType channelReadingType) {
        Device origin = mock(Device.class);
        when(originChannel.getReadingType()).thenReturn(channelReadingType);
        when(origin.getChannels()).thenReturn(Collections.singletonList(originChannel));
        when(origin.getName()).thenReturn(myDeviceName);
        DataLoggerReference dataLoggerReference = mock(DataLoggerReference.class);
        when(dataLoggerReference.getOrigin()).thenReturn(origin);
        return dataLoggerReference;
    }
}