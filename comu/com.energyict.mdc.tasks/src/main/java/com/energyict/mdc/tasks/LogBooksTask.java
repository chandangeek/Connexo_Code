/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.energyict.mdc.masterdata.LogBookType;

import java.util.List;

/**
 * Models the {@link com.energyict.mdc.tasks.ProtocolTask} which can read one or multiple Logbooks
 * from a Device.
 * <p>
 * The task can contain an optional list of {@link LogBookType logBookTypes},
 * which means only these LogbookTypes (if defined on the Device) will be fetched
 * from the Device. If no types is provided, then <b>all</b> defined @link LogBookType logBookTypes} on the Device
 * will be fetched.
 * </p>
 *
 * @author gna
 * @since 19/04/12 - 15:03
 */
public interface LogBooksTask extends ProtocolTask {

    /**
     * Return a list of LogbookTypes which need to be fetched during this task.
     * If no types are defined, then an empty list will be returned.
     *
     * @return the list of LogbookTypes
     *
     */
    public List<LogBookType> getLogBookTypes();
    public void setLogBookTypes(List<LogBookType> logBookTypes);

    interface LogBooksTaskBuilder {
        public LogBooksTaskBuilder logBookTypes(List<LogBookType> logBookTypes);
        public LogBooksTask add();
    }
}