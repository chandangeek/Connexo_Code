package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public class SecondsWorldCoordinate extends AbstractWorldCoordinate {

    public SecondsWorldCoordinate() {
        super();
    }

    public SecondsWorldCoordinate(BigDecimal value) {
        super(value);
    }

    public Integer getDegrees() {
        return new Integer(Math.abs(getValue().intValue()) / 3600);
    }

    private Integer getSignedDegrees() {
        return new Integer(getValue().intValue() / 3600);
    }

    public Integer getMinutes() {
        int result = getSignedMinutes().intValue();
        return result < 0 ? new Integer(-result) : getSignedMinutes();
    }

    private Integer getSignedMinutes() {
        BigDecimal decimalSeconds = getValue();
        int sign = (getSign() == -1) ? -1 : 1;
        return new Integer(((decimalSeconds.intValue() % 3600) * sign) / 60);
    }

    public BigDecimal getSeconds() {
        return getSignedSeconds().abs();
    }

    private BigDecimal getSignedSeconds() {
        BigDecimal decimalSeconds = getValue();
        int degrees = getSignedDegrees();
        int sign = (getSign() == -1) ? -1 : 1;
        BigDecimal value = new BigDecimal((degrees * 3600) + (getMinutes() * 60 * sign));
        return decimalSeconds.add(value.negate()).multiply(new BigDecimal(sign));
    }


}
