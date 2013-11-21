package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.ComServerService;

public class ComServerServiceHolder {
    private static ComServerService comServerService;

    public ComServerService getComServerService() {
        return comServerService;
    }

    static public void setComServerService(ComServerService newComServerService) {
        comServerService = newComServerService;
    }
}
