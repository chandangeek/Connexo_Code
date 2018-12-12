/*
 * HistoricalDisplayScalingSet.java
 *
 * Created on 15 juni 2004, 16:11
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class HistoricalDisplayScalingSet {

    TariffSources tariffSources;
    MDSources mdSources;
    ABBA1700MeterType meterType;

    /** Creates a new instance of HistoricalDisplayScalingSet */
    public HistoricalDisplayScalingSet(byte[] data, ABBA1700MeterType meterType) throws ProtocolException {
        this.meterType=meterType;
        parse(data);
    }

    public String toString() {
        return tariffSources.toString()+"\n"+mdSources.toString();
    }

    protected void parse(byte[] data) throws ProtocolException {
        tariffSources = new TariffSources(ProtocolUtils.getSubArray2(data,meterType.getDisplayScalingTOUOffset(),meterType.getNrOfTariffRegisters()),meterType);
        mdSources = new MDSources(ProtocolUtils.getSubArray2(data,(meterType.getDisplayScalingTOUOffset()+meterType.getNrOfTariffRegisters()),8));
    }

    /**
     * Getter for property tariffSources.
     * @return Value of property tariffSources.
     */
    public com.energyict.protocolimpl.iec1107.abba1700.TariffSources getTariffSources() {
        return tariffSources;
    }



}
