/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.webservicecall;

import com.elster.jupiter.servicecall.ServiceCall;

import java.util.Optional;

public interface DataExportServiceCallType {
    ServiceCall startServiceCall(String uuid, long timeout);

    Optional<ServiceCall> findServiceCall(String uuid);

    void tryFailingServiceCall(ServiceCall serviceCall, String errorMessage);

    void tryPassingServiceCall(ServiceCall serviceCall);

    ServiceCallStatus getStatus(ServiceCall serviceCall);
}
