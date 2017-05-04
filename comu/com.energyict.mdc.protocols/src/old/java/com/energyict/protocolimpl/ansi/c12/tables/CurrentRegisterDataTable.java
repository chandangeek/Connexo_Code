/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CurrentRegisterDataTable.java
 *
 * Created on 27 oktober 2005, 16:59
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
public class CurrentRegisterDataTable extends AbstractTable {

    private RegisterData registerData;


    /** Creates a new instance of CurrentRegisterDataTable */
    public CurrentRegisterDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(23));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentRegisterDataTable: registerData="+getRegisterData()+"\n");
        return strBuff.toString();
    }



    protected void prepareBuild() throws IOException {
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,RegisterData.getSize(getTableFactory()));
        setPartialReadInfo(partialReadInfo);
    }

    protected void parse(byte[] tableData) throws IOException {
        setRegisterData(new RegisterData(tableData,0, getTableFactory()));
    }

    public RegisterData getRegisterData() {
        return registerData;
    }

    public void setRegisterData(RegisterData registerData) {
        this.registerData = registerData;
    }


}
