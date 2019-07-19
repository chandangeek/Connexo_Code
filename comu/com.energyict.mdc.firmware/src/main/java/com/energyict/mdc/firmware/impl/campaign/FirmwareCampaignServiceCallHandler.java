/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.firmware.impl.EventType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FirmwareCampaignServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "FirmwareCampaignServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";
    public static final DefaultState RETRY_STATE = DefaultState.ONGOING;

    private volatile FirmwareCampaignServiceImpl firmwareCampaignService;

    @Inject
    public FirmwareCampaignServiceCallHandler(FirmwareServiceImpl firmwareService) {
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return true;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        if (!oldState.isOpen()) {
            switch (newState) {
                case ONGOING:
                    ServiceCallFilter filter = new ServiceCallFilter();
                    filter.states = Arrays.stream(DefaultState.values()).filter(DefaultState::isOpen).map(DefaultState::name).collect(Collectors.toList());
                    if (!serviceCall.findChildren(filter).stream().findFirst().isPresent()) {
                        serviceCall.findChildren().stream().forEach(kid ->  kid.requestTransition(kid.getType().getRetryState().orElse(RETRY_STATE)));
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (newState) {
                case PENDING:
                    serviceCall.requestTransition(DefaultState.ONGOING);
                    break;
                case FAILED:
                    break;
                case ONGOING:
                    firmwareCampaignService.createItemsOnCampaign(serviceCall);
                    break;
                case CANCELLED:
                    firmwareCampaignService.postEvent(EventType.FIRMWARE_CAMPAIGN_CANCELLED, serviceCall.getExtension(FirmwareCampaignDomainExtension.class).get());
                    break;
                case SUCCESSFUL:
                    serviceCall.log(LogLevel.INFO, "All child service call operations have been executed");
                    break;
                default:
                    break;
            }
        }
    }

    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
                firmwareCampaignService.handleFirmwareUploadCancellation(serviceCall);
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
                if (isCancelling(findChildren(parent))) {
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

    private boolean isCancelling(List<ServiceCall> serviceCalls) {
        return serviceCalls
                .stream()
                .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED));
    }


}