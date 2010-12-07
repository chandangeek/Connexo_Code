package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 *
 * TODO change the implementation of the buffer
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:58:31
 */
public class SynchronizationEvents extends AbstractEvent{

    public static final int SynchronizationEventGroup = 1;

    /**
     * Constructor
     *
     * @param dc
     */
    public SynchronizationEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return SynchronizationEventGroup;
    }
}
