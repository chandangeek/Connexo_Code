/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ServiceCallLifeCycleBuilder {

    ServiceCallLifeCycleBuilder remove(DefaultState state);

    ServiceCallLifeCycleBuilder removeTransition(DefaultState from, DefaultState to);

    ServiceCallLifeCycle create();
}
