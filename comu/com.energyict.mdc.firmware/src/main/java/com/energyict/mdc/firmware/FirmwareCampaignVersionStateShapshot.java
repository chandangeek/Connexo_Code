/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface FirmwareCampaignVersionStateShapshot {

    void save();

    void delete();

    String getFirmwareVersion();

    FirmwareType getFirmwareType();

    FirmwareStatus getFirmwareStatus();

    String getImageIdentifier();

    int getRank();

    String getMeterFirmwareDependency();

    String getCommunicationFirmwareDependency();

    String getAuxiliaryFirmwareDependency();
}
