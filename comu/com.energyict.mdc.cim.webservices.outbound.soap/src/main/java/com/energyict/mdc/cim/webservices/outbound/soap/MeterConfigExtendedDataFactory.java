/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.meterconfig.MeterConfig;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface MeterConfigExtendedDataFactory {

    String NAME = "MeterConfigExtendedDataFactory";

    MeterConfig extendData(List<Device> fromDevices, MeterConfig toMeterConfig);
}