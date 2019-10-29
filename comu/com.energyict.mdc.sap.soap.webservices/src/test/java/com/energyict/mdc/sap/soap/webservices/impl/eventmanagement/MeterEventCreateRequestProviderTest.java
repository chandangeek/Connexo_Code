/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventERPBulkCreateRequestCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import com.google.common.collect.ImmutableSetMultimap;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class MeterEventCreateRequestProviderTest extends AbstractOutboundWebserviceTest<UtilitiesSmartMeterEventERPBulkCreateRequestCOut> {
    private UtilsSmrtMtrEvtERPBulkCrteReqMsg message;
    private MeterEventCreateRequestProviderImpl service;

    @Before
    public void setUp() {
        service = getInstance(MeterEventCreateRequestProviderImpl.class);
    }

    @Test
    public void testGetters() {
        assertThat(service.getName()).isEqualTo(MeterEventCreateRequestProvider.SAP_CREATE_UTILITIES_SMART_METER_EVENT);
        assertThat(service.getApplication()).isEqualTo("MultiSense");
        assertThat(service.getService()).isSameAs(UtilitiesSmartMeterEventERPBulkCreateRequestCOut.class);
        assertThat(service.get()).isInstanceOf(UtilitiesSmartMeterEventERPBulkCreateRequestCOutService.class);
    }

    @Test
    public void testSending() {
        message = new UtilsSmrtMtrEvtERPBulkCrteReqMsg();

        service.send(message);

        verify(endpoint).utilitiesSmartMeterEventERPBulkCreateRequestCOut(message);
        verify(webServiceCallOccurrence).saveRelatedAttributes(ImmutableSetMultimap.of());
    }

    @Test
    public void testSendingWithDeviceId() {
        message = new UtilsSmrtMtrEvtERPBulkCrteReqMsg();
        UtilsSmrtMtrEvtERPCrteReqMsg item = new UtilsSmrtMtrEvtERPCrteReqMsg();
        UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt event = new UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt();
        UtilitiesDeviceID device = new UtilitiesDeviceID();
        device.setValue("00666");
        event.setUtilitiesDeviceID(device);
        item.setUtilitiesSmartMeterEvent(event);
        message.getUtilitiesSmartMeterEventERPCreateRequestMessage().add(item);

        service.send(message);

        verify(endpoint).utilitiesSmartMeterEventERPBulkCreateRequestCOut(message);
        verify(webServiceCallOccurrence).saveRelatedAttributes(ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), "00666"));
    }
}
