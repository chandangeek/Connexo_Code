/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredNotificationProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifMsg;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.AbstractModule;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
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

public class UtilitiesDeviceRegisteredNotificationTest extends AbstractOutboundWebserviceTest<UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut> {
    @Mock
    private Clock clock;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private WebServiceActivator webServiceActivator;
    @Mock
    private Device device;

    private String deviceId;
    private UtilitiesDeviceRegisteredNotificationProvider provider;

    @Before
    public void setUp() {
        when(webServiceCallOccurrence.getId()).thenReturn(1L);
        deviceId = "100000000524205";

        provider = getProviderInstance(UtilitiesDeviceRegisteredNotificationProvider.class, new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(WebServiceActivator.class).toInstance(webServiceActivator);

            }
        });
        when(sapCustomPropertySets.getDevice(anyString())).thenReturn(Optional.of(device));
        when(sapCustomPropertySets.getStartDate(any(Device.class))).thenReturn(Optional.of(Instant.EPOCH));
        when(webServiceActivator.getMeteringSystemId()).thenReturn(WebServiceActivator.DEFAULT_METERING_SYSTEM_ID);
    }

    @Test
    public void testCall() {
        provider.call(deviceId);

        SetMultimap<String,String> values = ImmutableSetMultimap.of(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), deviceId);

        verify(endpoint).utilitiesDeviceERPSmartMeterRegisteredNotificationCOut(any(UtilsDvceERPSmrtMtrRegedNotifMsg.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
    }

    @Test
    public void testCallWithoutPort() {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(deviceId))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'SAP SmartMeterRegisteredNotification'.");
    }

    @Test
    public void testGetService() {
        assertThat(provider.getService()).isSameAs(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut.class);
    }

    @Test
    public void testGet() {
        assertThat(provider.get()).isInstanceOf(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService.class);
    }
}
