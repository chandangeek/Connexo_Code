/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifMsg;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyObject;
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

    @Before
    public void setUp() {
        deviceId = "100000000524205";
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceRegisteredNotificationProvider provider = getTestInstance();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(deviceId);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterRegisteredNotificationCOut(anyObject());
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceRegisteredNotificationProvider provider = getTestInstance();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(deviceId);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceRegisteredNotificationProvider provider = getTestInstance();
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceRegisteredNotificationProvider provider = getTestInstance();
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService.class);
    }

    private UtilitiesDeviceRegisteredNotificationProvider getTestInstance() {
        UtilitiesDeviceRegisteredNotificationProvider provider =
                new UtilitiesDeviceRegisteredNotificationProvider(clock, sapCustomPropertySets, meteringService,
                        endPointConfigurationService, deviceService);
        return provider;
    }
}