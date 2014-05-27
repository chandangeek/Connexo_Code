package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceImpl;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;

import com.elster.jupiter.metering.readings.IntervalBlock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 29/11/13
 * Time: 11:45
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterDataFactoryTest {

    @Test
    public void convertToIntervalBlocksTest() {
        TimeDuration interval = new TimeDuration(15, TimeDuration.MINUTES);
        ChannelInfo channelInfo = new ChannelInfo(1, "1.0.1.8.0.255", Unit.get("kWh"));
        Date intervalDate = new Date(1385974800L);
        IntervalData intervalData = new IntervalData(intervalDate);
        BigDecimal intervalValue = new BigDecimal(123.456);
        intervalData.addValue(intervalValue);
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(Arrays.asList(channelInfo));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(Arrays.asList(intervalData));

        List<IntervalBlock> intervalBlocks = MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile, interval, new MdcReadingTypeUtilServiceImpl());

        assertThat(intervalBlocks).isNotNull();
        assertThat(intervalBlocks).hasSize(1);
        assertThat(intervalBlocks.get(0).getIntervals()).hasSize(1);
        assertThat(intervalBlocks.get(0).getIntervals().get(0).getValue()).isEqualTo(intervalValue);
        assertThat(intervalBlocks.get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalDate);
        assertThat(intervalBlocks.get(0).getReadingTypeCode()).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }

    @Test(expected = DeviceConfigurationException.class)
    public void channelInfoIsNotAnObisCode() {
        TimeDuration interval = new TimeDuration(15, TimeDuration.MINUTES);
        ChannelInfo channelInfo = new ChannelInfo(1, "NotAnObisCode", Unit.get("kWh"));
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(Arrays.asList(channelInfo));
        try {
            MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile, interval, new MdcReadingTypeUtilServiceImpl());
        } catch (DeviceConfigurationException e) {
            if (!e.getMessageId().equals("CSC-CONF-111")) {
                fail("Should have gotten an exception indicating that the channelInfo object does not contain an obiscode, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

}
