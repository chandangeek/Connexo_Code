/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.data.Device;

import java.util.List;

@ProviderType
public interface SendMeterConfigService {
    String RESOURCE = "/meterconfig/SendMeterConfig.wsdl";
    String SEND_METER_CONFIG = "CIM SendMeterConfig";
    String URL = "url";

    /**
     * Invoked by the service call when get meter config request completed or failed
     */
    void call(List<Device> successfulDevices, List<FailedMeterOperation> failedDevices, Long expectedNumberOfCalls, String url);
}
