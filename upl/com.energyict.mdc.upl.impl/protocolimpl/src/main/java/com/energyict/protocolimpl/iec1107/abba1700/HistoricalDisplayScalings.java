/*
 * HistoricalDisplayScalings.java
 *
 * Created on 15 juni 2004, 16:09
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class HistoricalDisplayScalings {

    HistoricalDisplayScalingSet historicalDisplayScalingSet;
    ABBA1700MeterType meterType;
    /** Creates a new instance of HistoricalDisplayScalings */
    public HistoricalDisplayScalings(byte[] data,ABBA1700MeterType meterType) throws ProtocolException {
        this.meterType=meterType;
        parse(data);
    }

    public String toString() {
        return historicalDisplayScalingSet.toString();
    }

    private void parse(byte[] data) throws ProtocolException {
        int length = 22+meterType.getNrOfTariffRegisters()+8+meterType.getExtraOffsetHistoricDisplayScaling();
        historicalDisplayScalingSet = new HistoricalDisplayScalingSet(ProtocolUtils.getSubArray2(data,0,length),meterType);
    }

    /**
     * Getter for property historicalDisplayScalingSets.
     * @return Value of property historicalDisplayScalingSets.
     */
    public com.energyict.protocolimpl.iec1107.abba1700.HistoricalDisplayScalingSet getHistoricalDisplayScalingSet() {
        return this.historicalDisplayScalingSet;
    }
}
