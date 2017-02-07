/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

/**
 * This is the wrapper class for FSM in service call bundle
 */
@ProviderType
public interface ServiceCallLifeCycle extends HasId, HasName {
    void triggerTransition(ServiceCall serviceCall, DefaultState requestedState);
    void delete();
}
