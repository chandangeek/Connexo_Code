/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusChangeRequestCreateConfirmationTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut port;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg confirmationMessage;
    @Mock
    private StatusChangeRequestCreateConfirmationMessage outboundMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts connectionStatus;


    private StatusChangeRequestCreateConfirmationProvider provider;
    private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts> deviceConnectionStatuses = new ArrayList<>();


    @Before
    public void setUp() {
        deviceConnectionStatuses.add(connectionStatus);
        provider = spy(new StatusChangeRequestCreateConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        when(requestSender.withRelatedAttributes(any(SetMultimap.class))).thenReturn(requestSender);

        when(confirmationMessage.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()).thenReturn(deviceConnectionStatuses);
        when(connectionStatus.getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceID");

    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationEOut(port, properties);
        provider.call(outboundMessage);

        SetMultimap<String,String> values = HashMultimap.create();

        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                    "UtilDeviceID");

        verify(provider).using("smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut");
        verify(requestSender).send(confirmationMessage);
        verify(requestSender).withRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SapStatusChangeRequestCreateConfirmation'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService.class);
    }
}