package com.energyict.mdc.io;

import java.math.BigDecimal;

/**
 * Provide predefined values for the used number of data bits.
 */
public enum NrOfDataBits {

    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8);

    private final BigDecimal nrOfDataBits;

    NrOfDataBits(Integer nrOfDataBits){
        this.nrOfDataBits = new BigDecimal(nrOfDataBits);
    }

    public static BigDecimal[] getTypedValues(){
        BigDecimal[] typedValues = new BigDecimal[values().length];
        int i = 0;
        for (NrOfDataBits dataBits : values()) {
            typedValues[i++] = dataBits.value();
        }
        return typedValues;
    }

    public BigDecimal value() {
        return nrOfDataBits;
    }

    public static NrOfDataBits valueFor (BigDecimal numercialValue) {
        for (NrOfDataBits dataBitsValues : values()) {
            if (dataBitsValues.value().equals(numercialValue)) {
                return dataBitsValues;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value().intValue());
    }

}