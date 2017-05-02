package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300Properties;

/**
 * Copyrights EnergyICT
 * Date: 12/09/12
 * Time: 15:11
 * Author: khe
 */
class AS300DPETProperties extends AS300Properties {

    private static final int FIRMWARE_CLIENT = 3;
    private static final int DEFAULT_AS300_PET_CLIENT_MAC_ADDRESS = 1;
    private static final String DEFAULT_AS300_PET_LOGICAL_DEVICE_ADDRESS = "1:17";

    AS300DPETProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public boolean isFirmwareUpdateSession() {
        return getClientMacAddress() == FIRMWARE_CLIENT;
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_AS300_PET_CLIENT_MAC_ADDRESS);   //Management client
    }

    @Override
    public String getServerMacAddress() {
        return getStringValue(SERVER_MAC_ADDRESS, DEFAULT_AS300_PET_LOGICAL_DEVICE_ADDRESS);
    }

}