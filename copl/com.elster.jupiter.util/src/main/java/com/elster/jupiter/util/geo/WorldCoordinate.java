/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import java.io.Serializable;
import java.math.BigDecimal;

public interface WorldCoordinate extends Serializable {

    int getSign(); // -1 means negative

    Integer getDegrees(); // Always > 0!

    Integer getMinutes();

    Integer getSeconds();

}
