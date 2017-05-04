/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SelfReadDemandRegister.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SelfReadDemandRegister {

    // for register type 0..2
    private QuantityId qid; // QUANTITY_ID // 2 bytes 2
    private float cumulativeDemand; // FLOAT, 6                          D=2
    private float contCumulativeDemand; // FLOAT, 10                     D=143 (OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND)
    private float peakValue; // FLOAT, 14                                D=6
    private Date peakTime; // EXTENDED_DATE_TIME, 22
    private QuantityId coincidentReg1Quantity; // QUANTITY_ID, 24        D=133 (OBISCODE_D_COINCIDENT)
    private QuantityId coincidentReg2Quantity; // QUANTITY_ID, 26        D=134 (OBISCODE_D_COINCIDENT)
    private QuantityId coincidentReg3Quantity; // QUANTITY_ID, 28        D=135 (OBISCODE_D_COINCIDENT)
    private float peakCoincidentReg1Value; // FLOAT, 32                  D=136 (OBISCODE_D_COINCIDENT)
    private float peakCoincidentReg2Value; // FLOAT, 36                  D=137 (OBISCODE_D_COINCIDENT)
    private float peakCoincidentReg3Value; // FLOAT, 40                  D=138 (OBISCODE_D_COINCIDENT)
    private float valleyValue; // FLOAT, 44                              D=3
    private Date valleyTime; // EXTENDED_DATE_TIME, 52
    private float valleyCoincidentReg1Value; // FLOAT, 56                D=139 (OBISCODE_D_COINCIDENT)
    private float valleyCoincidentReg2Value; // FLOAT, 60                D=140 (OBISCODE_D_COINCIDENT)
    private float valleyCoincidentReg3Value; // FLOAT, 64                D=141 (OBISCODE_D_COINCIDENT)

    /**
     * Creates a new instance of SelfReadDemandRegister
     */
    public SelfReadDemandRegister(byte[] data,int offset,TimeZone timeZone) throws IOException {
        setQid(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setCumulativeDemand(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setContCumulativeDemand(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPeakValue(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPeakTime(Utils.getDateFromDateTimeExtended(data,offset, timeZone));
        offset+=Utils.getDateTimeExtendedSize();
        setCoincidentReg1Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setCoincidentReg2Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setCoincidentReg3Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setPeakCoincidentReg1Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPeakCoincidentReg2Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPeakCoincidentReg3Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setValleyValue(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setValleyTime(Utils.getDateFromDateTimeExtended(data,offset, timeZone));
        offset+=Utils.getDateTimeExtendedSize();
        setValleyCoincidentReg1Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setValleyCoincidentReg2Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setValleyCoincidentReg3Value(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadDemandRegister:\n");
        strBuff.append("   coincidentReg1Quantity="+getCoincidentReg1Quantity()+"\n");
        strBuff.append("   coincidentReg2Quantity="+getCoincidentReg2Quantity()+"\n");
        strBuff.append("   coincidentReg3Quantity="+getCoincidentReg3Quantity()+"\n");
        strBuff.append("   contCumulativeDemand="+getContCumulativeDemand()+"\n");
        strBuff.append("   cumulativeDemand="+getCumulativeDemand()+"\n");
        strBuff.append("   peakCoincidentReg1Value="+getPeakCoincidentReg1Value()+"\n");
        strBuff.append("   peakCoincidentReg2Value="+getPeakCoincidentReg2Value()+"\n");
        strBuff.append("   peakCoincidentReg3Value="+getPeakCoincidentReg3Value()+"\n");
        strBuff.append("   peakTime="+getPeakTime()+"\n");
        strBuff.append("   peakValue="+getPeakValue()+"\n");
        strBuff.append("   qid="+getQid()+"\n");
        strBuff.append("   valleyCoincidentReg1Value="+getValleyCoincidentReg1Value()+"\n");
        strBuff.append("   valleyCoincidentReg2Value="+getValleyCoincidentReg2Value()+"\n");
        strBuff.append("   valleyCoincidentReg3Value="+getValleyCoincidentReg3Value()+"\n");
        strBuff.append("   valleyTime="+getValleyTime()+"\n");
        strBuff.append("   valleyValue="+getValleyValue()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 64;
    }

    public QuantityId getQid() {
        return qid;
    }

    public void setQid(QuantityId qid) {
        this.qid = qid;
    }

    public float getCumulativeDemand() {
        return cumulativeDemand;
    }

    public void setCumulativeDemand(float cumulativeDemand) {
        this.cumulativeDemand = cumulativeDemand;
    }

    public float getContCumulativeDemand() {
        return contCumulativeDemand;
    }

    public void setContCumulativeDemand(float contCumulativeDemand) {
        this.contCumulativeDemand = contCumulativeDemand;
    }

    public float getPeakValue() {
        return peakValue;
    }

    public void setPeakValue(float peakValue) {
        this.peakValue = peakValue;
    }

    public Date getPeakTime() {
        return peakTime;
    }

    public void setPeakTime(Date peakTime) {
        this.peakTime = peakTime;
    }

    public QuantityId getCoincidentReg1Quantity() {
        return coincidentReg1Quantity;
    }

    public void setCoincidentReg1Quantity(QuantityId coincidentReg1Quantity) {
        this.coincidentReg1Quantity = coincidentReg1Quantity;
    }

    public QuantityId getCoincidentReg2Quantity() {
        return coincidentReg2Quantity;
    }

    public void setCoincidentReg2Quantity(QuantityId coincidentReg2Quantity) {
        this.coincidentReg2Quantity = coincidentReg2Quantity;
    }

    public QuantityId getCoincidentReg3Quantity() {
        return coincidentReg3Quantity;
    }

    public void setCoincidentReg3Quantity(QuantityId coincidentReg3Quantity) {
        this.coincidentReg3Quantity = coincidentReg3Quantity;
    }

    public float getPeakCoincidentReg1Value() {
        return peakCoincidentReg1Value;
    }

    public void setPeakCoincidentReg1Value(float peakCoincidentReg1Value) {
        this.peakCoincidentReg1Value = peakCoincidentReg1Value;
    }

    public float getPeakCoincidentReg2Value() {
        return peakCoincidentReg2Value;
    }

    public void setPeakCoincidentReg2Value(float peakCoincidentReg2Value) {
        this.peakCoincidentReg2Value = peakCoincidentReg2Value;
    }

    public float getPeakCoincidentReg3Value() {
        return peakCoincidentReg3Value;
    }

    public void setPeakCoincidentReg3Value(float peakCoincidentReg3Value) {
        this.peakCoincidentReg3Value = peakCoincidentReg3Value;
    }

    public float getValleyValue() {
        return valleyValue;
    }

    public void setValleyValue(float valleyValue) {
        this.valleyValue = valleyValue;
    }

    public Date getValleyTime() {
        return valleyTime;
    }

    public void setValleyTime(Date valleyTime) {
        this.valleyTime = valleyTime;
    }

    public float getValleyCoincidentReg1Value() {
        return valleyCoincidentReg1Value;
    }

    public void setValleyCoincidentReg1Value(float valleyCoincidentReg1Value) {
        this.valleyCoincidentReg1Value = valleyCoincidentReg1Value;
    }

    public float getValleyCoincidentReg2Value() {
        return valleyCoincidentReg2Value;
    }

    public void setValleyCoincidentReg2Value(float valleyCoincidentReg2Value) {
        this.valleyCoincidentReg2Value = valleyCoincidentReg2Value;
    }

    public float getValleyCoincidentReg3Value() {
        return valleyCoincidentReg3Value;
    }

    public void setValleyCoincidentReg3Value(float valleyCoincidentReg3Value) {
        this.valleyCoincidentReg3Value = valleyCoincidentReg3Value;
    }



}
