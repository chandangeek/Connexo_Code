package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 15:12:50
 */
public class FraudDetectionEvents extends AbstractEvent {

    private static final int FraudDetectionGroup = 4;

    /**
     * Constructor
     *
     * @param dc
     */
    public FraudDetectionEvents(DataContainer dc, TimeZone timeZone) {
        super(dc, timeZone);
    }

    /**
     * Getter for the groupId
     *
     * @return the value of the event group
     */
    @Override
    protected int getGroupId() {
        return FraudDetectionGroup;
    }
}
