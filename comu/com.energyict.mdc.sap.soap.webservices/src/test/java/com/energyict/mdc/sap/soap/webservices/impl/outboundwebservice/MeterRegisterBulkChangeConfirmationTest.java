/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterBulkChangeConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegBulkChgConfMsg;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterRegisterBulkChangeConfirmationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut> {
    private UtilsDvceERPSmrtMtrRegBulkChgConfMsg resultMessage = new UtilsDvceERPSmrtMtrRegBulkChgConfMsg();
    @Mock
    private MeterRegisterBulkChangeConfirmationMessage outboundMessage;

    private MeterRegisterBulkChangeConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(resultMessage);

        provider = getProviderInstance(MeterRegisterBulkChangeConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).utilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut(resultMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP MeterRegisterBulkChangeConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterRegisterBulkChangeConfirmationCOutService.class);
    }}
