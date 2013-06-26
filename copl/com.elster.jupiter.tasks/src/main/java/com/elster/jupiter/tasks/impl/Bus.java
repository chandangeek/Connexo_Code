package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "TSK";

    private static final Logger LOGGER = initLogger();

    private static Logger initLogger() {
        Logger logger = Logger.getLogger(COMPONENTNAME);
        logger.setLevel(Level.ALL);
        return logger;
    }

    private static volatile ServiceLocator serviceLocator;

    public static Clock getClock() {
        return serviceLocator.getClock();
    }

    public static void setServiceLocator(final ServiceLocator serviceLocator) {
        if (serviceLocator == null) {
            for (Handler handler : LOGGER.getHandlers()) {
                LOGGER.removeHandler(handler);
            }
        } else {
            LOGGER.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    serviceLocator.getLogService().log(record.getLevel().intValue(), record.getMessage(), record.getThrown());
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }
            });
        }
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

    public static Logger getLogger() {
        return LOGGER;
    }

    public static CronExpressionParser getCronExpressionParser() {
        return serviceLocator.getCronExpressionParser();
    }

    public static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }
}
