/*
 * ChangeEndDeviceMode.java
 *
 * Created on 19 oktober 2005, 21:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ChangeEndDeviceMode extends AbstractProcedure {

    /** Creates a new instance of ChangeEndDeviceMode */
    public ChangeEndDeviceMode(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(6));
    }

    protected void prepare() throws IOException {
        int edMode = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getEndDeviceModeAndStatusTable().getEdMode();
        setProcedureData(new byte[]{(byte)edMode});
    }


}