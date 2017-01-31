/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

@ConsumerType
public interface ExecutableMicroCheck extends MicroCheck {

    Optional<ExecutableMicroCheckViolation> execute(UsagePoint usagePoint, Instant transitionTime);
}
