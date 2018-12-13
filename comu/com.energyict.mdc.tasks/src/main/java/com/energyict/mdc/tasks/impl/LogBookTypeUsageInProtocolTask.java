/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.LogBooksTask;

public interface LogBookTypeUsageInProtocolTask {

    LogBooksTask getLogBooksTask();

    void setLogBooksTask(LogBooksTask logBooksTask);

    LogBookType getLogBookType();

    void setLogBookType(LogBookType logBookType);
}
