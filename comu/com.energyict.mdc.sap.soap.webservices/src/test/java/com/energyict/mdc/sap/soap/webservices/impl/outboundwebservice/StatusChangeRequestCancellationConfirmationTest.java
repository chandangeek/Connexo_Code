/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation.StatusChangeRequestCancellationConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusChangeRequestCancellationConfirmationTest extends AbstractOutboundWebserviceTest<SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut> {
    private SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmationMessage = new SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg();

    private StatusChangeRequestCancellationConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);

        provider = getProviderInstance(StatusChangeRequestCancellationConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(confirmationMessage);

        verify(endpoint).smartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut(confirmationMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(confirmationMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP ConnectionStatusChangeCancellationConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationConfirmationCOutService.class);
    }
}
