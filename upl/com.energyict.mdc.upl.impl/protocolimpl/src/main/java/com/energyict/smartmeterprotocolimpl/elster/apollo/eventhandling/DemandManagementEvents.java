package com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling;

import com.energyict.dlms.DataContainer;

import java.util.TimeZone;

/**
 * Contians all events related to demand management, e.g. modifications on power threshold
 *<br/>
 * Copyrights EnergyICT<br/>
 * Date: 6-dec-2010<br/>
 * Time: 15:53:07<br/>
 */
public class DemandManagementEvents extends AbstractEvent {

    public static final int DemandManagementGroup = 5;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public DemandManagementEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return DemandManagementGroup;
    }
}
