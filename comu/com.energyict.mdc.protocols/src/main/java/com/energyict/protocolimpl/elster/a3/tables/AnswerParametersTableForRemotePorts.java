/*
 * AnswerParametersTableForRemotePorts.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class AnswerParametersTableForRemotePorts extends AbstractTable {
    /*
    Memory storage EEPROM
    Total table size (bytes) 12
    Read access 1
    Write access 3
    */

    private int nrOfRings; // 1 byte Number of rings to wait before answering calls received when inside an answer window. The meter will only answer calls inside answer windows.
    private int nrOfRingsOutside; // 1 byte The number of rings to wait before answering a call received when outside an answer window. The meter will ignore this field and not answer calls outside the answer window.
    private Window[] windows; // 2 windows

    /** Creates a new instance of AnswerParametersTableForRemotePorts */
    public AnswerParametersTableForRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(95,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("AnswerParametersTableForRemotePorts:\n");
        strBuff.append("   nrOfRings="+getNrOfRings()+"\n");
        strBuff.append("   nrOfRingsOutside="+getNrOfRingsOutside()+"\n");
        for (int i=0;i<getWindows().length;i++)
            strBuff.append("   windows["+i+"]="+getWindows()[i]+"\n");
        return strBuff.toString();
    }
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setNrOfRings(C12ParseUtils.getInt(tableData,offset++));
        setNrOfRingsOutside(C12ParseUtils.getInt(tableData,offset++));
        setWindows(new Window[2]);
        for (int i=0;i<getWindows().length;i++) {
            getWindows()[i] = new Window(tableData, offset, getTableFactory());
            offset+=Window.getSize(getTableFactory());
        }
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getNrOfRings() {
        return nrOfRings;
    }

    public void setNrOfRings(int nrOfRings) {
        this.nrOfRings = nrOfRings;
    }

    public int getNrOfRingsOutside() {
        return nrOfRingsOutside;
    }

    public void setNrOfRingsOutside(int nrOfRingsOutside) {
        this.nrOfRingsOutside = nrOfRingsOutside;
    }

    public Window[] getWindows() {
        return windows;
    }

    public void setWindows(Window[] windows) {
        this.windows = windows;
    }

}
