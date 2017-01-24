/*
 * TransformerRecord.java
 *
 * Created on 4 juli 2006, 9:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TransformerFactor {

    private int transformerFactor; // 3 BCD 32
    private Number ratioF1; // numberformat 1
    private Number ratioP1; // numberformat 1
    private int applyTransformerFactor; // UINT8

    /** Creates a new instance of TransformerRecord */
    public TransformerFactor(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {

        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setTransformerFactor((int)C12ParseUtils.getBCD2Long(data, offset, 3, cfgt.getDataOrder()));
        offset+=3;

        setRatioF1(C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat1(), cfgt.getDataOrder()));
        offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        setRatioP1(C12ParseUtils.getNumberFromNonInteger(data, offset, cfgt.getNonIntFormat1(), cfgt.getDataOrder()));
        offset+=C12ParseUtils.getNonIntegerSize(cfgt.getNonIntFormat1());
        setApplyTransformerFactor((int)data[offset++]&0xff);
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TransformerFactor:\n");
        strBuff.append("   applyTransformerFactor="+getApplyTransformerFactor()+"\n");
        strBuff.append("   ratioF1="+getRatioF1()+"\n");
        strBuff.append("   ratioP1="+getRatioP1()+"\n");
        strBuff.append("   transformerFactor="+getTransformerFactor()+"\n");

        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();

        return 3+1+cfgt.getNonIntFormat1()*2;
    }

    public int getTransformerFactor() {
        return transformerFactor;
    }

    private void setTransformerFactor(int transformerFactor) {
        this.transformerFactor = transformerFactor;
    }

    public Number getRatioF1() {
        return ratioF1;
    }

    private void setRatioF1(Number ratioF1) {
        this.ratioF1 = ratioF1;
    }

    public Number getRatioP1() {
        return ratioP1;
    }

    private void setRatioP1(Number ratioP1) {
        this.ratioP1 = ratioP1;
    }

    public int getApplyTransformerFactor() {
        return applyTransformerFactor;
    }

    private void setApplyTransformerFactor(int applyTransformerFactor) {
        this.applyTransformerFactor = applyTransformerFactor;
    }

}
