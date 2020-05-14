package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.AbstractEvent;


/**
 *
 */

//TODO: md: some events specific for slaved devices overriden here with generic event codes; check correctness
public abstract class ESMR50MbusEventLog<T extends EventEnum> extends AbstractEvent {

    public ESMR50MbusEventLog(DataContainer dc) {
        super(dc);
    }
}
