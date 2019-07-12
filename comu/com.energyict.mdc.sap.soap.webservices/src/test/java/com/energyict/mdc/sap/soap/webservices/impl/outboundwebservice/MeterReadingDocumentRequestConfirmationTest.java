/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentRequestConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port;
    @Mock
    private SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage;
    @Mock
    private MeterReadingDocumentRequestConfirmationMessage outboundMessage;

    private MeterReadingDocumentCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new MeterReadingDocumentCreateConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        verify(provider).using("smartMeterMeterReadingDocumentERPCreateConfirmationEOut");
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SapMeterReadingRequestConfirmation'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), SmartMeterMeterReadingDocumentERPCreateConfirmationEOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService.class);
    }
}
