/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceBulkCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class UtilitiesDeviceBulkCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceCreateConfirmationMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        UtilitiesDeviceBulkCreateConfirmationProvider provider = new UtilitiesDeviceBulkCreateConfirmationProvider();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).utilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut(outboundMessage.getConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        UtilitiesDeviceBulkCreateConfirmationProvider provider = new UtilitiesDeviceBulkCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        UtilitiesDeviceBulkCreateConfirmationProvider provider = new UtilitiesDeviceBulkCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        UtilitiesDeviceBulkCreateConfirmationProvider provider = new UtilitiesDeviceBulkCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService.class);
    }
}