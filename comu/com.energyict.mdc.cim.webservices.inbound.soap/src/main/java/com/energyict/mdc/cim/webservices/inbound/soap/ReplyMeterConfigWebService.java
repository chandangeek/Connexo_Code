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
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration - the outbound end point
     * @param successfulDevices - the list of successfully proceeded devices
     * @param failedDevices - the map contains the MRID of the device failed to proceed as a key and the error message as a value
     */
    void call(EndPointConfiguration endPointConfiguration, OperationEnum operation, List<Device> successfulDevices, Map<String, String> failedDevices);
}