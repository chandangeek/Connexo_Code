/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.AbstractEvent;

import java.util.TimeZone;

public class CommonEvents extends AbstractEvent {

    public static final int CommonEventGroup = 6;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public CommonEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return CommonEventGroup;
    }
}
