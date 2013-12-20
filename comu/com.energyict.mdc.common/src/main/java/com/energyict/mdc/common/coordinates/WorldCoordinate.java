package com.energyict.mdc.common.coordinates;

import java.io.Serializable;
import java.math.BigDecimal;

public interface WorldCoordinate extends Serializable {

    public int getSign(); // -1 means negative

    public Integer getDegrees(); // Always > 0!

    public Integer getMinutes();

    public BigDecimal getSeconds();

}
