package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public class SpatialCoordinates {


    public static final String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";


    private Latitude latitude;
    private Longitude longitude;
    private Elevation elevation;

    public SpatialCoordinates() {
        super();
    }

    public SpatialCoordinates(Latitude latitude, Longitude longitude, Elevation elevation) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
    }

    public Latitude getLatitude() {
        return latitude;
    }

    public void setLatitude(Latitude latitude) {
        this.latitude = latitude;
    }

    public Longitude getLongitude() {
        return longitude;
    }

    public void setLongitude(Longitude longitude) {
        this.longitude = longitude;
    }

    public Elevation getElevation() { return elevation;}

    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    protected Angle calculateDecimalDegrees(int signPara, Integer degrees, Integer minutes, BigDecimal seconds) {
        BigDecimal value = BigDecimal.valueOf(degrees);
        BigDecimal sign = BigDecimal.valueOf(signPara);
        BigDecimal secs = BigDecimal.valueOf(minutes).multiply(BigDecimal.valueOf(60)).add(seconds);
        BigDecimal result = value.add(secs.divide(BigDecimal.valueOf(3600), 20, BigDecimal.ROUND_HALF_UP));
        return new Angle(result.multiply(sign));
    }

    public void setLatitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds) {
        setLatitude((Latitude) calculateDecimalDegrees(sign, degrees, minutes, seconds));
    }

    public void setLongitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds) {
        setLongitude((Longitude) calculateDecimalDegrees(sign, degrees, minutes, seconds));
    }

    public void setElevation(BigDecimal value) {
        setElevation(elevation);
    }


    @Override
    public String toString() {
        return latitude.toString() + " : " + longitude.toString() + " : " + elevation.toString();
    }
}