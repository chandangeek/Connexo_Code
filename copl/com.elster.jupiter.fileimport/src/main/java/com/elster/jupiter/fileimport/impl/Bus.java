package com.elster.jupiter.fileimport.impl;

import org.osgi.service.log.LogService;

public enum Bus {
    ;

    static final String COMPONENTNAME = "FIM";

    private static volatile ServiceLocator serviceLocator;

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static LogService getLogService() {
        return serviceLocator.getLogService();
    }
}
