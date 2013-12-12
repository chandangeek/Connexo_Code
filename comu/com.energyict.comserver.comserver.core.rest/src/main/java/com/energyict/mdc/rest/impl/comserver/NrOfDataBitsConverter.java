package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.NrOfDataBits;

/**
 * Converts NrOfDataBits to String and vice-versa
 */
public class NrOfDataBitsConverter {

    public String fromServerValue(NrOfDataBits nrOfDataBits) {
        if (nrOfDataBits!=null) {
            return ""+nrOfDataBits.getNrOfDataBits().intValue();
        } else {
            return null;
        }
    }

    public NrOfDataBits toServerValue(String nrOfDataBitsString) {
        NrOfDataBits mappedValue=null;
        if (nrOfDataBitsString!=null && !nrOfDataBitsString.isEmpty()) {
            int jsonNrOfDataBits = Integer.parseInt(nrOfDataBitsString);
            for (NrOfDataBits nrOfDataBits : NrOfDataBits.values()) {
                if (nrOfDataBits.getNrOfDataBits().intValue()==jsonNrOfDataBits) {
                    mappedValue = nrOfDataBits;
                }
            }
            if (mappedValue==null) {
                throw new IllegalArgumentException("Unable to map NrOfDataBits "+nrOfDataBitsString+" to an existing NrOfDataBits");
            }
        }
        return mappedValue;
    }
}
