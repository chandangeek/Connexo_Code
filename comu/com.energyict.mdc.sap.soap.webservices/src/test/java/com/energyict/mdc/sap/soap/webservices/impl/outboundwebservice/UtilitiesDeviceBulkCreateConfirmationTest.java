/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceBulkCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collections;
import java.util.List;
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

public class UtilitiesDeviceBulkCreateConfirmationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsDvceERPSmrtMtrCrteConfMsg utilCreateConfMsg;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceCreateConfirmationMessage outboundMessage;

    private UtilitiesDeviceBulkCreateConfirmationProvider provider;
    private List<UtilsDvceERPSmrtMtrCrteConfMsg> utilCreateConfMsgs;

    @Before
    public void setUp() {
        utilCreateConfMsgs = Collections.singletonList(utilCreateConfMsg);
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getBulkConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));
        when(confirmationMessage.getUtilitiesDeviceERPSmartMeterCreateConfirmationMessage()).thenReturn(utilCreateConfMsgs);
        when(utilCreateConfMsg.getUtilitiesDevice().getID().getValue()).thenReturn("UtilDeviceID");

        provider = getProviderInstance(UtilitiesDeviceBulkCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        SetMultimap<String,String> values = ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), "UtilDeviceID");

        verify(endpoint).utilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut(confirmationMessage);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP SmartMeterBulkCreateConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterBulkCreateConfirmationCOutService.class);
    }
}
