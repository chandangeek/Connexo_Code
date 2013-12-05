/*
 * LogIn.java
 *
 * Created on 26 oktober 2005, 11:49
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
public class LogIn extends AbstractProcedure {

    private int userId; // 16 bit
    private byte[] password = new byte[20];

    /** Creates a new instance of LogIn */
    public LogIn(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(18));
    }

    protected void prepare() throws IOException {
        byte[] data = new byte[22];

        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        if (dataOrder==1) {
            data[0] = (byte)((getUserId()>>8) & 0xFF);
            data[1] = (byte)((getUserId()) & 0xFF);
        }
        else if (dataOrder==0) {
            data[0] = (byte)((getUserId()) & 0xFF);
            data[1] = (byte)((getUserId()>>8) & 0xFF);
        }
        else throw new IOException("SetTimeDate, prepare(), invalid dataOrder "+dataOrder);
        System.arraycopy(getPassword(), 0,data,2,getPassword().length);
        setProcedureData(data);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

}
