package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.exceptions.ObisCodeParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 29/11/13
 * Time: 11:45
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterDataFactoryTest {

    private String READING_TYPE_CODE = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    @Mock
    private ReadingType readingType;

    @Test
    public void convertToIntervalBlocksTest() {
        when(readingType.getMRID()).thenReturn(READING_TYPE_CODE);
        TimeDuration interval = new TimeDuration(15, TimeDuration.TimeUnit.MINUTES);
        ChannelInfo channelInfo = new ChannelInfo(1, "1.0.1.8.0.255", Unit.get("kWh"), "meter", readingType);
        Date intervalDate = new Date(1385974800L);
        IntervalData intervalData = new IntervalData(intervalDate);
        IntervalValue intervalValue = new IntervalValue(new BigDecimal(123.456), 0, IntervalStateBits.OTHER);
        intervalData.setIntervalValues(Collections.singletonList(intervalValue));
        intervalData.setEiStatus(IntervalStateBits.BADTIME);
        CollectedLoadProfile collectedLoadProfile = mock(CollectedLoadProfile.class);
        when(collectedLoadProfile.getChannelInfo()).thenReturn(Arrays.asList(channelInfo));
        when(collectedLoadProfile.getCollectedIntervalData()).thenReturn(Arrays.asList(intervalData));

        List<IntervalBlock> intervalBlocks = MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile);

        assertThat(intervalBlocks).isNotNull();
        assertThat(intervalBlocks).hasSize(1);
        assertThat(intervalBlocks.get(0).getIntervals()).hasSize(1);
        assertThat(intervalBlocks.get(0).getIntervals().get(0).getValue()).isEqualTo((BigDecimal) intervalValue.getNumber());
        assertThat(intervalBlocks.get(0).getIntervals().get(0).getTimeStamp()).isEqualTo(intervalDate.toInstant());
        assertThat(intervalBlocks.get(0).getIntervals().get(0).getProfileStatus().getBits()).isEqualTo(IntervalStateBits.BADTIME | IntervalStateBits.OTHER); // ProfileStatus should be a combination of the general IntervalData status
        assertThat(intervalBlocks.get(0).getReadingTypeCode()).isEqualTo(READING_TYPE_CODE);                                                                 //and the status of the individual IntervalValue
    }

}
