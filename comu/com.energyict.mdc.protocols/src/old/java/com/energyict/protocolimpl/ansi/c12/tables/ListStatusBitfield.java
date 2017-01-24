/*
 * ListStatusBitfield.java
 *
 * Created on 17 november 2005, 17:14
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
public class ListStatusBitfield {

    private int order; // bit 0
    private boolean overflowFlag; // bit 1
    private int listType; // bit 2
    private boolean inhibitOverflowFlag; // bit 3

    /** Creates a new instance of ListStatusBitfield */
    public ListStatusBitfield(byte[] data,int offset) throws IOException {
        int temp = C12ParseUtils.getInt(data,offset);
        setOrder(temp & 0x01);
        setOverflowFlag((temp & 0x02) == 0x02);
        setListType((temp >> 2) & 0x01);
        setInhibitOverflowFlag((temp & 0x08) == 0x08);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RecordTemplate: order="+getOrder()+", overflowFlag="+isOverflowFlag()+", listType="+getListType()+", inhibitOverflowFlag="+isInhibitOverflowFlag()+"\n");
        return strBuff.toString();

    }

    static public int getSize() throws IOException {
        return 1;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isOverflowFlag() {
        return overflowFlag;
    }

    public void setOverflowFlag(boolean overflowFlag) {
        this.overflowFlag = overflowFlag;
    }

    public int getListType() {
        return listType;
    }

    public void setListType(int listType) {
        this.listType = listType;
    }

    public boolean isInhibitOverflowFlag() {
        return inhibitOverflowFlag;
    }

    public void setInhibitOverflowFlag(boolean inhibitOverflowFlag) {
        this.inhibitOverflowFlag = inhibitOverflowFlag;
    }
}
