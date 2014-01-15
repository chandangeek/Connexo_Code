package com.energyict.mdc.protocol.api.device;

import com.elster.jupiter.util.time.Interval;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 12/12/12
 * Time: 9:04
 */
public interface DeviceMultiplier {

    BigDecimal getMultiplier();

    Interval getInterval();

}