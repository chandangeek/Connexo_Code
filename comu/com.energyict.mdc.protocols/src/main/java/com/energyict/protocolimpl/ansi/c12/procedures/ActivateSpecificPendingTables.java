/*
 * ActivateSpecificPendingTables.java
 *
 * Created on 26 oktober 2005, 10:40
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
public class ActivateSpecificPendingTables extends AbstractProcedure {

    Event event;

    /** Creates a new instance of ActivateSpecificPendingTables */
    public ActivateSpecificPendingTables(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(13));
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