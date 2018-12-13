/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;

public class DeviceInFirmwareCampaignStatusInfo {
    public long amount;
    public IdWithLocalizedValue<String> status;

    public DeviceInFirmwareCampaignStatusInfo() {}

    public DeviceInFirmwareCampaignStatusInfo(String id, long amount, Thesaurus thesaurus){
        this.amount = amount;
        this.status = new IdWithLocalizedValue<>(id, thesaurus.getString(MessageSeeds.Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + id, id));
    }
}
