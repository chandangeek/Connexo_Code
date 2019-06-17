/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMsg;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

public class MeterReadingDocumentResultTest extends AbstractOutboundWebserviceTest {

    @Mock
    private MeterReadingDocumentERPResultCreateRequestCOut port;
    @Mock
    private MtrRdngDocERPRsltCrteReqMsg resultMessage;
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;

    @Before
    public void setUp() {
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getResultMessage()).thenReturn(resultMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());

        MeterReadingDocumentResultCreateRequestProvider provider = new MeterReadingDocumentResultCreateRequestProvider();
        provider.addResultPort(port, properties);
        provider.call(outboundMessage);

        Mockito.verify(port).meterReadingDocumentERPResultCreateRequestCOut(outboundMessage.getResultMessage());
    }

    @Test
    public void testCallWithoutPort() {
        MeterReadingDocumentResultCreateRequestProvider provider = new MeterReadingDocumentResultCreateRequestProvider();
        provider.setThesaurus(webServiceActivator);

        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        MeterReadingDocumentResultCreateRequestProvider provider = new MeterReadingDocumentResultCreateRequestProvider();
        Assert.assertEquals(provider.getService(), MeterReadingDocumentERPResultCreateRequestCOut.class);
    }

    @Test
    public void testGet() {
        MeterReadingDocumentResultCreateRequestProvider provider = new MeterReadingDocumentResultCreateRequestProvider();
        Assert.assertEquals(provider.get().getClass(), MeterReadingDocumentERPResultCreateRequestCOutService.class);
    }
}