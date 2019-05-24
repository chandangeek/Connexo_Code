/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.math.BigDecimal;
import java.util.List;

@ConsumerType
public interface ReplyUsagePointConfigWebService {

    String NAME = "CIM ReplyUsagePointConfig";

    /**
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration
     *            - the outbound end point
     * @param operation
     *            - the operation that has been performed ("CREATE" or "UPDATE")
     * @param successList
     *            - the list of successfully created usagePoints
     * @param failureList
     *            - the list of failed usagePoint creation or update attempts with error message code and description
     * @param expectedNumberOfCalls
     *            - the expected number of child calls
     */
    void call(EndPointConfiguration endPointConfiguration, String operation, List<UsagePoint> successList,
            List<FailedUsagePointOperation> failureList, BigDecimal expectedNumberOfCalls);
}