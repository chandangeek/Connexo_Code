/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Map;

@ConsumerType
public interface ReplyMeterConfigWebService {

    String NAME = "CIM ReplyMeterConfig";

    /**
     * Get the registered web service name
     *
     * @return web service name
     */
    String getWebServiceName();

    /**
     * Invoked by the fsm framework when a state was changed
     *
     * @param endPointConfiguration - end point configuration list
     * @param successfulDevices - the list of successfully proceeded devices
     * @param failedDevices - map<deviceMrid, errorMessage>
     */
    void call(EndPointConfiguration endPointConfiguration, List<Device> successfulDevices, Map<String, String> failedDevices);
}