/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface ServiceCallCancellationHandler {
    List<ServiceCallType> getTypes();

    void cancel(ServiceCall serviceCall);
}
