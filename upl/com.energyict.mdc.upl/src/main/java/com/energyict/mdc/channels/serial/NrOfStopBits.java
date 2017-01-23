package com.energyict.mdc.channels.serial;

import java.math.BigDecimal;
import java.util.stream.Stream;

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
        return Stream
                .of(values())
                .map(NrOfStopBits::getNrOfStopBits)
                .toArray(BigDecimal[]::new);
    }

    public static NrOfStopBits valueFor(BigDecimal numercialValue) {
        return Stream
                .of(values())
                .filter(each -> each.getNrOfStopBits().compareTo(numercialValue) == 0)
                .findAny()
                .orElse(null);
    }

    public BigDecimal getNrOfStopBits() {
        return nrOfStopBits;
    }

    @Override
    public String toString() {
        return String.valueOf(getNrOfStopBits());
    }
}
