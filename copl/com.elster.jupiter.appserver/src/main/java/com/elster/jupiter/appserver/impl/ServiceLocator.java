package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.service.log.LogService;

public interface ServiceLocator {

    OrmClient getOrmClient();

    TransactionService getTransactionService();

    MessageService getMessageService();

    CronExpressionParser getCronExpressionParser();

    LogService getLogService();

    FileImportService getFileImportService();

    JsonService getJsonService();

    UserService getUserService();
}
