/*
 * TableIDCBitfield.java
 *
 * Created on 17 november 2005, 14:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TableIDCBitfield {


    private int procedureNr;        // TABLE_IDC_BFLD_SIZE bit 10..0
    private boolean stdVsMfgFlag;   // TABLE_IDC_BFLD_SIZE bit 11
    private boolean procFlag;       // TABLE_IDC_BFLD_SIZE bit 12
    private boolean flag1;       // TABLE_IDC_BFLD_SIZE bit 13
    private boolean flag2;       // TABLE_IDC_BFLD_SIZE bit 14
    private boolean flag3;       // TABLE_IDC_BFLD_SIZE bit 15


    /** Creates a new instance of TableIDCBitfield */
    public TableIDCBitfield(byte[] data,int offset, int dataOrder) throws IOException {
        int tableIdcBitfield = C12ParseUtils.getInt(data,offset,2,dataOrder);
        setProcedureNr(tableIdcBitfield & 0x07FF);
        setStdVsMfgFlag((tableIdcBitfield & 0x0800) == 0x0800);
        procFlag = (tableIdcBitfield & 0x1000) == 0x1000;
        flag1 = (tableIdcBitfield & 0x2000) == 0x2000;
        flag2 = (tableIdcBitfield & 0x4000) == 0x4000;
        flag3 = (tableIdcBitfield & 0x8000) == 0x8000;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TableIDCBitfield: procedureNr="+procedureNr+", stdVsMfgFlag="+stdVsMfgFlag+", procFlag="+procFlag+", flag1="+flag1+", flag2="+flag2+", flag3="+flag3+"\n");
        return strBuff.toString();

    }

    static public int getSize() throws IOException {
        return 2;
    }

    public int getProcedureNr() {
        return procedureNr;
    }

    public void setProcedureNr(int procedureNr) {
        this.procedureNr = procedureNr;
    }

    public boolean isStdVsMfgFlag() {
        return stdVsMfgFlag;
    }

    public void setStdVsMfgFlag(boolean stdVsMfgFlag) {
        this.stdVsMfgFlag = stdVsMfgFlag;
    }

    public boolean isProcFlag() {
        return procFlag;
    }

    public void setProcFlag(boolean procFlag) {
        this.procFlag = procFlag;
    }

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public boolean isFlag2() {
        return flag2;
    }

    public void setFlag2(boolean flag2) {
        this.flag2 = flag2;
    }

    public boolean isFlag3() {
        return flag3;
    }

    public void setFlag3(boolean flag3) {
        this.flag3 = flag3;
    }
}
