/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import ch.iec.tc57._2011.meterconfig.MeterConfig;

import java.util.Collection;

@ProviderType
public interface MeterConfigFactory {
    MeterConfig asMeterConfig(Device device);
    MeterConfig asGetMeterConfig(Device device);
    MeterConfig asMeterConfig(Collection<Device> devices);
    MeterConfig asGetMeterConfig(Collection<Device> devices);
}