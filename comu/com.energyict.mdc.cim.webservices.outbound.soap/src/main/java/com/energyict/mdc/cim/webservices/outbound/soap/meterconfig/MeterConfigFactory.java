/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.meterconfig.MeterConfig;

import java.util.List;

public interface MeterConfigFactory {
    MeterConfig asMeterConfig(List<Device> devices);
    MeterConfig asGetMeterConfig(List<Device> devices);
}