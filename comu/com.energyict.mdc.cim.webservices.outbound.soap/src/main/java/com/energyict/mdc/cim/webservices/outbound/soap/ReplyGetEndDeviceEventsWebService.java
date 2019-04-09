/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface ReplyGetEndDeviceEventsWebService {

    String NAME = "CIM ReplyGetEndDeviceEvents";

    /**
     * Invoked by the service call when the async inbound WS is completed
     *
     * @param endPointConfiguration - the outbound end point
     * @param endDeviceEvents - end device events in the system
     */
    void call(EndPointConfiguration endPointConfiguration, List<EndDeviceEventRecord> endDeviceEvents);
}
