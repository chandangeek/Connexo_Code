package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.ComServerService;

public class ComServerServiceHolder {
    private ComServerService comServerService;

    public ComServerService getComServerService() {
        return comServerService;
    }

    public ComServerServiceHolder(ComServerService comServerService) {
        this.comServerService = comServerService;
    }

//    static public void setComServerService(ComServerService newComServerService) {
//        comServerService = newComServerService;
//    }
}
