/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredBulkNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedBulkNotifMsg;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class UtilitiesDeviceRegisteredBulkNotificationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrRegedBulkNotifMsg notificationMsg;
    @Mock
    private Clock clock;

    private List<String> deviceIds;

    @Before
    public void setUp() {
        deviceIds = Arrays.asList("100000000524205", "100000000524206", "100000000524207");
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceRegisteredBulkNotificationProvider provider = new UtilitiesDeviceRegisteredBulkNotificationProvider(clock);
        provider.addRequestConfirmationPort(port, properties);
        provider.call(deviceIds);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut(anyObject());
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceRegisteredBulkNotificationProvider provider = new UtilitiesDeviceRegisteredBulkNotificationProvider(clock);
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(deviceIds);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceRegisteredBulkNotificationProvider provider = new UtilitiesDeviceRegisteredBulkNotificationProvider(clock);
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceRegisteredBulkNotificationProvider provider = new UtilitiesDeviceRegisteredBulkNotificationProvider(clock);
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService.class);
    }
}