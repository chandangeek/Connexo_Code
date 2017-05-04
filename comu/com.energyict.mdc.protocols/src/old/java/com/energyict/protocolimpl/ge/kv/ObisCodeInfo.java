/*
 * ObisCodeInfo.java
 *
 * Created on 16 november 2005, 14:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

/**
 *
 * @author Koen
 */
public class ObisCodeInfo {

    private ObisCode obisCode;
    private String description;
    private Unit unit;
    int registerIndex;

    /** Creates a new instance of ObisCodeInfo */
    public ObisCodeInfo(ObisCode obisCode,String description,Unit unit,int registerIndex) {
        this.obisCode=obisCode;
        this.description=description;
        this.unit=unit;
        this.registerIndex=registerIndex;
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
        return getObisCode().getB()-1;
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
        return getObisCode().getD() == ObisCodeInfoFactory.COIN_DEMAND;
    }
}

