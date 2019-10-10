/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentResultTest extends AbstractOutboundWebserviceTest {

    @Mock
    private MeterReadingDocumentERPResultCreateRequestCOut port;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MtrRdngDocERPRsltCrteReqMsg resultMessage;
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;

    private MeterReadingDocumentResultCreateRequestProvider provider;

    @Before
    public void setUp() {
        provider = spy(new MeterReadingDocumentResultCreateRequestProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getResultMessage()).thenReturn(resultMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        when(requestSender.withRelatedObject(any(SetMultimap.class))).thenReturn(requestSender);
        when(resultMessage.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue()).thenReturn("UtilMeasurmentTaskID");
        when(resultMessage.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceID");
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addResultPort(port, properties);
        provider.call(outboundMessage);

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), "UtilMeasurmentTaskID");
        values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),"UtilDeviceID");

        verify(provider).using("meterReadingDocumentERPResultCreateRequestCOut");
        verify(requestSender).withRelatedObject(values);
        verify(requestSender).send(resultMessage);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        provider.setWebServiceActivator(webServiceActivator);

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SapMeterReadingResult'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), MeterReadingDocumentERPResultCreateRequestCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), MeterReadingDocumentERPResultCreateRequestCOutService.class);
    }
}