package com.energyict.mdc.device.config;

/**
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 14:26
 */
public interface DeviceMessageUserAction {

    boolean isAuthorized(Role role);

    int databaseIdentifier();

    String getLocalizedName();
}
