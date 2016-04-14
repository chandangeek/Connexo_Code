package com.elster.jupiter.util.geo;

import java.io.Serializable;
import java.math.BigDecimal;

public interface Coordinates extends Serializable {

    void setLatitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds);

    void setLongitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds);

    WorldCoordinate getLatitude();

    WorldCoordinate getLongitude();

}
