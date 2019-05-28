/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.rest.impl.MessageSeeds;

public class FirmwareCampaignStatusAdapter extends MapBasedXmlAdapter<FirmwareCampaignStatus> {

    public FirmwareCampaignStatusAdapter() {
        register(DefaultState.SUCCESSFUL.getKey(), FirmwareCampaignStatus.COMPLETE);
        register(MessageSeeds.Keys.FIRMWARE_CAMPAIGN_STATUS_CANCELLED, FirmwareCampaignStatus.CANCELLED);
    }
}
