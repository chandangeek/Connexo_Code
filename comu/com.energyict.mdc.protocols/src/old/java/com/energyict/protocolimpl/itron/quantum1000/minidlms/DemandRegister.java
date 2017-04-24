/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Result.java
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
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class DemandRegister {

    private QuantityId quantityId;
    private int demandType; // INSTANTANEOUS=0, THERMAL=1, BLOCKINTERVAL=2

    private QuantityId[] coincidentQuantities = new QuantityId[3];
    // options
    // bit 0..2 multiplierId
    // bit 3 touSummaryRequired
    // bit 4 multiplePeaksRequired
    // bit 5 multipleMinimumRequired
    private int multiplierId;
    private BigDecimal multiplier;
    private boolean touSummaryRequired;
    private boolean multiplePeaksRequired;
    private boolean multipleMinimumRequired;


    /** Creates a new instance of Result */
    public DemandRegister(byte[] data,int offset, MeterSetup meterSetup) throws IOException {
        setQuantityId(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setDemandType(ProtocolUtils.getInt(data,offset++, 1));
        for (int i=0;i< getCoincidentQuantities().length;i++) {
            getCoincidentQuantities()[i] = QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2));
            offset+=2;
        }
        int options = ProtocolUtils.getInt(data,offset++, 1);
        setMultiplierId(options & 0x07);

        // Multiplier ID
        // 0 = 1.0
        // 1 = CT value
        // 2 = PT value
        // 3 = CT*PT value
        // 4 = customized value
        //int multiplierId = ProtocolUtils.getInt(data,offset++,1);
        switch(multiplierId) {
            case 0:
                setMultiplier(new BigDecimal("1.0"));
                break;
            case 1:
                setMultiplier(meterSetup.getCtMultiplier());
                break;
            case 2:
                setMultiplier(meterSetup.getPtMultiplier());
                break;
            case 3:
                setMultiplier(meterSetup.getCtMultiplier().multiply(meterSetup.getPtMultiplier()));
                break;
            case 4:
                setMultiplier(meterSetup.getCustomMultiplier());
                break;
        }
        setTouSummaryRequired((options & 0x08) == 0x08);
        setMultiplePeaksRequired((options & 0x10) == 0x10);
        setMultipleMinimumRequired((options & 0x20) == 0x20);

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegister:\n");
        for (int i=0;i<getCoincidentQuantities().length;i++) {
            strBuff.append("       coincidentQuantities["+i+"]="+getCoincidentQuantities()[i]+"\n");
        }
        strBuff.append("   demandType="+getDemandType()+"\n");
        strBuff.append("   multipleMinimumRequired="+isMultipleMinimumRequired()+"\n");
        strBuff.append("   multiplePeaksRequired="+isMultiplePeaksRequired()+"\n");
        strBuff.append("   multiplierId="+getMultiplierId()+"\n");
        strBuff.append("   quantityId="+getQuantityId()+"\n");
        strBuff.append("   touSummaryRequired="+isTouSummaryRequired()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 10;
    }

    public QuantityId getQuantityId() {
        return quantityId;
    }

    public void setQuantityId(QuantityId quantityId) {
        this.quantityId = quantityId;
    }

    public int getDemandType() {
        return demandType;
    }

    public void setDemandType(int demandType) {
        this.demandType = demandType;
    }

    public QuantityId[] getCoincidentQuantities() {
        return coincidentQuantities;
    }

    public void setCoincidentQuantities(QuantityId[] coincidentQuantities) {
        this.coincidentQuantities = coincidentQuantities;
    }

    private int getMultiplierId() {
        return multiplierId;
    }

    private void setMultiplierId(int multiplierId) {
        this.multiplierId = multiplierId;
    }

    public boolean isTouSummaryRequired() {
        return touSummaryRequired;
    }

    public void setTouSummaryRequired(boolean touSummaryRequired) {
        this.touSummaryRequired = touSummaryRequired;
    }

    public boolean isMultiplePeaksRequired() {
        return multiplePeaksRequired;
    }

    public void setMultiplePeaksRequired(boolean multiplePeaksRequired) {
        this.multiplePeaksRequired = multiplePeaksRequired;
    }

    public boolean isMultipleMinimumRequired() {
        return multipleMinimumRequired;
    }

    public void setMultipleMinimumRequired(boolean multipleMinimumRequired) {
        this.multipleMinimumRequired = multipleMinimumRequired;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }




}
