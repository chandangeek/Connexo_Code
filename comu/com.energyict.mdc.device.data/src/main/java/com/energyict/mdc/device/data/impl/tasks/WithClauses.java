package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;

/**
 * Models commonly used "with"-clauses that will optimize a number of SQL queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-11 (16:51)
 */
public enum WithClauses {

    BUSY_COMTASK_EXECUTION("select connectiontask, comport from DDC_COMTASKEXEC where comport is not null and obsolete_date is null"),
    BUSY_CONNECTION_TASK("select id as connectiontask, lastcommunicationstart, comserver from DDC_CONNECTIONTASK where comserver is not null");

    private String withClause;

    WithClauses(String withClause) {
        this.withClause = withClause;
    }

    public ClauseAwareSqlBuilder sqlBuilder(String alias) {
        return ClauseAwareSqlBuilder.with(this.withClause, alias);
    }

}