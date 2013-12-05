/*
 * PreviousDemandResetDataTable.java
 *
 * Created on 28 oktober 2005, 16:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class PreviousDemandResetDataTable extends AbstractTable {

    private RegisterInf registerInfo;
    private RegisterData previousDemandResetData;

    /** Creates a new instance of PreviousDemandResetDataTable */
    public PreviousDemandResetDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(25));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousDemandResetDataTable: \n");
        strBuff.append("    registerInfo="+getRegisterInfo()+"\n");
        strBuff.append("    previousDemandResetData="+getPreviousDemandResetData()+"\n");
        return strBuff.toString();
    }

    protected void prepareBuild() throws IOException {
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,RegisterInf.getSize(getTableFactory())+RegisterData.getSize(getTableFactory()));
        setPartialReadInfo(partialReadInfo);
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        setRegisterInfo(new RegisterInf(tableData, offset, getTableFactory()));
        offset+=RegisterInf.getSize(getTableFactory());
        setPreviousDemandResetData(new RegisterData(tableData, offset, getTableFactory()));
        offset+=RegisterData.getSize(getTableFactory());
    }

    public RegisterInf getRegisterInfo() {
        return registerInfo;
    }

    public void setRegisterInfo(RegisterInf registerInfo) {
        this.registerInfo = registerInfo;
    }

    public RegisterData getPreviousDemandResetData() {
        return previousDemandResetData;
    }

    public void setPreviousDemandResetData(RegisterData previousDemandResetData) {
        this.previousDemandResetData = previousDemandResetData;
    }

}
