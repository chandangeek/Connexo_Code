/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;

import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;
import com.google.inject.Inject;

import java.util.List;

public class EndDeviceEventsBuilder {
    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    private final MeteringService meteringService;
    private final EndDeviceEventsFaultMessageFactory faultMessageFactory;

    private List<EndDevice> endDevices;

    @Inject
    public EndDeviceEventsBuilder(MeteringService meteringService,
                           EndDeviceEventsFaultMessageFactory faultMessageFactory) {
        this.meteringService = meteringService;
        this.faultMessageFactory = faultMessageFactory;
    }

}
