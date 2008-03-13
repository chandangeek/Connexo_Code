/*
 * ABBA1700MeterType.java
 *
 * Created on 24 juni 2004, 8:35
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocol.meteridentification.MeterType;

/**
 *
 * @author  Koen
 */
public class ABBA1700MeterType {

    private static final int METERTYPE_16_TOU=0;
    private static final int METERTYPE_32_TOU=1;
    
    int nrOfTariffRegisters=-1;
    int extraOffsetHistoricDisplayScaling=-1;
    
    /** Creates a new instance of MeterType */
    public ABBA1700MeterType(int type) {
        if (type == METERTYPE_16_TOU)
            nrOfTariffRegisters = 16;
        else if (type == METERTYPE_32_TOU)
            nrOfTariffRegisters = 32;
        if (type == METERTYPE_16_TOU)
            extraOffsetHistoricDisplayScaling = 0;
        else if (type == METERTYPE_32_TOU)
            extraOffsetHistoricDisplayScaling = 124;
    }
    
   
    public ABBA1700MeterType(MeterType meterType) {
        updateWith(meterType);
    }
    
    public void updateWith(MeterType meterType) {
        /*
            /GEC5090100100400@000
            GEC        Manufacturers id (We were GEC, then ABB now Elster, but the original one has been kept for continuity) 
            5        Baud Rate ( 0 - 300, 1 - 600, 2 - 1200, 3 - 2400, 4 - 4800, 5 - 9600) 
            09        Master Unit Id (09 - A1700), 01 - 08 PPM) 
            010        Product Range (010 - A1700, 001 - PPM) 
            010        Device No. The Device No is used for the firmware issue. 
            04        Issue No. 
         */
        
        String deviceNr = meterType.getReceivedIdent().substring(10,13);
        //System.out.println("KV_DEBUG> deviceNr = "+deviceNr+", "+meterType.getReceivedIdent());
        if (("010".compareTo(deviceNr) == 0) ||
            ("012".compareTo(deviceNr) == 0) ||
            ("017".compareTo(deviceNr) == 0) ||
            ("020".compareTo(deviceNr) == 0)) {
             nrOfTariffRegisters = 32;  
             extraOffsetHistoricDisplayScaling = 124;
        }
        else {
             nrOfTariffRegisters = 16;
             extraOffsetHistoricDisplayScaling = 0;
        }
    }
    
    public boolean isAssigned() {
        return ((getExtraOffsetHistoricDisplayScaling()!=-1) &&
                (getNrOfTariffRegisters()!=-1));
    }
    /**
     * Getter for property nrOfTariffRegisters.
     * @return Value of property nrOfTariffRegisters.
     */
    public int getNrOfTariffRegisters() {
        return nrOfTariffRegisters;
    }
  
    /**
     * Getter for property extraOffsetHistoricDisplayScaling.
     * @return Value of property extraOffsetHistoricDisplayScaling.
     */
    public int getExtraOffsetHistoricDisplayScaling() {
        return extraOffsetHistoricDisplayScaling;
    }
    
 
}
