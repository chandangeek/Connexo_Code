/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;

public class RetryDeviceInFirmwareCampaignExceptions {

    static final String CAMPAIGN_IS_NOT_ONGOING = "DeviceInFirmwareCampaign.ChangeStatusNotAllowed.campaignNotOngoing";
    static final String DEVICE_IN_FIRMWARE_CAMPAIGN_STATE_CHANGE_TO_PENDING_NOT_ALLOWED = "DeviceInFirmwareCampaign.ChangeStatusNotallowed.fromCurrentState";


    public static LocalizedException invalidState(Thesaurus thesaurus){
        return new RetryDeviceInFirmwareCampaignException(thesaurus, MessageSeeds.FIRMWARE_CAMPAIGN_STATUS_INVALID);
    }

    public static LocalizedException transitionToPendingStateImpossible(Thesaurus thesaurus, DeviceInFirmwareCampaign deviceInFirmwareCampaign){
        return new RetryDeviceInFirmwareCampaignException(thesaurus, MessageSeeds.DEVICE_IN_FIRMWARE_CAMPAIGN_STATE_INVALID,
                thesaurus.getString(deviceInFirmwareCampaign.getStatus().key(),deviceInFirmwareCampaign.getStatus().key()) ,
                thesaurus.getString(FirmwareManagementDeviceStatus.Constants.PENDING, FirmwareManagementDeviceStatus.Constants.PENDING));
    }


    private static class RetryDeviceInFirmwareCampaignException extends LocalizedException{
        RetryDeviceInFirmwareCampaignException(Thesaurus thesaurus, MessageSeed messageSeed){
            super(thesaurus, messageSeed);
        }
        RetryDeviceInFirmwareCampaignException(Thesaurus thesaurus, MessageSeed messageSeed,Object... args){
            super(thesaurus, messageSeed, args);
        }
    }


}
