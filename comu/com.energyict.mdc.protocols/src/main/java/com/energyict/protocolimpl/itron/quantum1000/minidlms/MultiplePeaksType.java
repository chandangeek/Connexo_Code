/*
 * MultiplePeaksType.java
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
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class MultiplePeaksType {

    private QuantityId quantityID; // QUANTITY_ID,
    private int demandType; // UNSIGNED8,
    private boolean peakOrMin; // BOOLEAN,
    private QuantityId coincidentReg1Quantity; // QUANTITY_ID,
    private QuantityId coincidentReg2Quantity; // QUANTITY_ID,
    private QuantityId coincidentReg3Quantity; // QUANTITY_ID,
    private PeakValue[] peakValues;

    /**
     * Creates a new instance of MultiplePeaksType
     */
    public MultiplePeaksType(byte[] data,int offset, TimeZone timeZone) throws IOException {
        setQuantityID(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        setDemandType(ProtocolUtils.getInt(data,offset++,1));
        setPeakOrMin(ProtocolUtils.getInt(data,offset++,1) == 1);
        setCoincidentReg1Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        setCoincidentReg2Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        setCoincidentReg3Quantity(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        setPeakValues(new PeakValue[5]);
        for(int i=0;i<getPeakValues().length;i++) {
            getPeakValues()[i] = new PeakValue(data, offset, timeZone);
            offset+=PeakValue.size();
        }

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MultiplePeaksType:\n");
        strBuff.append("   coincidentReg1Quantity="+getCoincidentReg1Quantity()+"\n");
        strBuff.append("   coincidentReg2Quantity="+getCoincidentReg2Quantity()+"\n");
        strBuff.append("   coincidentReg3Quantity="+getCoincidentReg3Quantity()+"\n");
        strBuff.append("   demandType="+getDemandType()+"\n");
        strBuff.append("   peakOrMin="+isPeakOrMin()+"\n");
        for (int i=0;i<getPeakValues().length;i++) {
            strBuff.append("       peakValues["+i+"]="+getPeakValues()[i]+"\n");
        }
        strBuff.append("   quantityID="+getQuantityID()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 10+5*PeakValue.size();
    }

    public QuantityId getQuantityID() {
        return quantityID;
    }

    public void setQuantityID(QuantityId quantityID) {
        this.quantityID = quantityID;
    }

    public int getDemandType() {
        return demandType;
    }

    public void setDemandType(int demandType) {
        this.demandType = demandType;
    }

    public boolean isPeakOrMin() {
        return peakOrMin;
    }

    public void setPeakOrMin(boolean peakOrMin) {
        this.peakOrMin = peakOrMin;
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

    public PeakValue[] getPeakValues() {
        return peakValues;
    }

    public void setPeakValues(PeakValue[] peakValues) {
        this.peakValues = peakValues;
    }



}
