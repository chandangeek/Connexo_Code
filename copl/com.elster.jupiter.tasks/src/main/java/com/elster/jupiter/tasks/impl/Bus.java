package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

enum Bus {
    ;

    public static final String COMPONENTNAME = "TSK";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static Clock getClock() {
        return getLocator().getClock();
    }

    public static MessageService getMessageService() {
        return getLocator().getMessageService();
    }

    public static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    public static QueryService getQueryService() {
        return getLocator().getQueryService();
    }

    public static CronExpressionParser getCronExpressionParser() {
        return getLocator().getCronExpressionParser();
    }

    public static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

    public static JsonService getJsonService() {
        return getLocator().getJsonService();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
