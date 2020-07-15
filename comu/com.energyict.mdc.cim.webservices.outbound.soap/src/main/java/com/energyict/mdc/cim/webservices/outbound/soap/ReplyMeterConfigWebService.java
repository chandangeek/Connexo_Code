/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface ReplyMeterConfigWebService {

    String NAME = "CIM ReplyMeterConfig";

    /**
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration - the outbound end point
     * @param successfulDevices - the list of successfully proceeded devices
     * @param failedDevices - the list contains the device failed to proceed and the error message
     * @param expectedNumberOfCalls - the expected number of child calls
     * @param meterStatusRequired - specify if meter status should be included to GET response
     * @param correlationId - correlationId received in inbound request
     */
    void call(EndPointConfiguration endPointConfiguration, OperationEnum operation, List<Device> successfulDevices,
              List<FailedMeterOperation> failedDevices, long expectedNumberOfCalls, boolean meterStatusRequired, String correlationId);
}