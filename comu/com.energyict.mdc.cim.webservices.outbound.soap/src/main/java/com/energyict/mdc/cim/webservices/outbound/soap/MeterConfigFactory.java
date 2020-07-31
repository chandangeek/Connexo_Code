/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;
import ch.iec.tc57._2011.meterconfig.MeterConfig;

import java.util.Collection;
import java.util.Map;

@ProviderType
public interface MeterConfigFactory {
    MeterConfig asMeterConfig(Device device);

    MeterConfig asGetMeterConfig(Device device, PingResult pingResult, boolean meterStatusRequired);

    MeterConfig asMeterConfig(Collection<Device> devices);

    MeterConfig asGetMeterConfig(Map<Device, PingResult> devicesAndPingResult, boolean meterStatusRequired);
}