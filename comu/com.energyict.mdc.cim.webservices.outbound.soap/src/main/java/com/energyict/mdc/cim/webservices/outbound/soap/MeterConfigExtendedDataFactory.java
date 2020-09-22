/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ConsumerType;
import ch.iec.tc57._2011.meterconfig.MeterConfig;

import java.util.Collection;

@ConsumerType
public interface MeterConfigExtendedDataFactory {

    String NAME = "MeterConfigExtendedDataFactory";

    MeterConfig extendData(Collection<Device> fromDevices, MeterConfig toMeterConfig);
}