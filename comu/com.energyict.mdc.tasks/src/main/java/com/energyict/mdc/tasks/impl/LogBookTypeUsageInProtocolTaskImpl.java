/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.LogBooksTask;

import java.time.Instant;

public class LogBookTypeUsageInProtocolTaskImpl implements LogBookTypeUsageInProtocolTask {

    enum Fields {
        LOGBOOK_TASK_REFERENCE("logBooksTaskReference"),
        LOGBOOK_TYPE_REFERENCE("logBookTypeReference");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<LogBooksTask> logBooksTaskReference = ValueReference.absent();
    private Reference<LogBookType> logBookTypeReference = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Override
    public LogBooksTask getLogBooksTask() {
        return this.logBooksTaskReference.get();
    }

    @Override
    public void setLogBooksTask(LogBooksTask logBooksTask) {
        this.logBooksTaskReference.set(logBooksTask);
    }

    @Override
    public LogBookType getLogBookType() {
        return logBookTypeReference.get();
    }

    @Override
    public void setLogBookType(LogBookType logBookType) {
        this.logBookTypeReference.set(logBookType);
    }

}