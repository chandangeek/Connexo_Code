/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface NextExecutionSpecBuilder<S> {

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency, TimeDuration offset);

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency);

    S set();
}
