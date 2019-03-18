/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface FailedTransition {

    DeviceLifeCycle getLifecycle();

    StateTransition getTransition();

    State getFrom();

    State getTo();

    String getCause();

    Instant getOccurrenceTime();

    Instant getCreateTime();
}
