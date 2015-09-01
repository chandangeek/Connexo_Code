package com.energyict.mdc.io;

import java.math.BigDecimal;
import java.util.Arrays;

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
        return Arrays.stream(values()).map(NrOfDataBits::value).toArray(BigDecimal[]:: new);
    }

    public BigDecimal value() {
        return nrOfDataBits;
    }

    public static NrOfDataBits valueFor (BigDecimal numericalValue) {
        return Arrays.stream(values()).filter(x -> x.value().equals(numericalValue)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "" + value();
    }

}