/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.impl.TableSpecs;

/**
 * Provides sql building services that support cleaning marker flags on
 * ComTaskExecutions and {@link ConnectionTask}s that are running
 * on {@link OutboundComPort}s of a {@link ComServer}
 * for a period of time that is longer than the task execution timeout specified
 * on the {@link OutboundComPortPool} they are contained in.
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
     *
     * @param sqlBuilder     The SqlBuilder
     * @param comPortPool    The OutboundComPortPool
     * @param now            The current time in UTC seconds
     * @param timeOutSeconds The maximum number of seconds that tasks are expected to run
     */
    public static void appendTimedOutComTaskExecutionSql(SqlBuilder sqlBuilder, ComPortPool comPortPool, long now, int timeOutSeconds) {
        sqlBuilder.append("SELECT cte.id FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " cte, ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct ");
        sqlBuilder.append(" WHERE cte.connectiontask = ct.id");
        sqlBuilder.append("   AND cte.COMPORT is not null");
        sqlBuilder.append("   AND ct.comportpool = ");
        sqlBuilder.addLong(comPortPool.getId());
        sqlBuilder.append("   AND ct.lastcommunicationstart + ");
        sqlBuilder.addInt(timeOutSeconds);
        sqlBuilder.append(" < ");
        sqlBuilder.addLong(now);
    }

    // Hide utility class constructor
    private TimedOutTasksSqlBuilder() {
    }

}