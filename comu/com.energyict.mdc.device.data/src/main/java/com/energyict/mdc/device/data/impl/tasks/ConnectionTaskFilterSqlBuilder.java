package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateTimeConstants;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Builds the SQL query thats finds all {@link ConnectionTask}s
 * that match a {@link ConnectionTaskFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-06 (13:39)
 */
public class ConnectionTaskFilterSqlBuilder extends AbstractConnectionTaskFilterSqlBuilder {

    private Set<ServerConnectionTaskStatus> taskStatuses;
    private Set<ConnectionTask.SuccessIndicator> successIndicators;
    public Interval lastSessionStart = null;
    public Interval lastSessionEnd = null;

    public ConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock) {
        super(filterSpecification, clock);
        this.validate(filterSpecification);
        this.taskStatuses = EnumSet.noneOf(ServerConnectionTaskStatus.class);
        for (TaskStatus taskStatus : filterSpecification.taskStatuses) {
            this.taskStatuses.add(ServerConnectionTaskStatus.forTaskStatus(taskStatus));
        }
        this.lastSessionStart = filterSpecification.lastSessionStart;
        this.lastSessionEnd = filterSpecification.lastSessionEnd;
        if (filterSpecification.successIndicators.size() == ConnectionTask.SuccessIndicator.values().length) {
            /* All SuccessIndicator so the user is interested in either no last session,
             * a successful last session or a failed last session.
             * So in fact, he does not care about the last session at all
             * as that are the only three options.
             * In that case, it is easier to use empty set as that will avoid the complex clause to get the last session. */
            this.successIndicators = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);
        }
        else {
            this.successIndicators = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);
            for (ConnectionTask.SuccessIndicator successIndicator : filterSpecification.successIndicators) {
                this.successIndicators.add(successIndicator);
            }
        }
    }

    /**
     * Validates that all specification are correct and coherent,
     * i.e. that none of the specifications contradict each other.
     *
     * @param filterSpecification The ConnectionTaskFilterSpecification
     * @throws IllegalArgumentException Thrown when the specifications are not valid
     */
    protected void validate(ConnectionTaskFilterSpecification filterSpecification) throws IllegalArgumentException {
        if (   filterSpecification.successIndicators.contains(ConnectionTask.SuccessIndicator.NOT_APPLICABLE)
            && !this.isNull(filterSpecification.lastSessionEnd)) {
            throw new IllegalArgumentException("SuccessIndicator.NOT_APPLICABLE and last session end in interval cannot be combined");
        }
    }

    public SqlBuilder build(DataMapper<ConnectionTask> dataMapper, int pageStart, int pageSize) {
        SqlBuilder sqlBuilder = dataMapper.builder(null);   // Does not generate an alias
        this.setActualBuilder(new ClauseAwareSqlBuilder(sqlBuilder));
        if (   !this.isNull(this.lastSessionEnd)
            || !this.successIndicators.isEmpty()) {
            this.appendLastSessionClause(this.connectionTaskTableName());
            this.requiresLastComSessionClause(false);
        }
        String sqlStartClause = sqlBuilder.getText();
        Iterator<ServerConnectionTaskStatus> statusIterator = this.taskStatuses.iterator();
        while (statusIterator.hasNext()) {
            this.appendWhereClause(statusIterator.next());
            if (statusIterator.hasNext()) {
                this.unionAll();
                this.append(sqlStartClause);
                this.appendJoinedTables();
            }
        }
        this.appendLastSessionStartWhereClause();
        return sqlBuilder.asPageBuilder(pageStart, pageStart + pageSize - 1);
    }

    private boolean isNull(Interval interval) {
        return interval == null
            || (   (interval.getStart() == null)
                && (interval.getEnd() == null));
    }

    private void appendLastSessionClause(String connectionTaskTableName) {
        this.append(", (select cs.connectiontask, MAX(cs.successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(" cs where ");
        this.appendLastSessionStatusClause();
        if (!this.isNull(this.lastSessionEnd)) {
            this.append(" and ");
            this.appendIntervalWhereClause("cs", "STOPDATE", this.lastSessionEnd);
        }
        this.append(" group by connectiontask) t");
        this.appendWhereOrAnd();
        this.append(connectionTaskTableName);
        this.append(".id = t.connectiontask");
    }

    private void appendLastSessionStatusClause() {
        if (!this.successIndicators.isEmpty()) {
            if (   this.successIndicators.contains(ConnectionTask.SuccessIndicator.SUCCESS)
                && this.successIndicators.contains(ConnectionTask.SuccessIndicator.FAILURE)) {
                this.append(" cs.status = 1 or cs.status = 0");
            }
            else {
                if (this.successIndicators.contains(ConnectionTask.SuccessIndicator.SUCCESS)) {
                    this.append(" cs.status = 1");
                }
                else {
                    this.append(" cs.status = 0");
                }
            }
        }
    }

    private void appendLastSessionStartWhereClause() {
        if (!this.isNull(this.lastSessionStart)) {
            this.appendWhereOrAnd();
            this.appendIntervalWhereClause(TableSpecs.DDC_CONNECTIONTASK.name(), "LASTCOMMUNICATIONSTART", this.lastSessionStart);
        }
    }

    private void appendIntervalWhereClause(String tableName, String columnName, Interval interval) {
        if (interval.getStart() != null) {
            this.append(" (");
            this.append(tableName);
            this.append(".");
            this.append(columnName);
            this.append(" >=");
            this.addLong(interval.getStart().getTime() / DateTimeConstants.MILLIS_PER_SECOND);
            if (interval.getEnd() != null) {
                this.append(" and ");
            }
        }
        if (interval.getEnd() != null) {
            if (interval.getStart() == null) {
                this.append(" (");
            }
            this.append(columnName);
            this.append(" <");
            this.addLong(interval.getEnd().getTime() / DateTimeConstants.MILLIS_PER_SECOND);
            this.append(") ");
        }
        else {
            this.append(") ");
        }
    }

}