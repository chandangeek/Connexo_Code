/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ConsumerType;

import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;

@ConsumerType
public interface EndDeviceConfigExtendedDataFactory {

    String NAME = "EndDeviceConfigExtendedDataFactory";

    EndDeviceConfig extendData(EndDevice fromEndDevice, EndDeviceConfig toEndDeviceConfig);
}