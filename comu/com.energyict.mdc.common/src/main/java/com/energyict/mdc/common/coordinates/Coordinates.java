package com.energyict.mdc.common.coordinates;

import java.io.Serializable;
import java.math.BigDecimal;

public interface Coordinates extends Serializable {

    public void setLatitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds);

    public void setLongitude(int sign, Integer degrees, Integer minutes, BigDecimal seconds);

    public WorldCoordinate getLatitude();

    public WorldCoordinate getLongitude();

}
