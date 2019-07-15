/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class UtilitiesDeviceRegisterCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceRegisterCreateConfirmationMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceRegisterCreateConfirmationProvider provider = new UtilitiesDeviceRegisterCreateConfirmationProvider();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut(outboundMessage.getConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceRegisterCreateConfirmationProvider provider = new UtilitiesDeviceRegisterCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceRegisterCreateConfirmationProvider provider = new UtilitiesDeviceRegisterCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceRegisterCreateConfirmationProvider provider = new UtilitiesDeviceRegisterCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisterCreateConfirmationCOutService.class);
    }
}