package com.elster.jupiter.metering;

import com.elster.jupiter.util.units.Quantity;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:13
 * To change this template use File | Settings | File Templates.
 */
public interface WaterDetail extends UsagePointDetail {
    Boolean isGrounded();

    Boolean isLimiter();

    String getLoadLimiterType();

    Quantity getLoadLimit();

    Quantity getPhysicalCapacity();

    Quantity getPressure();

    Optional<Boolean> getBypass();

    BypassStatus getBypassStatus();

    Optional<Boolean> getValve();

    Optional<Boolean> getCapped();

    Optional<Boolean> getClamped();
}
