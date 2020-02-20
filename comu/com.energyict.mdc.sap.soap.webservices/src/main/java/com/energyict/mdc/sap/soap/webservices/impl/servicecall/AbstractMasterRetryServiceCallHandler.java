/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMasterRetryServiceCallHandler implements ServiceCallHandler {
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    protected volatile ServiceCallService serviceCallService;
    protected volatile WebServiceActivator webServiceActivator;

    public AbstractMasterRetryServiceCallHandler() {
    }

    @Inject
    public AbstractMasterRetryServiceCallHandler(ServiceCallService serviceCallService, WebServiceActivator webServiceActivator) {
        this.serviceCallService = serviceCallService;
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                RetrySearchDataSourceDomainExtension masterExtension = getMasterDomainExtension(serviceCall);
                masterExtension.setAttemptNumber(masterExtension.getAttemptNumber().add(BigDecimal.ONE));
                serviceCall.update(masterExtension);
                serviceCall.findChildren().stream().forEach(child -> {
                    if (child.canTransitionTo(DefaultState.PENDING)) {
                        child.requestTransition(DefaultState.PENDING);
                    }
                });
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PAUSED)) {
                    List<ServiceCall> openChildren = serviceCall.findChildren()
                            .stream()
                            .filter(child -> child.getState().isOpen())
                            .collect(Collectors.toList());
                    if (openChildren.isEmpty()) {
                        resultTransition(serviceCall);
                    } else {
                        openChildren.forEach(child -> child.requestTransition(DefaultState.ONGOING));
                    }
                } else {
                    resultTransition(serviceCall);
                }
                break;
            case CANCELLED:
            case FAILED:
            case PARTIAL_SUCCESS:
            case SUCCESSFUL:
                sendResultMessage(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
            case FAILED:
            case SUCCESSFUL:
                resultTransition(parentServiceCall);
                break;
            case PAUSED:
                parentServiceCall = lock(parentServiceCall);
                List<ServiceCall> children = ServiceCallHelper.findChildren(parentServiceCall);
                if (ServiceCallHelper.isLastPausedChild(children)) {
                    if (!parentServiceCall.getState().equals(DefaultState.PAUSED)) {
                        if (parentServiceCall.canTransitionTo(DefaultState.ONGOING)) {
                            parentServiceCall.requestTransition(DefaultState.ONGOING);
                        }
                        parentServiceCall.requestTransition(DefaultState.PAUSED);
                    }
                }
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    protected abstract RetrySearchDataSourceDomainExtension getMasterDomainExtension(ServiceCall serviceCall);

    protected void sendResultMessage(ServiceCall serviceCall) {
    }

    private void resultTransition(ServiceCall parent) {
        parent = lock(parent);
        List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
        if (ServiceCallHelper.isLastPausedChild(children)) {
            if (parent.getState().equals(DefaultState.PENDING) && parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.SUCCESSFUL)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.PAUSED)) {
                if (parent.canTransitionTo(DefaultState.PAUSED)) {
                    parent.requestTransition(DefaultState.PAUSED);
                }
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL) && parent.canTransitionTo(DefaultState.PARTIAL_SUCCESS)) {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED) && parent.canTransitionTo(DefaultState.CANCELLED)) {
                parent.requestTransition(DefaultState.CANCELLED);
            } else if (parent.canTransitionTo(DefaultState.FAILED)) {
                parent.requestTransition(DefaultState.FAILED);
            } else if (parent.canTransitionTo(DefaultState.ONGOING)) {
                parent.requestTransition(DefaultState.ONGOING);
            }
        }
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }

}
