package com.elster.jupiter.appserver.impl;


import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;
    public static final String COMPONENTNAME = "APS";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    public static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

    public static MessageService getMessageService() {
        return getLocator().getMessageService();
    }

    public static CronExpressionParser getCronExpressionParser() {
        return getLocator().getCronExpressionParser();
    }

    public static FileImportService getFileImportService() {
        return getLocator().getFileImportService();
    }

    public static JsonService getJsonService() {
        return getLocator().getJsonService();
    }

    public static UserService getUserService() {
        return getLocator().getUserService();
    }

    public static ThreadPrincipalService getThreadPrincipalService() {
        return getLocator().getThreadPrincipalService();
    }

    public static Optional<AppServer> getAppServer() {
        return getLocator().getAppServer();
    }

    public static AppServerCreator getAppServerCreator() {
        return getLocator().getAppServerCreator();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
