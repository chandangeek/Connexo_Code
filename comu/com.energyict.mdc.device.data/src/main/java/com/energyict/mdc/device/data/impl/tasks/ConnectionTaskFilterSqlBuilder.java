package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;

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

    public ConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock) {
        super(filterSpecification, clock);
        this.taskStatuses = EnumSet.noneOf(ServerConnectionTaskStatus.class);
        for (TaskStatus taskStatus : filterSpecification.taskStatuses) {
            this.taskStatuses.add(ServerConnectionTaskStatus.forTaskStatus(taskStatus));
        }
    }

    public SqlBuilder build(DataMapper<ConnectionTask> dataMapper) {
        SqlBuilder sqlBuilder = dataMapper.builder(null);   // Does not generate an alias
        String sqlStartClause = sqlBuilder.getText();
        this.setActualBuilder(new ClauseAwareSqlBuilder(sqlBuilder));
        Iterator<ServerConnectionTaskStatus> statusIterator = this.taskStatuses.iterator();
        while (statusIterator.hasNext()) {
            this.appendWhereClause(statusIterator.next());
            if (statusIterator.hasNext()) {
                this.unionAll();
                this.append(sqlStartClause);
                this.appendJoinedTables();
            }
        }
        return sqlBuilder;
    }

}