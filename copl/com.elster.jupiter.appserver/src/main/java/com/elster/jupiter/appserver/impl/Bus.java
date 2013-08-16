package com.elster.jupiter.appserver.impl;


import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

public enum Bus {
    ;
    public static final String COMPONENTNAME = "APS";
    private static volatile ServiceLocator serviceLocator;


    public static OrmClient getOrmClient() {
        return serviceLocator.getOrmClient();
    }

    public static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }

    public static MessageService getMessageService() {
        return serviceLocator.getMessageService();
    }

    public static CronExpressionParser getCronExpressionParser() {
        return serviceLocator.getCronExpressionParser();
    }

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static FileImportService getFileImportService() {
        return serviceLocator.getFileImportService();
    }

    public static JsonService getJsonService() {
        return serviceLocator.getJsonService();
    }

    public static UserService getUserService() {
        return serviceLocator.getUserService();
    }

    public static ThreadPrincipalService getThreadPrincipalService() {
        return serviceLocator.getThreadPrincipalService();
    }
}
