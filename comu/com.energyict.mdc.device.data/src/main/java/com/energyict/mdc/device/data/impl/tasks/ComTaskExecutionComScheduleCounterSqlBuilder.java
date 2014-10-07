package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.util.time.Clock;

/**
 * Builds the SQL query thats counts {@link com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution}s
 * for a single {@link TaskStatus}, grouping them by the {@link com.energyict.mdc.scheduling.model.ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-03 (13:41)
 */
public class ComTaskExecutionComScheduleCounterSqlBuilder extends AbstractComTaskExecutionFilterSqlBuilder {

    private ServerComTaskStatus taskStatus;

    public ComTaskExecutionComScheduleCounterSqlBuilder(ServerComTaskStatus taskStatus, Clock clock) {
        super(clock);
        this.taskStatus = taskStatus;
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.setActualBuilder(sqlBuilder);
        this.appendSelectClause();
        this.appendFromClause();
        this.appendWhereClause();
        this.appendGroupByClause();
    }

    private void appendSelectClause() {
        this.append("select '");
        this.append(this.taskStatus.getPublicStatus().name());
        this.append("', ctincs.comschedule, count(*)");
    }

    private void appendFromClause() {
        this.append(" from ");
        this.append(TableSpecs.DDC_COMTASKEXEC.name());
        this.append(" ");
        this.append(communicationTaskAliasName());
        this.append(" join sch_comschedule cs on cte.comschedule = cs.id");
        this.append(" join sch_comtaskincomschedule ctincs on ctincs.comschedule = cs.id");
    }

    private void appendWhereClause() {
        this.appendWhereOrAnd();
        this.append("cte.discriminator = ");
        this.append(ComTaskExecutionImpl.SHARED_SCHEDULE_COM_TASK_EXECUTION_DISCRIMINATOR);
        this.appendWhereClause(this.taskStatus);
    }

    private void appendGroupByClause() {
        this.append(" group by ctincs.comschedule");
    }

}