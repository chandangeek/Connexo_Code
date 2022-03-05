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
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return true;
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
                if (oldState.isOpen()) {
                    firmwareCampaignService.createItemsOnCampaign(serviceCall);
                }
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
                if (isCancelling(parent)) {
                    FirmwareCampaignDomainExtension firmwareCampaignDomainExtension = parent.getExtension(FirmwareCampaignDomainExtension.class).get();
                    firmwareCampaignDomainExtension.setManuallyCancelled(true);
                    parent.update(firmwareCampaignDomainExtension);
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
        return parent.getExtension(FirmwareCampaignDomainExtension.class).get().isManuallyCancelled() ||
                findChildren(parent)
                        .stream()
                        .allMatch(sc -> sc.getState().equals(DefaultState.CANCELLED));
    }

    @Override
    public void onCancel(ServiceCall serviceCall){
        firmwareCampaignService.cancelServiceCall(serviceCall);
        firmwareCampaignService.cancelDeviceMessage(serviceCall);
    }

}
