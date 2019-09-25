/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegBulkChgConfMsg;

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

public class MeterRegisterBulkChangeConfirmationTest extends AbstractOutboundWebserviceTest {
    @Mock
    private UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut port;
    @Mock
    private UtilsDvceERPSmrtMtrRegBulkChgConfMsg resultMessage;
    @Mock
    private MeterRegisterBulkChangeConfirmationMessage outboundMessage;

    private MeterRegisterBulkChangeConfirmationProvider provider;

    @Before
    public void setUp() {
        provider = spy(new MeterRegisterBulkChangeConfirmationProvider());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        when(outboundMessage.getConfirmationMessage()).thenReturn(resultMessage);
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

        verify(provider).using("utilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut");
        verify(requestSender).send(resultMessage);
    }

    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        provider.setWebServiceActivator(webServiceActivator);

        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP MeterRegisterBulkChangeConfirmation'.");

        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService.class);
    }}
