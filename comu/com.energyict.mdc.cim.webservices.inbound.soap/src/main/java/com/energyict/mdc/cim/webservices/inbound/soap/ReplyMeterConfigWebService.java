/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;

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
     * @param id - business object id
     * @param endPointConfigurations - end point configuration list
     */
    void call(long id, List<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate);
}