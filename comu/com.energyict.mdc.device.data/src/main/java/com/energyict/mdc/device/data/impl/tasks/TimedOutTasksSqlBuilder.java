package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;

import com.elster.jupiter.util.sql.SqlBuilder;

/**
 * Provides sql building services that support cleaning marker flags on
 * ComTaskExecutions and {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s that are running
 * on {@link com.energyict.mdc.engine.model.OutboundComPort}s of a {@link com.energyict.mdc.engine.model.ComServer}
 * for a period of time that is longer than the task execution timeout specified
 * on the {@link com.energyict.mdc.engine.model.OutboundComPortPool} they are contained in.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-05-02 (16:11)
 */
public class TimedOutTasksSqlBuilder {
    /**
     * Appends sql code to the {@link SqlBuilder} that returns the unique identifier
     * of all ComTaskExecutions that are currently running
     * on a ComPort from the {@link OutboundComPortPool} for a period
     * that is longer that the timeout specified on the OutboundComPortPool.
     *  @param sqlBuilder The SqlBuilder
     * @param ComPortPool The OutboundComPortPool
     * @param now The current time in UTC seconds
     * @param timeOutSeconds The maximum number of seconds that tasks are expected to run
     */
    public static void appendTimedOutComTaskExecutionSql (SqlBuilder sqlBuilder, ComPortPool ComPortPool, long now, int timeOutSeconds) {
        sqlBuilder.append("SELECT cte.id FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " cte, ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct, " );
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONMETHOD.name());
        sqlBuilder.append(" cm ");
        sqlBuilder.append(" WHERE cte.connectiontask = ct.id");
        sqlBuilder.append("   AND ct.connectionmethod = cm.id");
        sqlBuilder.append("   AND cm.comportpool = ?");
        sqlBuilder.append("   AND cte.executionStart + ? < ?");
        sqlBuilder.addLong(ComPortPool.getId());
        sqlBuilder.addInt(timeOutSeconds);
        sqlBuilder.addLong(now);
    }

    // Hide utility class constructor
    private TimedOutTasksSqlBuilder () {}

}