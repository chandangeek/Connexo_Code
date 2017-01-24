package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.AbstractEvent;

import java.util.TimeZone;

/**
 * Contains all events related to remote and local communication, e.g. begin communication serial port.
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 6-dec-2010<br/>
 * Time: 15:54:18<br/>
 */
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
