package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.log.LogService;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "FIM";

    private static volatile ServiceLocator serviceLocator;

    static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    static LogService getLogService() {
        return serviceLocator.getLogService();
    }

    static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }

    static CronExpressionParser getCronExpressionParser() {
        return serviceLocator.getCronExpressionParser();
    }

    static OrmClient getOrmClient() {
        return serviceLocator.getOrmClient();
    }

    static FileNameCollisionResolver getFileNameCollisionResolver() {
        return serviceLocator.getFileNameCollisionResollver();
    }

     static Clock getClock() {
        return serviceLocator.getClock();
    }

    static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }

    static JsonService getJsonService() {
        return serviceLocator.getJsonService();
    }

    static FileSystem getFileSystem() {
        return serviceLocator.getFileSystem();
    }

    static Predicates getPredicates() {
        return serviceLocator.getPredicates();
    }
}
