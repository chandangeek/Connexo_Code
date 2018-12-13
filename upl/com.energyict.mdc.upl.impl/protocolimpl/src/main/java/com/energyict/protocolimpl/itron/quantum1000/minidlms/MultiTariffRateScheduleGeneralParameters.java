/*
 * MultiTariffRateScheduleGeneralParameters.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MultiTariffRateScheduleGeneralParameters extends AbstractDataDefinition {
    
    
    private int scheduleId; // UNSIGNED16,
    private boolean delaySeasonChangeUntilDemandReset; // BOOLEAN,
    private boolean delayChangesUntilEOI; // BOOLEAN,
    //reserved OctetString(28)
    
    /**
     * Creates a new instance of MultiTariffRateScheduleGeneralParameters
     */
    public MultiTariffRateScheduleGeneralParameters(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MultiTariffRateScheduleGeneralParameters:\n");
        strBuff.append("   delayChangesUntilEOI="+isDelayChangesUntilEOI()+"\n");
        strBuff.append("   delaySeasonChangeUntilDemandReset="+isDelaySeasonChangeUntilDemandReset()+"\n");
        strBuff.append("   scheduleId="+getScheduleId()+"\n");
        return strBuff.toString();
    }
    
    protected int getVariableName() {
        return 131; // DLMS_TOU_RATE_SCHEDULE_GENERAL_PARAMETERS
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setScheduleId(ProtocolUtils.getInt(data,offset,2)); // UNSIGNED16,
        offset+=2;
        setDelaySeasonChangeUntilDemandReset(ProtocolUtils.getInt(data,offset++,1)==1);
        setDelayChangesUntilEOI(ProtocolUtils.getInt(data,offset++,1)==1);
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public boolean isDelaySeasonChangeUntilDemandReset() {
        return delaySeasonChangeUntilDemandReset;
    }

    public void setDelaySeasonChangeUntilDemandReset(boolean delaySeasonChangeUntilDemandReset) {
        this.delaySeasonChangeUntilDemandReset = delaySeasonChangeUntilDemandReset;
    }

    public boolean isDelayChangesUntilEOI() {
        return delayChangesUntilEOI;
    }

    public void setDelayChangesUntilEOI(boolean delayChangesUntilEOI) {
        this.delayChangesUntilEOI = delayChangesUntilEOI;
    }
}
