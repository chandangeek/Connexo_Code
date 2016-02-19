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
public interface HeatDetail extends UsagePointDetail {

    Quantity getPhysicalCapacity();

    Quantity getPressure();

    Optional<Boolean> getBypass();

    BypassStatus getBypassStatus();

    Optional<Boolean> getValve();

    Boolean isInterruptible();

    void setPressure(Quantity pressure);

    void setPhysicalCapacity(Quantity physicalCapacity);

    void setBypass(Optional<Boolean> bypass);

    void setBypassStatus(BypassStatus bypassStatus);

    void setValve(Optional<Boolean> valve);

    void setInterruptible(Boolean interruptible);
}
