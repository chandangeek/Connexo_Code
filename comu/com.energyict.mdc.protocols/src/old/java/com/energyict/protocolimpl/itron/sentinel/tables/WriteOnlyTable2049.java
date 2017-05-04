/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * WriteOnlyTable2049.java
 *
 * Created on 02112006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.sentinel.logicalid.DataReadDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class WriteOnlyTable2049 extends AbstractTable {



    private DataReadDescriptor dataReadDescriptor;

    /** Creates a new instance of WriteOnlyTable2049 */
    public WriteOnlyTable2049(ManufacturerTableFactory manufacturerTableFactory) {
        //super(manufacturerTableFactory,new TableIdentification(1,true)); // alternative way of declaration
        super(manufacturerTableFactory,new TableIdentification(2049));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("WriteOnlyTable2049:\n");
        strBuff.append("   dataReadDescriptor="+getDataReadDescriptor()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    protected void prepareTransfer() throws IOException {
        byte[] tableData = new byte[1+1+4*getDataReadDescriptor().getLids().length];
        int dataOrder = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        tableData[0] = (byte)getDataReadDescriptor().getMode();
        tableData[1] = (byte)getDataReadDescriptor().getCount();
        byte[] lid;
        for (int i=0;i<getDataReadDescriptor().getLids().length;i++) {
            if (dataOrder == 0) { // least significant first
                lid = ParseUtils.getArrayLE(getDataReadDescriptor().getLids()[i], 4);
            }
            else if (dataOrder == 1) { // most significant first
                lid = ParseUtils.getArray(getDataReadDescriptor().getLids()[i], 4);
            }
            else throw new IOException("WriteOnlyTable2049, prepareTransfer(), invalid dataOrder "+dataOrder);
            System.arraycopy(lid, 0, tableData, 2+i*4, 4);
        }
        setTableData(tableData);

    } // protected void prepareTransfer() throws IOException

    public DataReadDescriptor getDataReadDescriptor() {
        return dataReadDescriptor;
    }

    public void setDataReadDescriptor(DataReadDescriptor dataReadDescriptor) {
        this.dataReadDescriptor = dataReadDescriptor;
    }




}
