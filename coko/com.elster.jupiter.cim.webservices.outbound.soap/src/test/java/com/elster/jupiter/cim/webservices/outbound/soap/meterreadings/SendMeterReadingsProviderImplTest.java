/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsEventMessageType;
import ch.iec.tc57._2011.meterreadingsmessage.MeterReadingsResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterreadings.FaultMessage;
import ch.iec.tc57._2011.sendmeterreadings.MeterReadingsPort;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
    @Mock
    private  EndPointConfigurationService endPointConfigurationService;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private OutboundEndPointProvider.RequestSender requestSender;
    @Mock
    private MeterReadingsResponseMessageType response;
    private SendMeterReadingsProviderImpl provider;

    private void mockIntervalReadings() {
        mockIntervalReading(dailyReading, Range.openClosed(JAN_1ST.minusDays(1).toInstant(), JAN_1ST.toInstant()), 1.05);
    }

    @Before
    public void setup() {
        provider = spy(new SendMeterReadingsProviderImpl());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(mock(NlsMessageFormat.class));
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", thesaurus);
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(mock(NlsMessageFormat.class));
        Map<String, Object> properties = ImmutableMap.of("url", "some_url", "epcId", 1l);
        provider.addMeterReadingsPort(meterReadingsPort, properties);
        when(provider.using(anyString())).thenReturn(requestSender);
        when(requestSender.toEndpoints(endPointConfiguration)).thenReturn(requestSender);
        Map responseMap = new HashMap();
        responseMap.put(endPointConfiguration, response);
        when(requestSender.withRelatedObject(any())).thenReturn(requestSender);
        when(requestSender.send(any())).thenReturn(responseMap);
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
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Arrays.asList(endPointConfiguration));

        provider.call(listReadingInfo, HeaderType.Verb.CREATED);

        SetMultimap<String, String> values = HashMultimap.create();
        listReadingInfo.forEach(reading->{
            reading.getMeter().ifPresent(meter->{
                values.put(WebServiceRequestAttributesNames.CIM_DEVICE_NAME.getAttributeName(),
                        meter.getName());
                values.put(WebServiceRequestAttributesNames.CIM_DEVICE_MR_ID.getAttributeName(),
                        meter.getMRID());
            });
            reading.getUsagePoint().ifPresent(usp->{
                values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_NAME.getAttributeName(),
                        usp.getName());
                values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_MR_ID.getAttributeName(),
                        usp.getMRID());
            });
        });

        verify(provider).using("createdMeterReadings");
        verify(requestSender).withRelatedObject(values);
        verify(requestSender).send(any(MeterReadingsEventMessageType.class));
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

        MeterReadingsResponseMessageType meterReadingsResponseMessageType = new MeterReadingsResponseMessageType();
        ReplyType replyType = new ReplyType();
        replyType.setResult(ReplyType.Result.OK);
        meterReadingsResponseMessageType.setReply(replyType);
        when(meterReadingsPort.createdMeterReadings(anyObject()))
                .thenReturn(meterReadingsResponseMessageType);

        when(endPointConfiguration.getUrl()).thenReturn("some_url");
        ReplyType reply = mock(ReplyType.class);
        when(response.getReply()).thenReturn(reply);
        when(reply.getResult()).thenReturn(ReplyType.Result.OK);
        assertTrue(provider.call(meterReadings, getHeader(HeaderType.Verb.CREATED), endPointConfiguration));
        SetMultimap<String, String> values = HashMultimap.create();
        meterReadings.getMeterReading().forEach(reading->{
            Optional.ofNullable(reading.getMeter()).ifPresent(meter->{
                values.put(WebServiceRequestAttributesNames.CIM_DEVICE_NAME.getAttributeName(),
                        meter.getNames().get(0).getName());
                values.put(WebServiceRequestAttributesNames.CIM_DEVICE_MR_ID.getAttributeName(),
                        meter.getMRID());
            });

            Optional.ofNullable(reading.getUsagePoint()).ifPresent(usp->{
                values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_NAME.getAttributeName(),
                        usp.getNames().get(0).getName());
                values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_MR_ID.getAttributeName(),
                        usp.getMRID());
            });
        });
        verify(provider).using("createdMeterReadings");
        verify(requestSender).toEndpoints(endPointConfiguration);
        verify(requestSender).withRelatedObject(values);
        verify(requestSender).send(any(MeterReadingsEventMessageType.class));
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

    private HeaderType getHeader(HeaderType.Verb requestVerb) {
        HeaderType header = new HeaderType();
        header.setVerb(requestVerb);
        header.setNoun("MeterReadings");
        return header;
    }

    private static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}