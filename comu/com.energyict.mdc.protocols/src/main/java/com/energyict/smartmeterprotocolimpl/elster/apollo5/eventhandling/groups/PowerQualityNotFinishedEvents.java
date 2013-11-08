package com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.groups;

import com.energyict.dlms.DataContainer;
import com.energyict.smartmeterprotocolimpl.elster.apollo5.eventhandling.AbstractEvent;

import java.util.TimeZone;

/**
 * Contains all events related to voltage variations, e.g. changes of voltage under or over a threshold of Vn for non finished events
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 6-dec-2010<br/>
 * Time: 15:15:20<br/>
 */
public class PowerQualityNotFinishedEvents extends AbstractEvent {

    private static final int PowerQualityNotFinishedGroup = 3;

    /**
     * Constructor
     *
     * @param dc       the dataContainer containing all the raw events
     * @param timeZone the timezone to use for parsing eventTimes
     */
    public PowerQualityNotFinishedEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return PowerQualityNotFinishedGroup;
    }

}
