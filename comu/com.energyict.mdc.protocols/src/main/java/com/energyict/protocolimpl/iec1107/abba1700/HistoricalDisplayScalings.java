/*
 * HistoricalDisplayScalings.java
 *
 * Created on 15 juni 2004, 16:09
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import java.io.*;
import java.util.*;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class HistoricalDisplayScalings {
    
    HistoricalDisplayScalingSet historicalDisplayScalingSet;
    ABBA1700MeterType meterType;
    /** Creates a new instance of HistoricalDisplayScalings */
    public HistoricalDisplayScalings(byte[] data,ABBA1700MeterType meterType) throws IOException {
        this.meterType=meterType;
        parse(data);
    }
    
    public String toString() {
        return historicalDisplayScalingSet.toString();
    }
    
    private void parse(byte[] data) throws IOException {
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
