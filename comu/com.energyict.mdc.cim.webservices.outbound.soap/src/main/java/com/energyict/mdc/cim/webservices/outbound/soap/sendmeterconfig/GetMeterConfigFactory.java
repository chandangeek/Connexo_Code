/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig;

import aQute.bnd.annotation.ProviderType;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import com.energyict.mdc.device.data.Device;

import java.util.List;

@ProviderType
public interface GetMeterConfigFactory {
    MeterConfig asMeterConfig(List<Device> devices);
}