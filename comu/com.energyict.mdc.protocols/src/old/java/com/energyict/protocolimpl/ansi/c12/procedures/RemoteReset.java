/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RemoteReset.java
 *
 * Created on 26 oktober 2005, 10:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class RemoteReset extends AbstractProcedure {



    private int actionFlagBitfield;
    private int responseActionFlagBitfield;

    /** Creates a new instance of RemoteReset */
    public RemoteReset(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(9));
    }

    protected void prepare() throws IOException {
        setProcedureData(new byte[]{(byte)getActionFlagBitfield()});
    }

    protected void parse(byte[] data) throws IOException {
        responseActionFlagBitfield = C12ParseUtils.getInt(data,0);
    }

    public void setDemandReset(boolean demandReset) {
        if (demandReset)
           setActionFlagBitfield(getActionFlagBitfield() | 0x01);
        else
           setActionFlagBitfield(getActionFlagBitfield() & (0x01^0xFF));
    }

    public int getActionFlagBitfield() {
        return actionFlagBitfield;
    }

    public void setActionFlagBitfield(int actionFlagBitfield) {
        this.actionFlagBitfield = actionFlagBitfield;
    }

    public int getResponseActionFlagBitfield() {
        return responseActionFlagBitfield;
    }

    public void setResponseActionFlagBitfield(int responseActionFlagBitfield) {
        this.responseActionFlagBitfield = responseActionFlagBitfield;
    }
}
