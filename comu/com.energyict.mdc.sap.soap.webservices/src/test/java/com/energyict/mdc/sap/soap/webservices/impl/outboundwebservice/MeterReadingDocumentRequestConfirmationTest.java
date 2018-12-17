/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class MeterReadingDocumentRequestConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port;
    @Mock
    private SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage;
    @Mock
    private MeterReadingDocumentRequestConfirmationMessage outboundMessage;

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

        MeterReadingDocumentCreateConfirmationProvider provider = new MeterReadingDocumentCreateConfirmationProvider();
        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).smartMeterMeterReadingDocumentERPCreateConfirmationEOut(outboundMessage.getConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        MeterReadingDocumentCreateConfirmationProvider provider = new MeterReadingDocumentCreateConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        MeterReadingDocumentCreateConfirmationProvider provider = new MeterReadingDocumentCreateConfirmationProvider();
        Assert.assertEquals(provider.getService(), SmartMeterMeterReadingDocumentERPCreateConfirmationEOut.class);
    }

    @Test
    public void testGet() {
        MeterReadingDocumentCreateConfirmationProvider provider = new MeterReadingDocumentCreateConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService.class);
    }
}