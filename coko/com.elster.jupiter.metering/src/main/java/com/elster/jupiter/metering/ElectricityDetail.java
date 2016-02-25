package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.units.Quantity;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:13
 * To change this template use File | Settings | File Templates.
 */
public interface ElectricityDetail extends UsagePointDetail {

    Boolean isGrounded();

    Quantity getNominalServiceVoltage();

    PhaseCode getPhaseCode();

    Quantity getRatedCurrent();

    Quantity getRatedPower();

    Quantity getEstimatedLoad();

    Boolean isLimiter();

    String getLoadLimiterType();

    Quantity getLoadLimit();

    Boolean isInterruptible();
}
