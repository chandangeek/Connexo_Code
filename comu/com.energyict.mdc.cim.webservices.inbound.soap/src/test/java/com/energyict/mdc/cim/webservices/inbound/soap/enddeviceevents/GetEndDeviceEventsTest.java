/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;

import ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.UUID;

public class GetEndDeviceEventsTest extends AbstractMockActivator {

    private static final String DEVICE_MRID = UUID.randomUUID().toString();
    private static final String DEVICE_NAME = "SPE0000001";

    private static final ZonedDateTime MAY_1ST = ZonedDateTime.of(2017, 5, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime JUNE_1ST = MAY_1ST.with(Month.JUNE);
    private static final ZonedDateTime JULY_1ST = MAY_1ST.with(Month.JULY);

    private final ObjectFactory getEndDeviceEventsMessageObjectFactory = new ObjectFactory();

}
