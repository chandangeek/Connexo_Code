package com.energyict.mdc.common.coordinates;

import java.math.BigDecimal;

public class SpatialCoordinates extends AbstractCoordinates {


    public static final String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";

    private DegreesWorldCoordinate latitude;
    private DegreesWorldCoordinate longitude;

    public SpatialCoordinates() {
        super();
    }

    public SpatialCoordinates(DegreesWorldCoordinate latitude, DegreesWorldCoordinate longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public DegreesWorldCoordinate getLatitude() {
        return latitude;
    }

    public void setLatitude(DegreesWorldCoordinate latitude) {
        this.latitude = latitude;
    }

    public DegreesWorldCoordinate getLongitude() {
        return longitude;
    }

    public void setLongitude(DegreesWorldCoordinate longitude) {
        this.longitude = longitude;
    }

    protected DegreesWorldCoordinate calculateDecimalDegrees(int signPara, Integer degrees, Integer minutes, BigDecimal seconds) {
        BigDecimal value = new BigDecimal(degrees);
        BigDecimal sign = new BigDecimal(signPara);
        BigDecimal secs = new BigDecimal(minutes).multiply(BigDecimal.valueOf(60)).add(seconds);
        BigDecimal result = value.add(secs.divide(BigDecimal.valueOf(3600), 20, BigDecimal.ROUND_HALF_UP));
        return new DegreesWorldCoordinate(result.multiply(sign));
    }

    public void setLatitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds) {
        setLatitude(calculateDecimalDegrees(sign, degrees, minutes, seconds));
    }

    public void setLongitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds) {
        setLongitude(calculateDecimalDegrees(sign, degrees, minutes, seconds));
    }

}