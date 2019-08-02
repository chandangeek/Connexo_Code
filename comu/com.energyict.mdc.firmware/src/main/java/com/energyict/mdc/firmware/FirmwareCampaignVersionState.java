/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;


public interface FirmwareCampaignVersionState {

    void save();

    void delete();

    long getId();

    String getFirmwareVersion();
    String getFirmwareType();
    String getFirmwareStatus();
    String getImageIdentifier();
    String getRank();
    String getMeterFirmwareDependency();
    String getCommunicationFirmwareDependency();

}
