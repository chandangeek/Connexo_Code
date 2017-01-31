/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeInfo.java
 *
 * Created on 16 november 2005, 14:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

/**
 *
 * @author Koen
 */
public class ObisCodeInfo {

    private ObisCode obisCode;
    private String description;
    private Unit unit;
    int registerIndex;
    private int datacontrolEntryIndex;

    /** Creates a new instance of ObisCodeInfo */
    public ObisCodeInfo(ObisCode obisCode,String description,Unit unit,int registerIndex, int datacontrolEntryIndex) {
        this.obisCode=obisCode;
        this.description=description;
        this.unit=unit;
        this.registerIndex=registerIndex;
        this.setDatacontrolEntryIndex(datacontrolEntryIndex);
    }

    public String toString() {
        return (getObisCode() +", "+getDescription()+", "+getUnit());
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public String getDescription() {
        return description;
    }

    public Unit getUnit() {
        return unit;
    }

    public boolean isCurrent() {
        return getObisCode().getF()==ObisCodeInfoFactory.CURRENT;
    }
    public boolean isPreviousSeason() {
        return getObisCode().getF()==ObisCodeInfoFactory.PREVIOUS_SEASON;
    }
    public boolean isPreviousDemandReset() {
        return getObisCode().getF()==ObisCodeInfoFactory.PREVIOUS_DEMAND_RESET;
    }
    public boolean isSelfRead() {
        return !isCurrent() && !isPreviousSeason() && !isPreviousDemandReset();
    }
    public int getSelfReadIndex() {
        return getObisCode().getF()-254;
    }
    public int getRegisterIndex() {
        return registerIndex;
    }
    public int getTierIndex() {
        return getObisCode().getE()-1;
    }

    public int getOccurance() {
        return 0; //getObisCode().getB()-1; Occurances only 1! see KV2 doc! // B field is used to indicate harmonics or fundamental and special segmentation between phases!
    }

    public boolean isInstantaneous() {
        return getObisCode().getD() == ObisCode.CODE_D_INSTANTANEOUS;
    }
    public boolean isTimeIntegral() {
        return getObisCode().getD() == ObisCode.CODE_D_TIME_INTEGRAL;
    }
    public boolean isMaximumDemand() {
        return getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND;
    }
    public boolean isCumulativeMaximumDemand() {
        return getObisCode().getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND;
    }
    public boolean isContCumulativeMaximumDemand() {
        return getObisCode().getD() == ObisCodeInfoFactory.CONT_CUMULATIVE_DEMAND;
    }
    public boolean isCoinMaximumDemandDemand() {
        return (getObisCode().getD() >= ObisCodeInfoFactory.COIN_DEMAND) && (getObisCode().getD() < (ObisCodeInfoFactory.COIN_DEMAND+10));
    }

    public int getDatacontrolEntryIndex() {
        return datacontrolEntryIndex;
    }

    public void setDatacontrolEntryIndex(int datacontrolEntryIndex) {
        this.datacontrolEntryIndex = datacontrolEntryIndex;
    }
}

