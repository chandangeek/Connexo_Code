/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterReadingDocumentRequestConfirmationTest extends AbstractOutboundWebserviceTest<SmartMeterMeterReadingDocumentERPCreateConfirmationCOut> {
    private SmrtMtrMtrRdngDocERPCrteConfMsg confirmationMessage = new SmrtMtrMtrRdngDocERPCrteConfMsg();
    @Mock
    private MeterReadingDocumentRequestConfirmationMessage outboundMessage;

    private MeterReadingDocumentCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);

        provider = getProviderInstance(MeterReadingDocumentCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).smartMeterMeterReadingDocumentERPCreateConfirmationCOut(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SapMeterReadingRequestConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(SmartMeterMeterReadingDocumentERPCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(SmartMeterMeterReadingDocumentERPCreateConfirmationCOutService.class);
    }
}
