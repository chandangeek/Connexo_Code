package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Builds the SQL query that finds all {@link ConnectionTask}s
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
        SqlBuilder sqlBuilder = dataMapper.builder(connectionTaskAliasName());
        this.setActualBuilder(new ClauseAwareSqlBuilder(sqlBuilder));
        if (   !this.isNull(this.lastSessionEnd)
            || !this.latestStatuses.isEmpty()
            || !this.latestResults.isEmpty()) {
            this.appendLastSessionClause();
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
        this.appendNonStatusWhereClauses();
        this.append(" order by lastcommunicationstart desc");
        return sqlBuilder.asPageBuilder(pageStart, pageStart + pageSize);
    }

    private boolean isNull(Interval interval) {
        return interval == null
            || (   (interval.getStart() == null)
                && (interval.getEnd() == null));
    }

    private void appendLastSessionClause() {
        this.append(" join ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(" cs on ct.lastsession = cs.id");
        this.appendWhereOrAnd();
        boolean clauseAppended = this.appendLastSessionStatusClause();
        if (!this.isNull(this.lastSessionEnd)) {
            if (clauseAppended) {
                this.append(" and ");
            }
            this.appendIntervalWhereClause("cs", "STOPDATE", this.lastSessionEnd);
        }
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
        Holder<String> separator = HolderBuilder.first("").andThen(", ");
        for (Enum value : values) {
            this.append(separator.get());
            this.append(String.valueOf(value.ordinal()));
        }
    }

    private void appendLastSessionStartWhereClause() {
        if (!this.isNull(this.lastSessionStart)) {
            this.appendWhereOrAnd();
            this.appendIntervalWhereClause(TableSpecs.DDC_CONNECTIONTASK.name(), "LASTCOMMUNICATIONSTART", this.lastSessionStart);
        }
    }

}