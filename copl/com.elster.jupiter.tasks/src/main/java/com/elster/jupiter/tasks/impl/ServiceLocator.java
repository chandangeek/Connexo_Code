package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.log.LogService;

public interface ServiceLocator {

    Clock getClock();

    MessageService getMessageService();

    OrmClient getOrmClient();

    LogService getLogService();

    QueryService getQueryService();

    TransactionService getTransactionService();
}
