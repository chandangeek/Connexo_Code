/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredBulkNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedBulkNotifMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.AbstractModule;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UtilitiesDeviceRegisteredBulkNotificationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut> {
    @Mock
    private Clock clock;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private WebServiceActivator webServiceActivator;

    private List<String> deviceIds;
    private UtilitiesDeviceRegisteredBulkNotificationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        deviceIds = Arrays.asList("100000000524205", "100000000524206", "100000000524207");

        provider = getProviderInstance(UtilitiesDeviceRegisteredBulkNotificationProvider.class, new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(WebServiceActivator.class).toInstance(webServiceActivator);
            }
        });
        when(sapCustomPropertySets.getStartDate(anyString())).thenReturn(Optional.of(Instant.EPOCH));
        when(webServiceActivator.getMeteringSystemId()).thenReturn(WebServiceActivator.DEFAULT_METERING_SYSTEM_ID);
    }

    @Test
    public void testCall() {
        provider.call(deviceIds);

        SetMultimap<String, String> values = HashMultimap.create();
        deviceIds.forEach(deviceId -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), deviceId));

        verify(endpoint).utilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut(any(UtilsDvceERPSmrtMtrRegedBulkNotifMsg.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(deviceIds))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP SmartMeterRegisteredBulkNotification'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService.class);
    }
}
