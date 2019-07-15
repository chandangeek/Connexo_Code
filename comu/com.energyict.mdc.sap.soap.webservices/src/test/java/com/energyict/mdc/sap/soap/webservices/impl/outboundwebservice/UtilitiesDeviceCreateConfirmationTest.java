/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class UtilitiesDeviceCreateConfirmationTest extends AbstractOutboundWebserviceTest {
    @Mock
    private UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage;

    @Before
    public void setUp() {
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceCreateConfirmationProvider provider = new UtilitiesDeviceCreateConfirmationProvider();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(confirmationMessage);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterCreateConfirmationCOut(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceCreateConfirmationProvider provider = new UtilitiesDeviceCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(confirmationMessage);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceCreateConfirmationProvider provider = new UtilitiesDeviceCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceCreateConfirmationProvider provider = new UtilitiesDeviceCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService.class);
    }
}
