/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ReadingInfoType;
import com.elster.jupiter.nls.NlsService;

import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import com.google.common.collect.Range;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendMeterReadingsProviderImplTest extends SendMeterReadingsTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private MeterReadingsPort meterReadingsPort;
    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord dailyReading;
    @Mock
    private ReadingInfoType readingInfoType;

    private void mockIntervalReadings() {
        mockIntervalReading(dailyReading, Range.openClosed(JAN_1ST.minusDays(1).toInstant(), JAN_1ST.toInstant()), 1.05);
    }

    @Test
    public void testCall() throws FaultMessage {
        when(clock.instant()).thenReturn(JAN_1ST.toInstant());
        mockReadingsInfoType(readingInfoType, dailyReadingType, dailyReading);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfoType.add(readingInfoType);
        when(readingStorer.getReadings()).thenReturn(listReadingInfoType);
        mockIntervalReadings();

        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        provider.addMeterReadingsPortService(meterReadingsPort);

        provider.call(readingStorer, true);

        Mockito.verify(meterReadingsPort).createdMeterReadings(Mockito.any(MeterReadingsEventMessageType.class));
    }

    @Test
    public void testSendEventWithoutReadings() throws FaultMessage {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);

        expectedException.expect(MeterReadinsServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_READINGS_IN_EVENT.getDefaultFormat());

        provider.call(readingStorer, true);
    }

    @Test
    public void testSendWithoutPort() {
        when(clock.instant()).thenReturn(JAN_1ST.toInstant());
        mockReadingsInfoType(readingInfoType, dailyReadingType, dailyReading);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfoType.add(readingInfoType);
        when(readingStorer.getReadings()).thenReturn(listReadingInfoType);
        mockIntervalReadings();

        SendMeterReadingsProvider provider = new SendMeterReadingsProviderImpl(nlsService);

        expectedException.expect(MeterReadinsServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(readingStorer, true);
    }

    @Test
    public void testGetService() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        Assert.assertEquals(provider.getService(), MeterReadingsPort.class);
    }

    @Test
    public void testGet() {
        SendMeterReadingsProviderImpl provider = new SendMeterReadingsProviderImpl(nlsService);
        Assert.assertEquals(provider.get().getClass(), ch.iec.tc57._2011.sendmeterreadings.SendMeterReadings.class);
    }
}