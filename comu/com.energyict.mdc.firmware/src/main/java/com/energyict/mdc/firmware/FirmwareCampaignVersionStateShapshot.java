/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;


public interface FirmwareCampaignVersionStateShapshot {

    void save();

    void delete();

    long getId();

    String getFirmwareVersion();
    FirmwareType getFirmwareType();
    FirmwareStatus getFirmwareStatus();
    String getImageIdentifier();
    int getRank();
    String getMeterFirmwareDependency();
    String getCommunicationFirmwareDependency();

}
