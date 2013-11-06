package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.DeviceProtocolFactoryService;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 10:39
 */
public enum Bus {
    ;

    private static volatile ServiceLocator locator;

    static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.locator = serviceLocator;
    }

    static DeviceProtocolFactoryService getDeviceProtocolFactoryService(){
        return Bus.locator.getDeviceProtocolFactoryService();
    }

}
