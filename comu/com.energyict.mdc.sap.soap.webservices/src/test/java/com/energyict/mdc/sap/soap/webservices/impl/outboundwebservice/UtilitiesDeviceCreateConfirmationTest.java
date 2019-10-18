/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

public class UtilitiesDeviceCreateConfirmationTest extends AbstractOutboundWebserviceTest {
    @Mock
    private UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceCreateConfirmationMessage outboundMessage;

    private UtilitiesDeviceCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new UtilitiesDeviceCreateConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(requestSender.withRelatedAttributes(any(SetMultimap.class))).thenReturn(requestSender);
        when(outboundMessage.getConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        when(confirmationMessage.getUtilitiesDevice().getID().getValue()).thenReturn("UtilDeviceID");
    }

    @Test
    public void testCall() {
        when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
        properties.put(WebServiceActivator.URL_PROPERTY, getURL());
        properties.put("epcId", 1l);

        provider.addRequestConfirmationPort(port, properties);
        provider.call(outboundMessage);

        verify(provider).using("utilitiesDeviceERPSmartMeterCreateConfirmationCOut");
        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                "UtilDeviceID");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP UtilitiesDeviceERPSmartMeterCreateConfirmation_C_Out'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService.class);
    }
}
