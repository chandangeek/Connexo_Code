/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import java.util.List;

public interface FirmwareCampaignVersionState {

    void save();

    void delete();

    void setFirmwareVersionState(List<FirmwareVersion> firmwareVersion);
}
