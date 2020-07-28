/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.schema.message.ErrorType;

import java.util.List;

@ProviderType
public interface EndDeviceEventsServiceProvider {

    String NAME = "CIM SendEndDeviceEvents";

    void call(EndDeviceEventRecord record);

    void call(List<EndDeviceEvent> events, List<ErrorType> errorTypes,
              EndPointConfiguration endPointConfiguration, String correlationId);
}