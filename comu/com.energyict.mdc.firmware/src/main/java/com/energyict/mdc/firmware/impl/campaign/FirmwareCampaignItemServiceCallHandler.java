/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.MessageSeeds;

import javax.inject.Inject;

public class FirmwareCampaignItemServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "FirmwareCampaignItemServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private final FirmwareCampaignServiceImpl firmwareCampaignService;
    private final Thesaurus thesaurus;

    @Inject
    public FirmwareCampaignItemServiceCallHandler(FirmwareServiceImpl firmwareService, Thesaurus thesaurus) {
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.thesaurus = thesaurus;
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
                if (oldState.equals(DefaultState.CREATED)) {
                    serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().startFirmwareProcess();
                }
                else {
                    serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().retryFirmwareProcess();
                }
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