/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.MeterRegisterChangeConfirmationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterRegisterChangeConfirmationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut> {
    private UtilsDvceERPSmrtMtrRegChgConfMsg resultMessage = new UtilsDvceERPSmrtMtrRegChgConfMsg();
    @Mock
    private MeterRegisterChangeConfirmationMessage outboundMessage;

    private MeterRegisterChangeConfirmationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        when(outboundMessage.getConfirmationMessage()).thenReturn(resultMessage);

        provider = getProviderInstance(MeterRegisterChangeConfirmationProvider.class);
    }

    @Test
    public void testCall() {
        provider.call(outboundMessage);

        verify(endpoint).utilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut(resultMessage);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(outboundMessage))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP MeterRegisterChangeConfirmation'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterRegisterChangeConfirmationCOutService.class);
    }}
