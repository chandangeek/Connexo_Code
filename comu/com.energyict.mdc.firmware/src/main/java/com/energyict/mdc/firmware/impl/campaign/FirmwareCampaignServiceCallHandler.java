/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.firmware.impl.EventType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class FirmwareCampaignServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "FirmwareCampaignServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final FirmwareCampaignServiceImpl firmwareCampaignService;

    @Inject
    public FirmwareCampaignServiceCallHandler(FirmwareServiceImpl firmwareService) {
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                if (oldState.isOpen()) {
                    firmwareCampaignService.createItemsOnCampaign(serviceCall);
                }
                break;
            case CANCELLED:
                firmwareCampaignService.postEvent(EventType.FIRMWARE_CAMPAIGN_CANCELLED, serviceCall.getExtension(FirmwareCampaignDomainExtension.class).get());
                break;
            case SUCCESSFUL:
                serviceCall.log(LogLevel.INFO, "All child service call operations have been executed.");
                break;
            default:
                break;
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case CANCELLED:
                firmwareCampaignService.postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CANCEL, serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get());
                // and intentionally falling through to the following cases
            case FAILED:
            case REJECTED:
            case SUCCESSFUL:
                completeIfRequired(parent);
                parent.log(LogLevel.INFO, MessageFormat.format("Service call {0} (type={1}) was " +
                        newState.getDefaultFormat().toLowerCase(), serviceCall.getId(), serviceCall.getType().getName()));
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeCancelling(ServiceCall serviceCall, DefaultState oldState) {
        serviceCall.getExtension(FirmwareCampaignDomainExtension.class).get().beforeCancelling();
    }

    private void completeIfRequired(ServiceCall parent) {
        List<ServiceCall> children = findChildren(parent);
        if (parent.getState().equals(DefaultState.ONGOING) && areAllClosed(children)) {
            FirmwareCampaignDomainExtension firmwareCampaignDomainExtension = parent.getExtension(FirmwareCampaignDomainExtension.class).get();
            if (shouldBeCancelled(firmwareCampaignDomainExtension, children)) {
                firmwareCampaignDomainExtension.setManuallyCancelled(true);
                parent.update(firmwareCampaignDomainExtension);
                parent.requestTransition(DefaultState.CANCELLED);
            } else if (children.stream().map(ServiceCall::getState).allMatch(s -> DefaultState.SUCCESSFUL == s || DefaultState.CANCELLED == s)) {
                parent.requestTransition(DefaultState.SUCCESSFUL);
            } else if (children.stream().map(ServiceCall::getState).noneMatch(DefaultState.SUCCESSFUL::equals)) {
                parent.requestTransition(DefaultState.FAILED);
            } else {
                parent.requestTransition(DefaultState.PARTIAL_SUCCESS);
            }
        }
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean areAllClosed(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .noneMatch(sc -> sc.getState().isOpen());
    }

    private boolean shouldBeCancelled(FirmwareCampaignDomainExtension firmwareCampaignDomainExtension, List<ServiceCall> children) {
        return firmwareCampaignDomainExtension.isManuallyCancelled() || children.stream().allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED));
    }
}
