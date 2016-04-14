package com.elster.jupiter.util.geo;

import java.io.Serializable;
import java.math.BigDecimal;

public interface WorldCoordinate extends Serializable {

    int getSign(); // -1 means negative

    Integer getDegrees(); // Always > 0!

    Integer getMinutes();

    BigDecimal getSeconds();

}
