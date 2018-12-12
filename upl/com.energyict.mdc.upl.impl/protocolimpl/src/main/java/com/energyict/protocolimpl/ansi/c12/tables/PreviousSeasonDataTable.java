/*
 * PreviousSeasonDataTable.java
 *
 * Created on 28 oktober 2005, 15:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import java.io.*;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

/**
 *
 * @author Koen
 */
public class PreviousSeasonDataTable extends AbstractTable {
    
    private RegisterInf registerInfo;
    private RegisterData previousSeasonRegisterData;
    
    /** Creates a new instance of PreviousSeasonDataTable */
    public PreviousSeasonDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(24));
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousSeasonDataTable: \n");
        strBuff.append("    registerInfo="+getRegisterInfo()+"\n");
        strBuff.append("    previousSeasonRegisterData="+getPreviousSeasonRegisterData()+"\n");
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
        setPreviousSeasonRegisterData(new RegisterData(tableData, offset, getTableFactory()));
        offset+=RegisterData.getSize(getTableFactory());
    }

    public RegisterInf getRegisterInfo() {
        return registerInfo;
    }

    public void setRegisterInfo(RegisterInf registerInfo) {
        this.registerInfo = registerInfo;
    }

    public RegisterData getPreviousSeasonRegisterData() {
        return previousSeasonRegisterData;
    }

    public void setPreviousSeasonRegisterData(RegisterData previousSeasonRegisterData) {
        this.previousSeasonRegisterData = previousSeasonRegisterData;
    }
        
}
