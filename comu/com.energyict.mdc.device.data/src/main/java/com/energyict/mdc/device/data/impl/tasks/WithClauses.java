package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;

/**
 * Models commonly used "with"-clauses that will optimize a number of SQL queries.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-11 (16:51)
 */
public enum WithClauses {

    COMTASK_EXECUTION_WITH_DEVICE_STATE("select * from DDC_COMTASKEXEC cte join DDC_DEVICE dev on cte.device = dev.id join enddevices kd on dev.meterid = kd.id"),
    BUSY_COMTASK_EXECUTION("select connectiontask from DDC_COMTASKEXEC where comport is not null and obsolete_date is null"),
    BUSY_CONNECTION_TASK("select id as connectiontask from DDC_CONNECTIONTASK where comserver is not null");

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