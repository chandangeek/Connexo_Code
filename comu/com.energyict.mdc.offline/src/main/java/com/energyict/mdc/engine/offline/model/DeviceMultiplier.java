package com.energyict.mdc.engine.offline.model;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 12/12/12
 * Time: 9:04
 */
public interface DeviceMultiplier {

    BigDecimal getMultiplier();

    TimePeriod getPeriod();
}
