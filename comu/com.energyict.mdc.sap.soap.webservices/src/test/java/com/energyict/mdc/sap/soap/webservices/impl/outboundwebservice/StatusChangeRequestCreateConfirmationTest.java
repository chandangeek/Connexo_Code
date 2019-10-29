/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusChangeRequestCreateConfirmationTest extends AbstractOutboundWebserviceTest<SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts connectionStatus;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg confirmationMessage;
    @Mock
    private StatusChangeRequestCreateConfirmationMessage outboundMessage;


    private StatusChangeRequestCreateConfirmationProvider provider;
    private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts> deviceConnectionStatuses;


    @Before
    public void setUp() {
        deviceConnectionStatuses = Collections.singletonList(connectionStatus);
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(confirmationMessage);
        when(confirmationMessage.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()).thenReturn(deviceConnectionStatuses);
        when(connectionStatus.getUtilitiesDeviceID().getValue()).thenReturn("UtilDeviceID");

        provider = getProviderInstance(StatusChangeRequestCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        SetMultimap<String,String> values = ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), "UtilDeviceID");

        verify(endpoint).smartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut(confirmationMessage);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SapStatusChangeRequestCreateConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(SmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationCOutService.class);
    }
}
