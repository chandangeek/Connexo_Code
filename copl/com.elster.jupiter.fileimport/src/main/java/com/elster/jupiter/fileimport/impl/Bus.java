package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
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

    public static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }

    public static CronExpressionParser getCronExpressionParser() {
        return serviceLocator.getCronExpressionParser();
    }

    public static OrmClient getOrmClient() {
        return serviceLocator.getOrmClient();
    }

    public static FileNameCollisionResolver getFileNameCollisionResolver() {
        return new SimpleFileNameCollisionResolver();
    }

    public static Clock getClock() {
        return serviceLocator.getClock();
    }

    public static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }
}
