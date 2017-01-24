package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.AbstractEvent;

import java.util.TimeZone;

/**
 * Contains all events related to firmware changes
 * <br/>
 * TODO change the implementation of the buffer
 * Copyrights EnergyICT<br/>
 * Date: 6-dec-2010<br/
 * Time: 15:56:50<br/
 */
public class FirmwareEvents extends AbstractEvent {

    public static final int FirmwareEventGroup = 1;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public FirmwareEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return FirmwareEventGroup;
    }

}
