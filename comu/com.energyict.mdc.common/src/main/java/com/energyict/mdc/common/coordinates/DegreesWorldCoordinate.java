package com.energyict.mdc.common.coordinates;

import java.math.BigDecimal;

public class DegreesWorldCoordinate extends AbstractWorldCoordinate {

    public DegreesWorldCoordinate() {
        super();
    }

    public DegreesWorldCoordinate(BigDecimal value) {
        super(value);
    }

    public Integer getDegrees() {
        return new Integer(Math.abs(getValue().intValue()));
    }

    public Integer getSignedDegrees() {
        return new Integer(getValue().intValue());
    }

    public Integer getMinutes() {
        int result = getSignedMinutes().intValue();
        return result < 0 ? new Integer(-result) : getSignedMinutes();
    }

    private Integer getSignedMinutes() {
        double mod = getValue().doubleValue() % 1;
        double coord = mod * 60;
        return (int)coord;
    }

    public BigDecimal getSeconds() {
        return getSignedSeconds().abs();
    }

    private BigDecimal getSignedSeconds() {
        double mod = getValue().doubleValue() % 1;
        double coord = mod * 60;
        mod = coord % 1;
        coord = mod * 60;
        return BigDecimal.valueOf(coord);
    }
}
