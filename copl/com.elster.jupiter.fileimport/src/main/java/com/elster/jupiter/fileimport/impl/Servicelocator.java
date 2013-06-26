package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.log.LogService;

/**
 * Copyrights EnergyICT
 * Date: 24/06/13
 * Time: 11:37
 */
public interface ServiceLocator {

    LogService getLogService();

    MessageService getMessageService();

    CronExpressionParser getCronExpressionParser();

    OrmClient getOrmClient();

    Clock getClock();
}
