/*
 * ClearStandardStatusFlags.java
 *
 * Created on 26 oktober 2005, 10:12
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
public class ClearStandardStatusFlags extends AbstractProcedure {

    private int responseEdModeStatus1; // 16 bit endDeviceModeAndStatusTable end device status 1 bitfield
    private int responseEdModeStatus2; // 8 bit endDeviceModeAndStatusTable end device status 2 bitfield

    /** Creates a new instance of ClearStandardStatusFlags */
    public ClearStandardStatusFlags(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(7));
    }

    protected void parse(byte[] data) throws IOException {
        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        responseEdModeStatus1 = C12ParseUtils.getInt(data,0,2, dataOrder);
        responseEdModeStatus2 = C12ParseUtils.getInt(data,2);
    }

    public int getResponseEdModeStatus1() {
        return responseEdModeStatus1;
    }

    public void setResponseEdModeStatus1(int responseEdModeStatus1) {
        this.responseEdModeStatus1 = responseEdModeStatus1;
    }

    public int getResponseEdModeStatus2() {
        return responseEdModeStatus2;
    }

    public void setResponseEdModeStatus2(int responseEdModeStatus2) {
        this.responseEdModeStatus2 = responseEdModeStatus2;
    }

}
