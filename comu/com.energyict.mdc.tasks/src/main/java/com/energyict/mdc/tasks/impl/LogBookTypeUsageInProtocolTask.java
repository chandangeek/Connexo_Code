package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.LogBooksTask;

/**
 * Copyrights EnergyICT
 * Date: 13/05/14
 * Time: 14:28
 */
public interface LogBookTypeUsageInProtocolTask {

    LogBooksTask getLogBooksTask();

    void setLogBooksTask(LogBooksTask logBooksTask);

    LogBookType getLogBookType();

    void setLogBookType(LogBookType logBookType);
}
