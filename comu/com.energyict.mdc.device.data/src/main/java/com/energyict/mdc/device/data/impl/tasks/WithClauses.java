/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;

/**
 * Models commonly used "with"-clauses that will optimize a number of SQL queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-11 (16:51)
 */
public enum WithClauses {

    COMTASK_EXECUTION_WITH_DEVICE_STATE("select /* +INLINE */" +
            "       cte.obsolete_date, cte.nextExecutionTimestamp, cte.lastExecutionTimestamp, cte.lastSuccessfulCompletion, cte.currentretrycount, cte.lastExecutionFailed, " +
            "       cte.comport, cte.onhold, cte.device," +
            "       ct.id as thereisabusytask " +
            "  from " + TableSpecs.DDC_COMTASKEXEC.name() + " cte " +
            "  join " + TableSpecs.DDC_DEVICE.name() + " dev on cte.device = dev.id " +
            "  join enddevices kd on dev.meterid = kd.id " +
            "  LEFT OUTER JOIN " + TableSpecs.DDC_CONNECTIONTASK.name() + " ct on ct.id = cte.connectiontask AND ct.comserver IS NOT NULL and ct.lastCommunicationStart > cte.nextExecutionTimestamp" +
            " where cte.obsolete_date is null"),
    BUSY_COMTASK_EXECUTION("select connectiontask, comport from " + TableSpecs.DDC_COMTASKEXEC.name() + " where comport is not null and obsolete_date is null"),
    BUSY_CONNECTION_TASK("select id as connectiontask, lastcommunicationstart, comserver from " + TableSpecs.DDC_CONNECTIONTASK.name() + " where comserver is not null");

    private String withClause;

    WithClauses(String withClause) {
        this.withClause = withClause;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder, String alias) {
        sqlBuilder.appendWith(this.withClause, alias);
    }

    public ClauseAwareSqlBuilder sqlBuilder(String alias) {
        return ClauseAwareSqlBuilder.with(this.withClause, alias);
    }

}