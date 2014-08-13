package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;

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
    private Set<ConnectionTask.SuccessIndicator> latestStatuses;
    private Set<ComSession.SuccessIndicator> latestResults;
    public Interval lastSessionStart = null;
    public Interval lastSessionEnd = null;

    public ConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock) {
        super(filterSpecification, clock);
        this.validate(filterSpecification);
        this.copyTaskStatuses(filterSpecification);
        this.lastSessionStart = filterSpecification.lastSessionStart;
        this.lastSessionEnd = filterSpecification.lastSessionEnd;
        this.copyLatestStatuses(filterSpecification);
        this.copyLatestResults(filterSpecification);
    }

    private void copyTaskStatuses(ConnectionTaskFilterSpecification filterSpecification) {
        this.taskStatuses = EnumSet.noneOf(ServerConnectionTaskStatus.class);
        for (TaskStatus taskStatus : filterSpecification.taskStatuses) {
            this.taskStatuses.add(ServerConnectionTaskStatus.forTaskStatus(taskStatus));
        }
    }

    private void copyLatestStatuses(ConnectionTaskFilterSpecification filterSpecification) {
        if (filterSpecification.latestStatuses.size() == ConnectionTask.SuccessIndicator.values().length) {
            /* All SuccessIndicator so the user is interested in either no last session,
             * a successful last session or a failed last session.
             * So in fact, he does not care about the last session at all
             * as that are the only three options.
             * In that case, it is easier to use empty set as that will avoid the complex clause to get the last session. */
            this.latestStatuses = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);
        }
        else {
            this.latestStatuses = EnumSet.noneOf(ConnectionTask.SuccessIndicator.class);
            for (ConnectionTask.SuccessIndicator successIndicator : filterSpecification.latestStatuses) {
                this.latestStatuses.add(successIndicator);
            }
        }
    }

    private void copyLatestResults(ConnectionTaskFilterSpecification filterSpecification) {
        if (filterSpecification.latestResults.size() == ComSession.SuccessIndicator.values().length) {
            /* All SuccessIndicator so the user is interested in any type of last session.
             * So in fact, he only cares about the fact that there is a last session. */
            this.latestResults = EnumSet.noneOf(ComSession.SuccessIndicator.class);
            this.requiresLastComSessionClause(true);
        }
        else {
            this.latestResults = EnumSet.noneOf(ComSession.SuccessIndicator.class);
            for (ComSession.SuccessIndicator successIndicator : filterSpecification.latestResults) {
                this.latestResults.add(successIndicator);
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
        if (   filterSpecification.latestStatuses.contains(ConnectionTask.SuccessIndicator.NOT_APPLICABLE)
            && !this.isNull(filterSpecification.lastSessionEnd)) {
            throw new IllegalArgumentException("SuccessIndicator.NOT_APPLICABLE and last session end in interval cannot be combined");
        }
    }

    public SqlBuilder build(DataMapper<ConnectionTask> dataMapper, int pageStart, int pageSize) {
        SqlBuilder sqlBuilder = dataMapper.builder(null);   // Does not generate an alias
        this.setActualBuilder(new ClauseAwareSqlBuilder(sqlBuilder));
        if (   !this.isNull(this.lastSessionEnd)
            || !this.latestStatuses.isEmpty()
            || !this.latestResults.isEmpty()) {
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
        boolean clauseAppended = this.appendLastSessionStatusClause();
        if (!this.isNull(this.lastSessionEnd)) {
            if (clauseAppended) {
                this.append(" and ");
            }
            this.appendIntervalWhereClause("cs", "STOPDATE", this.lastSessionEnd);
        }
        this.append(" group by connectiontask) t");
        this.appendWhereOrAnd();
        this.append(connectionTaskTableName);
        this.append(".id = t.connectiontask");
    }

    private boolean appendLastSessionStatusClause() {
        boolean result = false;
        if (!this.latestStatuses.isEmpty()) {
            this.append(" cs.status in (");
            this.appendEnumValues(this.latestStatuses);
            this.append(")");
            result = true;
        }
        if (!this.latestResults.isEmpty()) {
            if (!this.latestStatuses.isEmpty()) {
                this.append(" and ");
            }
            this.append(" cs.successIndicator in (");
            this.appendEnumValues(this.latestResults);
            this.append(")");
            result = true;
        }
        return result;
    }

    private void appendEnumValues(Set<? extends Enum> values) {
        ListAppendMode appendMode = ListAppendMode.FIRST;
        for (Enum value : values) {
            appendMode.startOn(this);
            this.append(String.valueOf(value.ordinal()));
            appendMode = ListAppendMode.REMAINING;
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

    private enum ListAppendMode {
        FIRST {
            @Override
            protected void startOn (ConnectionTaskFilterSqlBuilder builder) {
                // Nothing to append before the first message
            }
        },
        REMAINING {
            @Override
            protected void startOn (ConnectionTaskFilterSqlBuilder builder) {
                builder.append(", ");
            }
        };

        protected abstract void startOn (ConnectionTaskFilterSqlBuilder sqlBuilder);

    }

}