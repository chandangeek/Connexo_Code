/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;

@ConsumerType
public interface StateTransitionWebServiceClient {

    String WS_NAME = "CIM ReplyEndDeviceConfig";
    /**
     * Get the registered web service name
     *
     * @return web service name
     */
    String getWebServiceName();

    /**
     * Invoked by the fsm framework when a state was changed
     *
     * @param id - business object id
     * @param endPointConfigurationIds - end point configuration id list
     * @param state - new device state
     * @param effectiveDate - effective date
     */
    void call(long id, List<Long> endPointConfigurationIds, String state, Instant effectiveDate);
}