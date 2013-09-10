package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

enum Bus {
    ;

    public static final String COMPONENTNAME = "TSK";

    private static volatile ServiceLocator serviceLocator;

    public static Clock getClock() {
        return serviceLocator.getClock();
    }

    public static void setServiceLocator(final ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }

    public static OrmClient getOrmClient() {
        return serviceLocator.getOrmClient();
    }

    public static QueryService getQueryService() {
        return serviceLocator.getQueryService();
    }

    public static CronExpressionParser getCronExpressionParser() {
        return serviceLocator.getCronExpressionParser();
    }

    public static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }

    public static JsonService getJsonService() {
        return serviceLocator.getJsonService();
    }
}
