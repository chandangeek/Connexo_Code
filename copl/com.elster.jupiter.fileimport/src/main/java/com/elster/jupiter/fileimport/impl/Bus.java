package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.log.LogService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "FIM";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

     static Clock getClock() {
        return getLocator().getClock();
    }

    static CronExpressionParser getCronExpressionParser() {
        return getLocator().getCronExpressionParser();
    }

    static FileNameCollisionResolver getFileNameCollisionResolver() {
        return getLocator().getFileNameCollisionResollver();
    }

    static FileSystem getFileSystem() {
        return getLocator().getFileSystem();
    }

    static JsonService getJsonService() {
        return getLocator().getJsonService();
    }

    static LogService getLogService() {
        return getLocator().getLogService();
    }

    static MessageService getMessageService() {
        return getLocator().getMessageService();
    }

    static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    static Predicates getPredicates() {
        return getLocator().getPredicates();
    }

    static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
