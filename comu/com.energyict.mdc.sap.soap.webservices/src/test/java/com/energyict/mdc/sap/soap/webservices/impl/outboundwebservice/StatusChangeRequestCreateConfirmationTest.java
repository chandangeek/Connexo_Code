/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusChangeRequestCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut port;
    @Mock
    private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg confirmationMessage;
    @Mock
    private StatusChangeRequestCreateConfirmationMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        provider.addSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(outboundMessage.getConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut.class);
    }

    @Test
    public void testGet() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOutService.class);
    }
}
