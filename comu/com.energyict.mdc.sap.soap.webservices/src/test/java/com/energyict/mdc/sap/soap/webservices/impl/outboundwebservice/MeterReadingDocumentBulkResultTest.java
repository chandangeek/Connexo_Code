/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentBulkResultCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateResultMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentBulkResultTest extends AbstractOutboundWebserviceTest {

    @Mock
    private MeterReadingDocumentERPResultBulkCreateRequestCOut port;
    @Mock
    private MtrRdngDocERPRsltBulkCrteReqMsg resultMessage;
    @Mock
    private MeterReadingDocumentCreateResultMessage outboundMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MtrRdngDocERPRsltCrteReqMsg crteReqMsg;

    private MeterReadingDocumentBulkResultCreateRequestProvider provider;
    private List<MtrRdngDocERPRsltCrteReqMsg> meterReadingDocumentERPResultCreateRequestMessage = new ArrayList<>();

    @Before
    public void setUp() {
        meterReadingDocumentERPResultCreateRequestMessage.add(crteReqMsg);
        provider = spy(new MeterReadingDocumentBulkResultCreateRequestProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(requestSender.withRelatedObject(any(SetMultimap.class))).thenReturn(requestSender);
        when(outboundMessage.getUrl()).thenReturn(getURL());
        when(outboundMessage.getBulkResultMessage()).thenReturn(resultMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        when(resultMessage.getMeterReadingDocumentERPResultCreateRequestMessage()).thenReturn(meterReadingDocumentERPResultCreateRequestMessage);
        when(crteReqMsg.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue()).thenReturn("UtilMesurmentTaskId");
        when(crteReqMsg.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceId");
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addBulkResultsPort(port, properties);
        provider.call(outboundMessage);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                "UtilMesurmentTaskId");
        values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                "UtilDeviceId");

        verify(provider).using("meterReadingDocumentERPResultBulkCreateRequestCOut");
        verify(requestSender).send(resultMessage);
        verify(requestSender).withRelatedObject(values);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SapMeterReadingBulkResult'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), MeterReadingDocumentERPResultBulkCreateRequestCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), MeterReadingDocumentERPResultBulkCreateRequestCOutService.class);
    }
}