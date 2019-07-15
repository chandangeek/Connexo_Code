/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterBulkCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegBulkCrteConfMsg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class UtilitiesDeviceRegisterBulkCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrRegBulkCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceRegisterCreateConfirmationMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getBulkConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceRegisterBulkCreateConfirmationProvider provider = new UtilitiesDeviceRegisterBulkCreateConfirmationProvider();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut(outboundMessage.getBulkConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceRegisterBulkCreateConfirmationProvider provider = new UtilitiesDeviceRegisterBulkCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceRegisterBulkCreateConfirmationProvider provider = new UtilitiesDeviceRegisterBulkCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceRegisterBulkCreateConfirmationProvider provider = new UtilitiesDeviceRegisterBulkCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService.class);
    }
}