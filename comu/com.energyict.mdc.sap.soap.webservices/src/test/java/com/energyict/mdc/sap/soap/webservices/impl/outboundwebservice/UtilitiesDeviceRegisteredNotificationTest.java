/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
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

public class UtilitiesDeviceRegisteredNotificationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port;
    @Mock
    private Clock clock;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private DeviceService deviceService;

    private String deviceId;
    UtilitiesDeviceRegisteredNotificationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new UtilitiesDeviceRegisteredNotificationProvider(clock, sapCustomPropertySets, meteringService, endPointConfigurationService, deviceService));
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(requestSender.withRelatedAttributes(any(SetMultimap.class))).thenReturn(requestSender);
        deviceId = "100000000524205";
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(deviceId);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                deviceId);

        verify(provider).using("utilitiesDeviceERPSmartMeterRegisteredNotificationCOut");
        verify(requestSender).send(any(UtilsDvceERPSmrtMtrRegedNotifMsg.class));
        verify(requestSender).withRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP UtilitiesDeviceERPSmartMeterRegisteredNotification_C_Out'.");


        provider.call(deviceId);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService.class);
    }
}