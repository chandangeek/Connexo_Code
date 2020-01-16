/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterBulkCreateConfirmationProvider;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;

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

public class UtilitiesDeviceRegisterBulkCreateConfirmationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsDvceERPSmrtMtrRegCrteConfMsg msgMock;
    @Mock
    private UtilsDvceERPSmrtMtrRegBulkCrteConfMsg confirmationMessage;
    @Mock
    private UtilitiesDeviceRegisterCreateConfirmationMessage outboundMessage;

    private List<UtilsDvceERPSmrtMtrRegCrteConfMsg> msgList;

    private UtilitiesDeviceRegisterBulkCreateConfirmationProvider provider;

    @Before
    public void setUp() {
        msgList = Collections.singletonList(msgMock);
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getBulkConfirmationMessage()).thenReturn(Optional.of(confirmationMessage));
        when(confirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterCreateConfirmationMessage()).thenReturn(msgList);
        when(msgMock.getUtilitiesDevice().getID().getValue()).thenReturn("UtilDeviceID");

        provider = getProviderInstance(UtilitiesDeviceRegisterBulkCreateConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        SetMultimap<String,String> values = ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), "UtilDeviceID");
        verify(endpoint).utilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut(confirmationMessage);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP SmartMeterRegisterBulkCreateConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmationCOutService.class);
    }
}
