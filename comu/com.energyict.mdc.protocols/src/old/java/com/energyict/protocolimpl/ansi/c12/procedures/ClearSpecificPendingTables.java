/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClearSpecificPendingTables.java
 *
 * Created on 26 oktober 2005, 11:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;


import com.energyict.protocolimpl.ansi.c12.tables.Event;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ClearSpecificPendingTables extends AbstractProcedure {
    Event event;

    /** Creates a new instance of ClearSpecificPendingTables */
    public ClearSpecificPendingTables(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(15));
    }

    protected void prepare() throws IOException {
        byte[] data = new byte[Event.SIZE];
        data[0] = (byte)getEvent().getStatusBitfield();
        System.arraycopy(getEvent().getEventStorage(),0,data,1,getEvent().getEventStorage().length);
        setProcedureData(data);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event=event;
    }

}
