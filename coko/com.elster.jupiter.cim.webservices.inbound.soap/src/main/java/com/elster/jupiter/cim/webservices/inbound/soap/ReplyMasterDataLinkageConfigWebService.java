package com.elster.jupiter.cim.webservices.inbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.math.BigDecimal;

@ConsumerType
public interface ReplyMasterDataLinkageConfigWebService {

    String NAME = "CIM ReplyMasterDataLinkageConfig";

    /**
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration
     *            - the outbound end point
     * @param successfulDevices
     *            - the list of successfully proceeded devices
     * @param failedDevices
     *            - the list contains the device failed to proceed and the error message
     * @param expectedNumberOfCalls
     *            - the expected number of child calls
     */
    void call(EndPointConfiguration endPointConfiguration, String operation, // List<Device> successfulDevices,
            // List<FailedMeterOperation> failedDevices,
            BigDecimal expectedNumberOfCalls);
}