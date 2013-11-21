package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.ComPortPoolService;

public class ComPortPoolServiceHolder {
    private static ComPortPoolService comPortPoolService;

    public ComPortPoolService getComPortPoolService() {
        return comPortPoolService;
    }

    static public void setComPortPoolService(ComPortPoolService newComServerService) {
        comPortPoolService = newComServerService;
    }
}
