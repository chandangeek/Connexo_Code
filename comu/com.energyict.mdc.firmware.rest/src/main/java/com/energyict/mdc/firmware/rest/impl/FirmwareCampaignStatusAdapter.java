package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;

public class FirmwareCampaignStatusAdapter extends MapBasedXmlAdapter<FirmwareCampaignStatus> {

    public FirmwareCampaignStatusAdapter() {
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_PROCESSING, FirmwareCampaignStatus.PROCESSING);
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_SCHEDULED, FirmwareCampaignStatus.SCHEDULED);
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_ONGOING, FirmwareCampaignStatus.ONGOING);
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_COMPLETE, FirmwareCampaignStatus.COMPLETE);
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_CANCELLED, FirmwareCampaignStatus.CANCELLED);
    }
}
