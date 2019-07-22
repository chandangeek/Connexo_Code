/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.eventmanagement.MeterEventCreateRequestProviderImpl;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequest;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequestService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetertEventCreateTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SOAUtilitiesSmartMeterEventERPBulkCreateRequest port;
    @Mock
    private UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg;
    @Mock
    private WebServiceActivator webServiceActivator;

    private MeterEventCreateRequestProviderImpl provider;

    @Before
    public void setUp() {
        provider = spy(new MeterEventCreateRequestProviderImpl());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        provider.addSOAUtilitiesSmartMeterEventERPBulkCreateRequest(port, ImmutableMap.of(AbstractOutboundEndPointProvider.ENDPOINT_CONFIGURATION_ID_PROPERTY, 1l));
        provider.send(reqMsg);
        verify(provider).using("soaUtilitiesSmartMeterEventERPBulkCreateRequest");
    }

    @Test
    public void testCallWithoutPort() {
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'CreateUtilitiesSmartMeterEvent'.");
        provider.send(reqMsg);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), SOAUtilitiesSmartMeterEventERPBulkCreateRequest.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), SOAUtilitiesSmartMeterEventERPBulkCreateRequestService.class);
    }
}
