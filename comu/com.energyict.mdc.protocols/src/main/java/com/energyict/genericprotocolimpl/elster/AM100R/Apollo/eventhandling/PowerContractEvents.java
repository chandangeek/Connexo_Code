package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;

import java.util.TimeZone;

/**
 * Contains all events related to power contract change.
 * <br/>
 * TODO change the implementation of the buffer
 * Copyrights EnergyICT<br/>
 * Date: 6-dec-2010<br/>
 * Time: 15:55:25<br/>
 */
public class PowerContractEvents extends AbstractEvent {

    public static final int PowerContractEventsGroup = 1;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public PowerContractEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return PowerContractEventsGroup;
    }

}
