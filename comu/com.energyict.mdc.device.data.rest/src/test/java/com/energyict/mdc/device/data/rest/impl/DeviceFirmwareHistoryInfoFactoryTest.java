/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.DeviceFirmwareVersionHistoryRecord;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceFirmwareHistoryInfoFactoryTest {

    @Mock
    private DeviceFirmwareHistory deviceFirmwareHistory;
    @Mock
    private DeviceFirmwareVersionHistoryRecord record1, record2, record3, record4, record5;
    @Mock
    private FirmwareVersion version1, version2, version3, version4, version5;
    @Mock
    private Thesaurus thesaurus;

    private Instant twoMonthsAgo, oneMonthAgo, oneWeekAgo, nextMonth;

    @Before
    public void prepareMokskes(){
        LocalDateTime now = LocalDateTime.now();
        twoMonthsAgo = Instant.ofEpochSecond(now.minus(2, ChronoUnit.MONTHS).toEpochSecond(ZoneOffset.UTC));
        oneMonthAgo = Instant.ofEpochSecond(now.minus(1, ChronoUnit.MONTHS).toEpochSecond(ZoneOffset.UTC));
        oneWeekAgo = Instant.ofEpochSecond(now.minus(1, ChronoUnit.WEEKS).toEpochSecond(ZoneOffset.UTC));
        nextMonth = Instant.ofEpochSecond(now.plus(1, ChronoUnit.MONTHS).toEpochSecond(ZoneOffset.UTC));;

        when(version1.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(version2.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(version3.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(version4.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(version5.getFirmwareType()).thenReturn(FirmwareType.METER);

        when(version1.getFirmwareVersion()).thenReturn("firmware version 1");
        when(version2.getFirmwareVersion()).thenReturn("firmware version 2");
        when(version3.getFirmwareVersion()).thenReturn("firmware version 3");
        when(version4.getFirmwareVersion()).thenReturn("firmware version 4");
        when(version5.getFirmwareVersion()).thenReturn("firmware version 5");

        when(record1.getFirmwareVersion()).thenReturn(version1);
        when(record2.getFirmwareVersion()).thenReturn(version2);
        when(record3.getFirmwareVersion()).thenReturn(version3);
        when(record4.getFirmwareVersion()).thenReturn(version4);
        when(record5.getFirmwareVersion()).thenReturn(version5);

        when(record1.getInterval()).thenReturn(Interval.of(twoMonthsAgo, oneMonthAgo));
        when(record2.getInterval()).thenReturn(Interval.of(twoMonthsAgo, oneMonthAgo));
        when(record3.getInterval()).thenReturn(Interval.startAt(oneMonthAgo));
        when(record4.getInterval()).thenReturn(Interval.startAt(oneWeekAgo));
        when(record5.getInterval()).thenReturn(Interval.startAt(nextMonth));

        when(deviceFirmwareHistory.history()).thenReturn(Arrays.asList(record1, record2, record3, record4, record5));

        when(thesaurus.getString(FirmwareType.METER.getType(), FirmwareType.METER.getDescription())).thenReturn(FirmwareType.METER.getDescription());
        when(thesaurus.getString(FirmwareType.COMMUNICATION.getType(), FirmwareType.COMMUNICATION.getDescription())).thenReturn(FirmwareType.COMMUNICATION.getDescription());
    }
    @Test
    public void testTotal(){
        DeviceFirmwareHistoryInfoFactory.DeviceFirmwareHistoryInfos infos = new DeviceFirmwareHistoryInfoFactory.DeviceFirmwareHistoryInfos(deviceFirmwareHistory, thesaurus);
        assertThat(infos.total).isEqualTo(5);
        assertThat(infos.deviceFirmwareHistoryInfos).hasSize(5);
    }
    @Test
    public void testDeviceFirmwareHistoryInfos(){
        DeviceFirmwareHistoryInfoFactory.DeviceFirmwareHistoryInfos infos = new DeviceFirmwareHistoryInfoFactory.DeviceFirmwareHistoryInfos(deviceFirmwareHistory, thesaurus);
        List<DeviceFirmwareHistoryInfoFactory.DeviceFirmwareHistoryInfo> info = infos.deviceFirmwareHistoryInfos;

        assertThat(info.get(0).activationDate).isEqualTo(twoMonthsAgo);
        assertThat(info.get(1).activationDate).isEqualTo(twoMonthsAgo);
        assertThat(info.get(2).activationDate).isEqualTo(oneMonthAgo);
        assertThat(info.get(3).activationDate).isEqualTo(oneWeekAgo);
        assertThat(info.get(4).activationDate).isEqualTo(nextMonth);

        assertThat(info.get(0).firmwareVersion).isEqualTo("firmware version 1");
        assertThat(info.get(1).firmwareVersion).isEqualTo("firmware version 2");
        assertThat(info.get(2).firmwareVersion).isEqualTo("firmware version 3");
        assertThat(info.get(3).firmwareVersion).isEqualTo("firmware version 4");
        assertThat(info.get(4).firmwareVersion).isEqualTo("firmware version 5");

        assertThat(info.get(0).firmwareType).isEqualTo(FirmwareType.METER.getDescription());
        assertThat(info.get(1).firmwareType).isEqualTo(FirmwareType.COMMUNICATION.getDescription());
        assertThat(info.get(2).firmwareType).isEqualTo(FirmwareType.METER.getDescription());
        assertThat(info.get(3).firmwareType).isEqualTo(FirmwareType.COMMUNICATION.getDescription());
        assertThat(info.get(4).firmwareType).isEqualTo(FirmwareType.METER.getDescription());
    }


}

