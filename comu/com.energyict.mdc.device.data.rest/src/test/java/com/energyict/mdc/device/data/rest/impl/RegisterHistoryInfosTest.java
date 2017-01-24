package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
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

/**
 * Copyrights EnergyICT
 * Date: 12.07.16
 * Time: 16:25
 */
public class RegisterHistoryInfosTest {

    private final String myDeviceName = "MyDeviceMRID";
    private final Long registerId = 6542L;

    @Test
    public void getNoSlavesLinkedYetTest() {
        assertThat(RegisterHistoryInfos.from(Collections.emptyList()).registerHistory).isEmpty();
    }

    @Test
    public void getWithOneActiveSlaveTest() {
        long startDate = 1467496800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        Register originRegister = mockRegister(registerId);

        DataLoggerChannelUsage dataLoggerChannelUsage = mockDataLoggerChannelUsage(startDate, null, channel1ReadingType, slaveChannel, originRegister);
        List<RegisterHistoryInfo> registerHistory = RegisterHistoryInfos.from(Collections.singletonList(dataLoggerChannelUsage)).registerHistory;
        assertThat(registerHistory).isNotEmpty();
        assertThat(registerHistory.get(0).startDate).isEqualTo(startDate);
        assertThat(registerHistory.get(0).deviceName).isEqualTo(myDeviceName);
        assertThat(registerHistory.get(0).registerId).isEqualTo(registerId);
        assertThat(registerHistory.get(0).endDate).isNull();
    }

    @Test
    public void getWithOneSlaveWhichIsNotLinkedTest() {
        long startDate = 1467496800000L;
        long endDate = 1467596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        Register originRegister = mockRegister(registerId);

        DataLoggerChannelUsage dataLoggerChannelUsage = mockDataLoggerChannelUsage(startDate, endDate, channel1ReadingType, slaveChannel, originRegister);
        List<RegisterHistoryInfo> registerHistory = RegisterHistoryInfos.from(Collections.singletonList(dataLoggerChannelUsage)).registerHistory;
        assertThat(registerHistory).hasSize(2);
        assertThat(registerHistory.get(0).startDate).isEqualTo(endDate);
        assertThat(registerHistory.get(0).deviceName).isNull();
        assertThat(registerHistory.get(0).registerId).isNull();
        assertThat(registerHistory.get(0).endDate).isNull();
        assertThat(registerHistory.get(1).startDate).isEqualTo(startDate);
        assertThat(registerHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(registerHistory.get(1).registerId).isEqualTo(registerId);
        assertThat(registerHistory.get(1).endDate).isEqualTo(endDate);
    }

    @Test
    public void getWithTwoConsecutiveSlavesTest() {
        long startDate = 1467496800000L;
        long endDateFirstStartDateNew = 1467596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        Register originChannel1 = mockRegister(registerId);
        long registerId2 = 123L;
        Register originChannel2 = mockRegister(registerId2);

        DataLoggerChannelUsage dataLoggerChannelUsage1 = mockDataLoggerChannelUsage(startDate, endDateFirstStartDateNew, channel1ReadingType, slaveChannel, originChannel1);
        DataLoggerChannelUsage dataLoggerChannelUsage2 = mockDataLoggerChannelUsage(endDateFirstStartDateNew, null, channel1ReadingType, slaveChannel, originChannel2);
        List<RegisterHistoryInfo> registerHistory = RegisterHistoryInfos.from(Arrays.asList(dataLoggerChannelUsage2, dataLoggerChannelUsage1)).registerHistory;
        assertThat(registerHistory).hasSize(2);
        assertThat(registerHistory.get(0).startDate).isEqualTo(endDateFirstStartDateNew);
        assertThat(registerHistory.get(0).deviceName).isEqualTo(myDeviceName);
        assertThat(registerHistory.get(0).registerId).isEqualTo(registerId2);
        assertThat(registerHistory.get(0).endDate).isNull();
        assertThat(registerHistory.get(1).startDate).isEqualTo(startDate);
        assertThat(registerHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(registerHistory.get(1).registerId).isEqualTo(this.registerId);
        assertThat(registerHistory.get(1).endDate).isEqualTo(endDateFirstStartDateNew);
    }

    @Test
    public void getWithTwoNoneConsecutiveNoneLinkedAnymoreSlavesTest() {
        long startDate1 = 1467496800000L;
        long endDate1 = 1467596800000L;
        long startDate2 = 1468496800000L;
        long endDate2 = 1469596800000L;
        ReadingType channel1ReadingType = mock(ReadingType.class);
        Channel slaveChannel = mockSlaveChannel(channel1ReadingType);
        Register originChannel1 = mockRegister(registerId);
        long registerId2 = 123L;
        Register originChannel2 = mockRegister(registerId2);

        DataLoggerChannelUsage dataLoggerChannelUsage1 = mockDataLoggerChannelUsage(startDate1, endDate1, channel1ReadingType, slaveChannel, originChannel1);
        DataLoggerChannelUsage dataLoggerChannelUsage2 = mockDataLoggerChannelUsage(startDate2, endDate2, channel1ReadingType, slaveChannel, originChannel2);
        List<RegisterHistoryInfo> registerHistory = RegisterHistoryInfos.from(Arrays.asList(dataLoggerChannelUsage2, dataLoggerChannelUsage1)).registerHistory;
        assertThat(registerHistory).hasSize(4);
        assertThat(registerHistory.get(0).startDate).isEqualTo(endDate2);
        assertThat(registerHistory.get(0).deviceName).isNull();
        assertThat(registerHistory.get(0).registerId).isNull();
        assertThat(registerHistory.get(0).endDate).isNull();
        assertThat(registerHistory.get(1).startDate).isEqualTo(startDate2);
        assertThat(registerHistory.get(1).deviceName).isEqualTo(myDeviceName);
        assertThat(registerHistory.get(1).registerId).isEqualTo(registerId2);
        assertThat(registerHistory.get(1).endDate).isEqualTo(endDate2);
        assertThat(registerHistory.get(2).startDate).isEqualTo(endDate1);
        assertThat(registerHistory.get(2).deviceName).isNull();
        assertThat(registerHistory.get(2).registerId).isNull();
        assertThat(registerHistory.get(2).endDate).isEqualTo(startDate2);
        assertThat(registerHistory.get(3).startDate).isEqualTo(startDate1);
        assertThat(registerHistory.get(3).deviceName).isEqualTo(this.myDeviceName);
        assertThat(registerHistory.get(3).registerId).isEqualTo(this.registerId);
        assertThat(registerHistory.get(3).endDate).isEqualTo(endDate1);
    }

    private Register mockRegister(Long registerId) {
        Register originRegister = mock(Register.class);
        when(originRegister.getRegisterSpecId()).thenReturn(registerId);
        return originRegister;
    }

    private DataLoggerChannelUsage mockDataLoggerChannelUsage(Long startDate, Long endDate, ReadingType channel1ReadingType, Channel slaveChannel, Register originRegister) {
        DataLoggerReference dataLoggerReference = getDataLoggerReference(originRegister, channel1ReadingType);
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

    private DataLoggerReference getDataLoggerReference(Register originRegister, ReadingType channelReadingType) {
        Device origin = mock(Device.class);
        when(originRegister.getReadingType()).thenReturn(channelReadingType);
        when(origin.getRegisters()).thenReturn(Collections.singletonList(originRegister));
        when(origin.getName()).thenReturn(myDeviceName);
        DataLoggerReference dataLoggerReference = mock(DataLoggerReference.class);
        when(dataLoggerReference.getOrigin()).thenReturn(origin);
        return dataLoggerReference;
    }

}