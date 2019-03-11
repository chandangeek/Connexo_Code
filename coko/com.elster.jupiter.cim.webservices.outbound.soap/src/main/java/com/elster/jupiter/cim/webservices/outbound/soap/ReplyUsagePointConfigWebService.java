/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;

import java.math.BigDecimal;
import java.util.List;

@ConsumerType
public interface ReplyUsagePointConfigWebService {

    String NAME = "CIM ReplyUsagePointConfigConfig";

    /**
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration
     *            - the outbound end point
     * @param operation
     *            - the operation that has been performed ("CREATE" or "CLOSE")
     * @param successfulLinkages
     *            - the list of successfully created linkages between usagePoints and meters
     * @param failedLinkages
     *            - the list of failed linkage creation attempts (between usagePoints and meters) with error message code and description
     * @param expectedNumberOfCalls
     *            - the expected number of child calls
     */
    void call(EndPointConfiguration endPointConfiguration, String operation, List<UsagePoint> successList,List<FailedUsagePointOperation> failureList, BigDecimal expectedNumberOfCalls);
}