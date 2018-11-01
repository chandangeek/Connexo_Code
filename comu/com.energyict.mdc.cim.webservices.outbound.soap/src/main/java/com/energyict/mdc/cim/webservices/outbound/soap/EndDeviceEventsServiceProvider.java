/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface EndDeviceEventsServiceProvider {

    String NAME = "CIM SendEndDeviceEvents";

    boolean send(EndDeviceEventRecord record);
}