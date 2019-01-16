/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.eventmanagement.MeterEventCreateRequestProviderImpl;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequest;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.SOAUtilitiesSmartMeterEventERPBulkCreateRequestService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class MetertEventCreateTest extends AbstractOutboundWebserviceTest {

    @Mock
    private SOAUtilitiesSmartMeterEventERPBulkCreateRequest port;
    @Mock
    private UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg;
    @Mock
    private WebServiceActivator webServiceActivator;

    @Test
    public void testCall() {
        MeterEventCreateRequestProviderImpl meterEventCreateRequestProvider = new MeterEventCreateRequestProviderImpl();
        meterEventCreateRequestProvider.addSOAUtilitiesSmartMeterEventERPBulkCreateRequest(port);
        meterEventCreateRequestProvider.send(reqMsg);
        Mockito.verify(port).soaUtilitiesSmartMeterEventERPBulkCreateRequest(reqMsg);
    }

    @Test
    public void testCallWithoutPort() {
        when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        MeterEventCreateRequestProviderImpl meterEventCreateRequestProvider = new MeterEventCreateRequestProviderImpl();
        meterEventCreateRequestProvider.setThesaurus(webServiceActivator);
        expectedException.expect(SAPWebServiceException.class);
        expectedException.expectMessage(MessageSeeds.NO_WEB_SERVICE_ENDPOINTS.getDefaultFormat());
        meterEventCreateRequestProvider.send(reqMsg);
    }

    @Test
    public void testGetService() {
        MeterEventCreateRequestProviderImpl meterEventCreateRequest = new MeterEventCreateRequestProviderImpl();
        Assert.assertEquals(meterEventCreateRequest.getService(), SOAUtilitiesSmartMeterEventERPBulkCreateRequest.class);
    }

    @Test
    public void testGet() {
        MeterEventCreateRequestProviderImpl meterEventCreateRequest = new MeterEventCreateRequestProviderImpl();
        Assert.assertEquals(meterEventCreateRequest.get().getClass(), SOAUtilitiesSmartMeterEventERPBulkCreateRequestService.class);
    }
}