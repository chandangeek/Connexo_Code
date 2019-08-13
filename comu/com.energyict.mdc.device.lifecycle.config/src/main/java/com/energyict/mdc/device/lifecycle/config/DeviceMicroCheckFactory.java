/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.energyict.mdc.common.device.lifecycle.config.MicroCheck;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface DeviceMicroCheckFactory {

    Optional<? extends MicroCheck> from(String microCheckKey);

    Set<? extends MicroCheck> getAllChecks();
}
