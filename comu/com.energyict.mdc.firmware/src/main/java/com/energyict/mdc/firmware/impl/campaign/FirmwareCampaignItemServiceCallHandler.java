/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FirmwareCampaignItemServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "FirmwareCampaignItemServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";
    public static final DefaultState RETRY_STATE = DefaultState.PENDING;

    private final FirmwareCampaignServiceImpl firmwareCampaignService;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;

    @Inject
    public FirmwareCampaignItemServiceCallHandler(FirmwareServiceImpl firmwareService, ServiceCallService serviceCallService,
                                                  Thesaurus thesaurus) {
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                ServiceCallFilter serviceCallFilter = new ServiceCallFilter();
                if (serviceCall.getTargetObject().isPresent()) {
                    serviceCallFilter.targetObjects.add(serviceCall.getTargetObject().get());
                    serviceCallFilter.states = Arrays.stream(DefaultState.values())
                            .filter(DefaultState::isOpen)
                            .map(DefaultState::name)
                            .collect(Collectors.toList());
                    serviceCallFilter.types.add(serviceCall.getType().getName());
                    if (serviceCallService.getServiceCallFinder(serviceCallFilter).stream().anyMatch(sc -> !sc.equals(serviceCall))) {
                        throw new FirmwareCampaignException(thesaurus, MessageSeeds.DEVICE_PART_OF_ANOTHER_CAMPAIGN);
                    }
                }
                if (oldState.equals(DefaultState.CREATED)) {
                    serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().startFirmwareProcess();
                } else {
                    if (!oldState.isOpen()) {
                        serviceCall.getParent().filter(parent -> parent.canTransitionTo(DefaultState.ONGOING))
                                .ifPresent(parent -> parent.requestTransition(DefaultState.ONGOING));
                    }
                    serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().retryFirmwareProcess();
                }
                break;
            case SUCCESSFUL:
                serviceCall.log(LogLevel.INFO, "All child service call operations have been executed");
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeCancelling(ServiceCall serviceCall, DefaultState oldState) {
        serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().beforeCancelling();
    }
}
