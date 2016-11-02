package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a violation of one of the {@link MicroCheck}s
 * that are configured on an {@link com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition}.
 */
@ProviderType
public interface UsagePointLifeCycleViolation {

    MicroCheck getCheck();

    String getLocalizedMessage();

}