/*
 * RecordTemplate.java
 *
 * Created on 4 juli 2006, 9:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReverseRotation {

    private int kWhReverseRotation; // : UINT(0..1);
    private int powerFactorReverseRotation; // : UINT(2..3);
    private int selectableMetricReverseRotation; // : UINT(4..5);
    private int thirdMetricIgnoreReverseRotation; // : UINT(6..7);

    /** Creates a new instance of RecordTemplate */
    public ReverseRotation(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int temp = (int)data[offset++]&0xFF;
        setKWhReverseRotation(temp & 0x03);
        setPowerFactorReverseRotation(((temp & 0x0C)>>2));
        setSelectableMetricReverseRotation(((temp & 0x30)>>4));
        setThirdMetricIgnoreReverseRotation(((temp & 0xC0)>>6));
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReverseRotation:\n");
        strBuff.append("   KWhReverseRotation="+getKWhReverseRotation()+"\n");
        strBuff.append("   powerFactorReverseRotation="+getPowerFactorReverseRotation()+"\n");
        strBuff.append("   selectableMetricReverseRotation="+getSelectableMetricReverseRotation()+"\n");
        strBuff.append("   thirdMetricIgnoreReverseRotation="+getThirdMetricIgnoreReverseRotation()+"\n");
        return strBuff.toString();
    }

    public int getKWhReverseRotation() {
        return kWhReverseRotation;
    }

    private void setKWhReverseRotation(int kWhReverseRotation) {
        this.kWhReverseRotation = kWhReverseRotation;
    }

    public int getPowerFactorReverseRotation() {
        return powerFactorReverseRotation;
    }

    private void setPowerFactorReverseRotation(int powerFactorReverseRotation) {
        this.powerFactorReverseRotation = powerFactorReverseRotation;
    }

    public int getSelectableMetricReverseRotation() {
        return selectableMetricReverseRotation;
    }

    private void setSelectableMetricReverseRotation(int selectableMetricReverseRotation) {
        this.selectableMetricReverseRotation = selectableMetricReverseRotation;
    }

    public int getThirdMetricIgnoreReverseRotation() {
        return thirdMetricIgnoreReverseRotation;
    }

    private void setThirdMetricIgnoreReverseRotation(int thirdMetricIgnoreReverseRotation) {
        this.thirdMetricIgnoreReverseRotation = thirdMetricIgnoreReverseRotation;
    }

}
