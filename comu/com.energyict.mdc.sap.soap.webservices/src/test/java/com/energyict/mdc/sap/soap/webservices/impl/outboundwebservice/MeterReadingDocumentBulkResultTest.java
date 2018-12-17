/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentBulkResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltBulkCrteReqMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class MeterReadingDocumentBulkResultTest extends AbstractOutboundWebserviceTest {

    @Mock
    private MeterReadingDocumentERPResultBulkCreateRequestEOut port;
    @Mock
    private MtrRdngDocERPRsltBulkCrteReqMsg resultMessage;
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getBulkResultMessage()).thenReturn(resultMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        MeterReadingDocumentBulkResultCreateRequestProvider provider = new MeterReadingDocumentBulkResultCreateRequestProvider();
        provider.addBulkResultsPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).meterReadingDocumentERPResultBulkCreateRequestEOut(outboundMessage.getBulkResultMessage());
    }

    @Test
    public void testCallWithoutPort() {
        MeterReadingDocumentBulkResultCreateRequestProvider provider = new MeterReadingDocumentBulkResultCreateRequestProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        MeterReadingDocumentBulkResultCreateRequestProvider provider = new MeterReadingDocumentBulkResultCreateRequestProvider();
        Assert.assertEquals(provider.getService(), MeterReadingDocumentERPResultBulkCreateRequestEOut.class);
    }

    @Test
    public void testGet() {
        MeterReadingDocumentBulkResultCreateRequestProvider provider = new MeterReadingDocumentBulkResultCreateRequestProvider();
        Assert.assertEquals(provider.get().getClass(), MeterReadingDocumentERPResultBulkCreateRequestEOutService.class);
    }
}