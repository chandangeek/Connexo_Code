package com.energyict.mdc.device.config;

import com.elster.jupiter.time.TimeDuration;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 13:23
 */
public interface NextExecutionSpecBuilder<S> {

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency, TimeDuration offset);

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency);

    S set();
}
