/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.Optional;

public class ServiceCallStatusImpl implements ServiceCallStatus {
    public static final ServiceCallStatus SUCCESS = new ServiceCallStatusImpl(null, DefaultState.SUCCESSFUL, null);
    public static final ServiceCallStatus ONGOING = new ServiceCallStatusImpl(null, DefaultState.ONGOING, null);

    private final ServiceCall serviceCall;
    private final DefaultState state;
    private String errorMessage;

    ServiceCallStatusImpl(ServiceCallService serviceCallService, ServiceCall serviceCall) {
        this(serviceCallService.getServiceCall(serviceCall.getId()).orElse(serviceCall));
    }

    private ServiceCallStatusImpl(ServiceCall serviceCall) {
        this.serviceCall = serviceCall;
        state = this.serviceCall.getState();
        if (!state.isOpen()) {
            this.serviceCall.getExtension(WebServiceDataExportDomainExtension.class).ifPresent(properties -> errorMessage = properties.getErrorMessage());
        }
    }

    ServiceCallStatusImpl(ServiceCall serviceCall, DefaultState state, String errorMessage) {
        this.serviceCall = serviceCall;
        this.state = state;
        this.errorMessage = errorMessage;
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    @Override
    public DefaultState getState() {
        return state;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public boolean isSuccessful() {
        return DefaultState.SUCCESSFUL == state;
    }

    @Override
    public boolean isFailed() {
        return DefaultState.FAILED == state;
    }

    @Override
    public boolean isOpen() {
        return state.isOpen();
    }
}
