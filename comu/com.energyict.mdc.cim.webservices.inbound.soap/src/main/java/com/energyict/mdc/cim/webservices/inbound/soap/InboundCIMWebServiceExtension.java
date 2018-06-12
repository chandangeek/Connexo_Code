/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface InboundCIMWebServiceExtension {
    void extendMeterInfo(Device device, MeterInfo meterInfo);
}