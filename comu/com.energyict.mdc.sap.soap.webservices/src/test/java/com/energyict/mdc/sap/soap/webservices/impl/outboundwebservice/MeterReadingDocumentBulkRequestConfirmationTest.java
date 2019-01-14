/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentBulkRequestConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmrtMtrMtrRdngDocERPBulkCrteConfMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class MeterReadingDocumentBulkRequestConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut port;
    @Mock
    private SmrtMtrMtrRdngDocERPBulkCrteConfMsg confirmationMessage;
    @Mock
    private MeterReadingDocumentRequestConfirmationMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getBulkConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        MeterReadingDocumentBulkRequestConfirmationProvider provider = new MeterReadingDocumentBulkRequestConfirmationProvider();
        provider.addConfirmationPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).smartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut(outboundMessage.getBulkConfirmationMessage());
    }

    @Test
    public void testCallWithoutPort() {
        MeterReadingDocumentBulkRequestConfirmationProvider provider = new MeterReadingDocumentBulkRequestConfirmationProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        MeterReadingDocumentBulkRequestConfirmationProvider provider = new MeterReadingDocumentBulkRequestConfirmationProvider();
        Assert.assertEquals(provider.getService(), SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut.class);
    }

    @Test
    public void testGet() {
        MeterReadingDocumentBulkRequestConfirmationProvider provider = new MeterReadingDocumentBulkRequestConfirmationProvider();
        Assert.assertEquals(provider.get().getClass(), SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOutService.class);
    }
}