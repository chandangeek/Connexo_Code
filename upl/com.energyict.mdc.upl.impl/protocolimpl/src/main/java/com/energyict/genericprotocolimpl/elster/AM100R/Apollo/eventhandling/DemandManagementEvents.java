package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:53:07
 */
public class DemandManagementEvents extends AbstractEvent{

    public static final int DemandManagementGroup = 5;

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return DemandManagementGroup;
    }

    /**
     * Constructor
     *
     * @param dc
     */
    public DemandManagementEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }
}
