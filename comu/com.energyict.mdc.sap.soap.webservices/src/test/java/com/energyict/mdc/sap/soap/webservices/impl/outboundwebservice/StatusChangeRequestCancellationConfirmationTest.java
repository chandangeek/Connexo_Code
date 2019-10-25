/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation.StatusChangeRequestCancellationConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusChangeRequestCancellationConfirmationTest extends AbstractOutboundWebserviceTest {
    @Mock
    private SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut port;
    @Mock
    private SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmationMessage;

    private StatusChangeRequestCancellationConfirmationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new StatusChangeRequestCancellationConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(confirmationMessage);

        verify(provider).using("smartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut");
        verify(requestSender).send(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP StatusChangeRequestCancellationConfirmation'.");

        provider.call(confirmationMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService.class);
    }
}
