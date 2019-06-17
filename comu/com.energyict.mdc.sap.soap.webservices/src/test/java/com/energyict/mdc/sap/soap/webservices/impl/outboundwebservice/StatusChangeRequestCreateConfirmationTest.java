/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusChangeRequestCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port;
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

        Mockito.verify(port).smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut(outboundMessage.getConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        StatusChangeRequestCreateConfirmationProvider provider = new StatusChangeRequestCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService.class);
    }
}