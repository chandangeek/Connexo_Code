/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendMeterReadingsProviderImplTest extends SendMeterReadingsTest {

    @Mock
    private MeterReadingsPort meterReadingsPort;
    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord dailyReading;
    @Mock
    private ReadingInfo readingInfo;
    @Mock
    EndPointConfiguration endPointConfiguration;

    private void mockIntervalReadings() {
        mockIntervalReading(dailyReading, Range.openClosed(JAN_1ST.minusDays(1).toInstant(), JAN_1ST.toInstant()), 1.05);
    }

    @Test
    public void testCallWithReadingInfos() throws FaultMessage {
        when(clock.instant()).thenReturn(JAN_1ST.toInstant());
        mockReadingsInfoType(readingInfo, dailyReadingType, dailyReading);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfo.add(readingInfo);
        when(readingStorer.getReadings()).thenReturn(listReadingInfo);
        mockIntervalReadings();

        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl();
        Map<String, Object> properties = ImmutableMap.of("url", "some_url");
        //provider.addMeterReadingsPort(meterReadingsPort, properties);

        provider.call(listReadingInfo, HeaderType.Verb.CREATED);

        verify(meterReadingsPort).createdMeterReadings(Mockito.any(MeterReadingsEventMessageType.class));
    }

    @Test
    public void testCallWithMeterReadings() throws FaultMessage {
        when(clock.instant()).thenReturn(JAN_1ST.toInstant());
        mockReadingsInfoType(readingInfo, dailyReadingType, dailyReading);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfo.add(readingInfo);
        when(readingStorer.getReadings()).thenReturn(listReadingInfo);
        mockIntervalReadings();
        MeterReadingsBuilder builder = new MeterReadingsBuilder();
        MeterReadings meterReadings = builder.build(listReadingInfo);

        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl();
        Map<String, Object> properties = ImmutableMap.of("url", "some_url");
        MeterReadingsResponseMessageType meterReadingsResponseMessageType = new MeterReadingsResponseMessageType();
        ReplyType replyType = new ReplyType();
        replyType.setResult(ReplyType.Result.OK);
        meterReadingsResponseMessageType.setReply(replyType);
        when(meterReadingsPort.createdMeterReadings(anyObject()))
                .thenReturn(meterReadingsResponseMessageType);
        //provider.addMeterReadingsPort(meterReadingsPort, properties);

        when(endPointConfiguration.getUrl()).thenReturn("some_url");
        assertTrue(provider.call(meterReadings, HeaderType.Verb.CREATED, endPointConfiguration));
        verify(meterReadingsPort).createdMeterReadings(Mockito.any(MeterReadingsEventMessageType.class));
    }

    @Test
    public void testGetService() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl();
        Assert.assertEquals(provider.getService(), MeterReadingsPort.class);
    }

    @Test
    public void testGet() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl();
        Assert.assertEquals(provider.get().getClass(), ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings.class);
    }
}