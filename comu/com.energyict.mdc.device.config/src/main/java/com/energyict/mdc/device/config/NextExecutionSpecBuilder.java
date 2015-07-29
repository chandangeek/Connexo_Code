package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 13:23
 */
@ProviderType
public interface NextExecutionSpecBuilder<S> {

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency, TimeDuration offset);

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency);

    S set();
}
