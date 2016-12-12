package com.energyict.mdc.channels.serial;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * Provide predefined values for the used number of data bits
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
        return Stream
                .of(values())
                .map(NrOfDataBits::getNrOfDataBits)
                .toArray(BigDecimal[]::new);
    }

    public BigDecimal getNrOfDataBits() {
        return nrOfDataBits;
    }

    public static NrOfDataBits valueFor (BigDecimal numercialValue) {
        return Stream
                .of(values())
                .filter(each -> each.getNrOfDataBits().compareTo(numercialValue) == 0)
                .findAny()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.valueOf(getNrOfDataBits().intValue());
    }
}
