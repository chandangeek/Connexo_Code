/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EventLogMfgCodeFactory.java
 *
 * Created on 20/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a1800;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.ansi.c12.tables.EventLogCode;
import com.energyict.protocolimpl.ansi.c12.tables.EventLogCodeFactory;
/**
 *
 * @author jsm
 */
public class EventLogMfgCodeFactory extends EventLogCodeFactory {


    static {
        getMfgList().add(new EventLogCode(0,"Enter Tier override",""));
        getMfgList().add(new EventLogCode(1,"Exit Tier override",""));
        getMfgList().add(new EventLogCode(2,"Terminal Cover tamper event","",MeterEvent.TAMPER));
        getMfgList().add(new EventLogCode(3,"Main Cover tamper event","", MeterEvent.TAMPER));
        getMfgList().add(new EventLogCode(4,"External event 0",""));
        getMfgList().add(new EventLogCode(5,"External event 1",""));
        getMfgList().add(new EventLogCode(6,"External event 2",""));
        getMfgList().add(new EventLogCode(7,"External event 3",""));
        getMfgList().add(new EventLogCode(8,"Ph A OFF event","",MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(9,"Ph A ON event",""));
        getMfgList().add(new EventLogCode(10,"Ph B OFF event","",MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(11,"Ph B ON event",""));
        getMfgList().add(new EventLogCode(12,"Ph C OFF event","",MeterEvent.PHASE_FAILURE));
        getMfgList().add(new EventLogCode(13,"Ph C ON event",""));
    }

    /** Creates a new instance of EventLogMfgCodeFactory */
    public EventLogMfgCodeFactory() {
    }

}
