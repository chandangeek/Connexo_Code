package com.energyict.mdc.channels.serial;

import java.math.BigDecimal;

/**
 * Provide predefined values for the used number of stop bits
 */
public enum NrOfStopBits {
    ONE(1),
    ONE_AND_HALF(1.5f),
    TWO(2);

    private final BigDecimal nrOfStopBits;

    NrOfStopBits(Number nrOfStopBits) {
        this.nrOfStopBits = new BigDecimal(nrOfStopBits.floatValue());
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
