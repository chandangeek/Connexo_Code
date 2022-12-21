/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class TimeOfUseCampaignServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "TimeOfUseCampaignServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    @Inject
    public TimeOfUseCampaignServiceCallHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    public void beforeCancelling(ServiceCall serviceCall, DefaultState oldState) {
        // TODO: may need fix like for firmware campaign
        serviceCall.getExtension(TimeOfUseCampaignDomainExtension.class).get().cancel();
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case FAILED:
                break;
            case ONGOING:
                timeOfUseCampaignService.createItemsOnCampaign(serviceCall);
                break;
            case CANCELLED:
                break;
            case SUCCESSFUL:
                serviceCall.log(LogLevel.INFO, "All child service call operations have been executed");
                break;
            default:
                break;
        }
    }

    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
                timeOfUseCampaignService.cancelCalendarSend(serviceCall);
            case FAILED:
            case REJECTED:
            case SUCCESSFUL:
                complete(parent);
                parent.log(LogLevel.INFO, MessageFormat.format("Service call {0} (type={1}) was " +
                        newState.getDefaultFormat().toLowerCase(), serviceCall.getId(), serviceCall.getType().getName()));
                break;
            default:
                break;
        }
    }

    private void complete(ServiceCall parent) {
        if (isLastChild(findChildren(parent))) {
            if (parent.getState().equals(DefaultState.ONGOING)) {
                if (isCancelling(parent)) {
                    TimeOfUseCampaignDomainExtension timeOfUseCampaignDomainExtension = parent.getExtension(TimeOfUseCampaignDomainExtension.class).get();
                    timeOfUseCampaignDomainExtension.setManuallyCancelled(true);
                    parent.update(timeOfUseCampaignDomainExtension);
                    parent.requestTransition(DefaultState.CANCELLED);
                } else {
                    parent.requestTransition(DefaultState.SUCCESSFUL);
                }
            }
        }
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean isLastChild(List<ServiceCall> serviceCalls) {
        return serviceCalls
                .stream()
                .noneMatch(sc -> sc.getState().equals(DefaultState.ONGOING)
                        || sc.getState().equals(DefaultState.PENDING));
    }

    private boolean isCancelling(ServiceCall parent) {
        return parent.getExtension(TimeOfUseCampaignDomainExtension.class).get().isManuallyCancelled() ||
                findChildren(parent)
                        .stream()
                        .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED));
    }


}
