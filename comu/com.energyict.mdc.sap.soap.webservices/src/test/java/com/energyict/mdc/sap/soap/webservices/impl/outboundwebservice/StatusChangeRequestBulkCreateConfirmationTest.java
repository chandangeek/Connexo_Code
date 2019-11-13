/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestBulkCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusChangeRequestBulkCreateConfirmationTest extends AbstractOutboundWebserviceTest<SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut> {
    private SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg confirmationMessage = new SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg();
    @Mock
    private StatusChangeRequestBulkCreateConfirmationMessage outboundMessage;

    private StatusChangeRequestBulkCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);

        provider = getProviderInstance(StatusChangeRequestBulkCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).smartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP ConnectionStatusChangeBulkConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(SmartMeterUtilitiesConnectionStatusChangeRequestERPBulkCreateConfirmationCOutService.class);
    }
}
