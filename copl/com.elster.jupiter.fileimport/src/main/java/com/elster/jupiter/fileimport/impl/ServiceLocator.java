package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.log.LogService;

public interface ServiceLocator {

    LogService getLogService();

    MessageService getMessageService();

    CronExpressionParser getCronExpressionParser();

    OrmClient getOrmClient();

    Clock getClock();

    TransactionService getTransactionService();
}
