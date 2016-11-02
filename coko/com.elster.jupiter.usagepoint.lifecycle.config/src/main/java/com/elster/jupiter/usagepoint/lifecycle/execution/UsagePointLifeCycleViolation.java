package com.elster.jupiter.usagepoint.lifecycle.execution;

import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a violation of one of the {@link MicroCheck}s
 * that are configured on an {@link UsagePointTransition}.
 */
@ProviderType
public interface UsagePointLifeCycleViolation {

    MicroCheck getCheck();

    String getLocalizedMessage();

}