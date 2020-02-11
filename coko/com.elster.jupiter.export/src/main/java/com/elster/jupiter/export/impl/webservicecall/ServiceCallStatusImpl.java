/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceCallStatusImpl implements ServiceCallStatus {
    private final ServiceCall serviceCall;
    private final DefaultState state;
    private String errorMessage;

    ServiceCallStatusImpl(ServiceCallService serviceCallService, ServiceCall serviceCall) {
        this(serviceCallService.getServiceCall(serviceCall.getId()).orElse(serviceCall));
    }

    static List<ServiceCallStatus> from(ServiceCallService serviceCallService, Collection<ServiceCall> serviceCalls) {
        if (serviceCalls.isEmpty()) {
            return new ArrayList<>();
        }
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.ids = serviceCalls.stream()
                .map(ServiceCall::getId)
                .collect(Collectors.toSet());
        return serviceCallService.getServiceCallFinder(filter).stream()
                .map(ServiceCallStatusImpl::new)
                .collect(Collectors.toList());
    }

    private ServiceCallStatusImpl(ServiceCall serviceCall) {
        this.serviceCall = serviceCall;
        state = this.serviceCall.getState();
        if (!state.isOpen()) {
            this.serviceCall.getExtension(WebServiceDataExportDomainExtension.class).ifPresent(properties -> errorMessage = properties.getErrorMessage());
        }
    }

    public ServiceCallStatusImpl(ServiceCall serviceCall, DefaultState state, String errorMessage) {
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
    public boolean isPartiallySuccessful() {
        return DefaultState.PARTIAL_SUCCESS == state;
    }

    @Override
    public boolean isFailed() {
        return DefaultState.FAILED == state;
    }

    @Override
    public boolean isOpen() {
        return state.isOpen();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof ServiceCallStatusImpl
                && Objects.equals(serviceCall, ((ServiceCallStatusImpl) obj).serviceCall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceCall);
    }
}
