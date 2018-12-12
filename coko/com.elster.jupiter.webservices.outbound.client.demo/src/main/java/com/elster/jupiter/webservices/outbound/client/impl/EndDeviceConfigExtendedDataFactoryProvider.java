/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.webservices.outbound.client.impl;

import com.elster.jupiter.cim.webservices.outbound.soap.EndDeviceConfigExtendedDataFactory;
import com.elster.jupiter.metering.EndDevice;

import org.osgi.service.component.annotations.Component;

import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.demo.enddeviceconfig.extendeddata.provider",
        service = {EndDeviceConfigExtendedDataFactory.class},
        immediate = true,
        property = {"name=" + EndDeviceConfigExtendedDataFactory.NAME})
public class EndDeviceConfigExtendedDataFactoryProvider implements EndDeviceConfigExtendedDataFactory {

    @Override
    public EndDeviceConfig extendData(EndDevice fromEndDevice, EndDeviceConfig toEndDeviceConfig) {
        toEndDeviceConfig.getEndDevice().forEach(endDevice -> {
            endDevice.setAmrSystem(fromEndDevice.getAmrSystem().getName());
            fromEndDevice.getLifecycleDates().getReceivedDate().ifPresent(date -> endDevice.getLifecycle().setManufacturedDate(date));
        });
        return toEndDeviceConfig;
    }
}