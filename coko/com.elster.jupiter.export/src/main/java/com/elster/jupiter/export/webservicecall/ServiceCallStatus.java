/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.webservicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.Optional;

public interface ServiceCallStatus {
    ServiceCall getServiceCall();

    DefaultState getState();

    Optional<String> getErrorMessage();

    boolean isSuccessful();

    boolean isFailed();

    boolean isOpen();
}
