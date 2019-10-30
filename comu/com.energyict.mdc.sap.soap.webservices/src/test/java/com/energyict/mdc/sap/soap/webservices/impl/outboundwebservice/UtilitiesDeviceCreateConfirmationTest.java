/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UtilitiesDeviceCreateConfirmationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterCreateConfirmationCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceCreateConfirmationMessage outboundMessage;

    private UtilitiesDeviceCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));
        when(confirmationMessage.getUtilitiesDevice().getID().getValue()).thenReturn("UtilDeviceID");

        provider = getProviderInstance(UtilitiesDeviceCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).utilitiesDeviceERPSmartMeterCreateConfirmationCOut(confirmationMessage);
        SetMultimap<String,String> values = ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), "UtilDeviceID");
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP UtilitiesDeviceERPSmartMeterCreateConfirmation_C_Out'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService.class);
    }
}
