/*
 * EventLogMfgCodeFactory.java
 *
 * Created on 20/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.tables.EventLogCode;
import com.energyict.protocolimpl.ansi.c12.tables.EventLogCodeFactory;
/**
 *
 * @author Koen
 */
public class EventLogMfgCodeFactory extends EventLogCodeFactory {


    static {
        getMfgList().add(new EventLogCode(0,"Enter Tier override",""));
        getMfgList().add(new EventLogCode(1,"Exit Tier override",""));
    }

    /** Creates a new instance of EventLogMfgCodeFactory */
    public EventLogMfgCodeFactory() {
    }

}
