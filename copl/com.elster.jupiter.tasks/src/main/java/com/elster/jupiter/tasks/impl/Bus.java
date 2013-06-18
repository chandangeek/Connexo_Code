package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.time.Clock;

public enum Bus {
    ;
    public static final String COMPONENTNAME = "TSK";

    private static ServiceLocator serviceLocator;

    public static Clock getClock() {
        return serviceLocator.getClock();
    }

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }
}
