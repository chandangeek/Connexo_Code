package com.energyict.mdc.io;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Provide predefined values for the used number of stop bits.
 */
public enum NrOfStopBits {
    ONE(1f),
    ONE_AND_HALF(1.5f),
    TWO(2f);

    private final BigDecimal nrOfStopBits;

    NrOfStopBits(float nrOfStopBits) {
        this.nrOfStopBits = new BigDecimal(nrOfStopBits);
    }

    public static BigDecimal[] getTypedValues() {
        return Arrays.stream(values()).map(NrOfStopBits::value).toArray(BigDecimal[]:: new);
    }

    public BigDecimal value() {
        return nrOfStopBits;
    }

    public static NrOfStopBits valueFor (BigDecimal numericalValue) {
        return Arrays.stream(values()).filter(x -> x.value().equals(numericalValue)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "" + value();
    }

}