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

    boolean isGrounded();

    Quantity getNominalServiceVoltage();

    PhaseCode getPhaseCode();

    Quantity getRatedCurrent();

    Quantity getRatedPower();

    Quantity getEstimatedLoad();

    void setGrounded(boolean grounded);

    void setNominalServiceVoltage(Quantity nominalServiceVoltage);

    void setPhaseCode(PhaseCode phaseCode);

    void setRatedCurrent(Quantity ratedCurrent);

    void setRatedPower(Quantity ratedPower);

    void setEstimatedLoad(Quantity estimatedLoad);
}
