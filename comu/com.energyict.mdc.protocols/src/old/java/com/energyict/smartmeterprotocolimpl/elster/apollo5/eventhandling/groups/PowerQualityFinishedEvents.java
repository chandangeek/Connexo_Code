/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.AbstractEvent;

import java.util.TimeZone;

public class PowerQualityFinishedEvents extends AbstractEvent {

    private static final int PowerQualityFinishedGroup = 3;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public PowerQualityFinishedEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return PowerQualityFinishedGroup;
    }

}
