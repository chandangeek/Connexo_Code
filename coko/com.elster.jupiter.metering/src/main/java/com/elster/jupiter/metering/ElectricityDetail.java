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

    void setGrounded(Boolean grounded);

    void setNominalServiceVoltage(Quantity nominalServiceVoltage);

    void setPhaseCode(PhaseCode phaseCode);

    void setRatedCurrent(Quantity ratedCurrent);

    void setRatedPower(Quantity ratedPower);

    void setEstimatedLoad(Quantity estimatedLoad);

    void setLimiter(Boolean limiter);

    void setLoadLimiterType(String loadLimiterType);

    void setLoadLimit(Quantity loadLimit);

    void setInterruptible(Boolean interruptible);
}
