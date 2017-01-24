/*
 * SnapShotData.java
 *
 * Created on 9 december 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.procedures;

import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SetLoadProfileReadControl extends AbstractProcedure {

    private int nrOfReadBlocks;
    private int readBlockOffset;
    private int defaultNrOfReadBlocks;

    /** Creates a new instance of SnapShotData */
    public SetLoadProfileReadControl(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(11,true));
    }

    protected void prepare() throws IOException {
        byte[] data = new byte[6];

        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        if (dataOrder==1) {
            data[0] = (byte)((nrOfReadBlocks>>8) & 0xFF);
            data[1] = (byte)((nrOfReadBlocks) & 0xFF);
            data[2] = (byte)((readBlockOffset>>8) & 0xFF);
            data[3] = (byte)((readBlockOffset) & 0xFF);
            data[4] = (byte)((defaultNrOfReadBlocks>>8) & 0xFF);
            data[5] = (byte)((defaultNrOfReadBlocks) & 0xFF);
        }
        else if (dataOrder==0) {
            data[0] = (byte)((nrOfReadBlocks) & 0xFF);
            data[1] = (byte)((nrOfReadBlocks>>8) & 0xFF);
            data[2] = (byte)((readBlockOffset) & 0xFF);
            data[3] = (byte)((readBlockOffset>>8) & 0xFF);
            data[4] = (byte)((defaultNrOfReadBlocks) & 0xFF);
            data[5] = (byte)((defaultNrOfReadBlocks>>8) & 0xFF);
        }

        setProcedureData(data);
    }

    public int getNrOfReadBlocks() {
        return nrOfReadBlocks;
    }

    public void setNrOfReadBlocks(int nrOfReadBlocks) {
        this.nrOfReadBlocks = nrOfReadBlocks;
    }

    public int getReadBlockOffset() {
        return readBlockOffset;
    }

    public void setReadBlockOffset(int readBlockOffset) {
        this.readBlockOffset = readBlockOffset;
    }

    public int getDefaultNrOfReadBlocks() {
        return defaultNrOfReadBlocks;
    }

    public void setDefaultNrOfReadBlocks(int defaultNrOfReadBlocks) {
        this.defaultNrOfReadBlocks = defaultNrOfReadBlocks;
    }

}
