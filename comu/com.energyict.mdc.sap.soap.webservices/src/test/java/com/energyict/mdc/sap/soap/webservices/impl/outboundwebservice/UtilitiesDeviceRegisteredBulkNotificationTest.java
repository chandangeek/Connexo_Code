/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredBulkNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedBulkNotifMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UtilitiesDeviceRegisteredBulkNotificationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port;
    @Mock
    private Clock clock;

    private List<String> deviceIds;
    UtilitiesDeviceRegisteredBulkNotificationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new UtilitiesDeviceRegisteredBulkNotificationProvider(clock));
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(requestSender.withRelatedAttributes(any(SetMultimap.class))).thenReturn(requestSender);
        deviceIds = Arrays.asList("100000000524205", "100000000524206", "100000000524207");
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(deviceIds, "123");

        SetMultimap<String,String> values = HashMultimap.create();

        deviceIds.forEach(deviceId->{
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                    deviceId);
        });

        verify(provider).using("utilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut");
        verify(requestSender).send(any(UtilsDvceERPSmrtMtrRegedBulkNotifMsg.class));
        verify(requestSender).withRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP UtilitiesDeviceERPSmartMeterRegisteredBulkNotification_C_Out'.");

        provider.call(deviceIds, "123");
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService.class);
    }
}