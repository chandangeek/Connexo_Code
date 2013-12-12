package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.NrOfStopBits;

/**
 * Converts NrOfStopBits to String and vice-versa
 */
public class NrOfStopBitsConverter {

    public String fromServerValue(NrOfStopBits nrOfStopBits) {
        if (nrOfStopBits!=null) {
            return ""+nrOfStopBits.getNrOfStopBits().intValue();
        } else {
            return null;
        }
    }

    public NrOfStopBits toServerValue(String nrOfStopBitsString) {
        NrOfStopBits mappedValue=null;
        if (nrOfStopBitsString!=null && !nrOfStopBitsString.isEmpty()) {
            int jsonNrOfStopBits = Integer.parseInt(nrOfStopBitsString);
            for (NrOfStopBits nrOfStopBits : NrOfStopBits.values()) {
                if (nrOfStopBits.getNrOfStopBits().intValue()==jsonNrOfStopBits) {
                    mappedValue = nrOfStopBits;
                }
            }
            if (mappedValue==null) {
                throw new IllegalArgumentException("Unable to map NrOfStopBits "+nrOfStopBitsString+" to an existing NrOfStopBits");
            }
        }
        return mappedValue;
    }
}
