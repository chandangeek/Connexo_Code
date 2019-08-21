/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface NextExecutionSpecBuilder<S> {

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency, TimeDuration offset);

    NextExecutionSpecBuilder<S> temporalExpression(TimeDuration frequency);

    S set();
}
