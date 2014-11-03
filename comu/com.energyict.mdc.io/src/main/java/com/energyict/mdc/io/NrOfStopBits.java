package com.energyict.mdc.io;

import java.math.BigDecimal;

/**
 * Provide predefined values for the used number of stop bits.
 */
public enum NrOfStopBits {
    ONE(BigDecimal.ONE),
    ONE_AND_HALF(new BigDecimal(1.5f)),
    TWO(BigDecimal.ONE.add(BigDecimal.ONE));

    private final BigDecimal nrOfStopBits;

    NrOfStopBits(BigDecimal nrOfStopBits) {
        this.nrOfStopBits = nrOfStopBits;
    }

    public static BigDecimal[] getTypedValues() {
        BigDecimal[] typedValues = new BigDecimal[values().length];
        int i = 0;
        for (NrOfStopBits stopBits : values()) {
            typedValues[i++] = stopBits.getNrOfStopBits();
        }
        return typedValues;
    }

    public BigDecimal getNrOfStopBits() {
        return nrOfStopBits;
    }

    public static NrOfStopBits valueFor (BigDecimal numercialValue) {
        for (NrOfStopBits stopBitsValue : values()) {
            if (stopBitsValue.getNrOfStopBits().equals(numercialValue)) {
                return stopBitsValue;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(getNrOfStopBits());
    }

}