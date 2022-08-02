/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySetValues;

import com.energyict.mdc.common.device.data.Device;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DeviceSAPInfoDomainExtensionTest {
    private String deviceIdentifier = "100000000524205";
    private String deviceLocation = "1001216751";
    private String pointOfDelivery = "1234";
    private Boolean registered = true;
    private Boolean pushEventsToSap = true;

    private CustomPropertySetValues cpsValues;
    private DeviceSAPInfoDomainExtension domainExtension;

    @Mock
    private Device device;

    @Before
    public void setup() {
        cpsValues = CustomPropertySetValues.empty();
        domainExtension = new DeviceSAPInfoDomainExtension(null, null);
    }

    @Test
    public void testCopyFrom() {
        cpsValues.setProperty(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName(), deviceIdentifier);
        cpsValues.setProperty(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName(), deviceLocation);
        cpsValues.setProperty(DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName(), pointOfDelivery);
        cpsValues.setProperty(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.javaName(), registered);
        cpsValues.setProperty(DeviceSAPInfoDomainExtension.FieldNames.PUSH_EVENTS_TO_SAP.javaName(), pushEventsToSap);

        domainExtension.copyFrom(device, cpsValues);

        assertThat(domainExtension.getDeviceIdentifier().get()).isSameAs(deviceIdentifier);
        assertThat(domainExtension.getDeviceLocation().get()).isSameAs(deviceLocation);
        assertThat(domainExtension.getPointOfDelivery().get()).isSameAs(pointOfDelivery);
        assertThat(domainExtension.isRegistered()).isSameAs(registered);
        assertThat(domainExtension.shouldPushEventsToSap()).isSameAs(pushEventsToSap);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setDeviceIdentifier(deviceIdentifier);
        domainExtension.setDeviceLocation(deviceLocation);
        domainExtension.setPointOfDelivery(pointOfDelivery);
        domainExtension.setRegistered(registered);
        domainExtension.setPushEventsToSap(pushEventsToSap);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName()))
                .isEqualTo(deviceIdentifier);
        assertThat(cpsValues.getProperty(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName()))
                .isEqualTo(deviceLocation);
        assertThat(cpsValues.getProperty(DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName()))
                .isEqualTo(pointOfDelivery);
        assertThat(cpsValues.getProperty(DeviceSAPInfoDomainExtension.FieldNames.REGISTERED.javaName()))
                .isEqualTo(registered);
        assertThat(cpsValues.getProperty(DeviceSAPInfoDomainExtension.FieldNames.PUSH_EVENTS_TO_SAP.javaName()))
                .isEqualTo(pushEventsToSap);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}
