/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import javax.inject.Inject;

public class TimeOfUseItemServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "TimeOfUseItemServiceCallHandler";
    public static final String VERSION = "v1.0";

    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    @Inject
    public TimeOfUseItemServiceCallHandler(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
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
                timeOfUseCampaignService.setCalendarOnDevice(serviceCall);
                break;
            case ONGOING:
                break;
            case FAILED:
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

}