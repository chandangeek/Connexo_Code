package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.util.time.Clock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides code reuse opportunities to builds SQL queries that will
 * match {@link ConnectionTask}s against a {@link ConnectionTaskFilterSpecification}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public abstract class AbstractConnectionTaskFilterSqlBuilder {

    private static final String SUCCESS_INDICATOR_ALIAS_NAME = "successindicator";
    private static final int MAX_ELEMENTS_FOR_IN_CLAUSE = 1000;

    private final Clock clock;
    private ClauseAwareSqlBuilder actualBuilder;
    private Set<ConnectionTypePluggableClass> connectionTypes;
    private Set<ComPortPool> comPortPools;
    private Set<DeviceType> deviceTypes;
    private boolean appendLastComSessionJoinClause;

    public AbstractConnectionTaskFilterSqlBuilder(ConnectionTaskFilterSpecification filterSpecification, Clock clock) {
        super();
        this.clock = clock;
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.comPortPools = new HashSet<>(filterSpecification.comPortPools);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
        this.appendLastComSessionJoinClause = filterSpecification.useLastComSession;
    }

    protected void setActualBuilder(ClauseAwareSqlBuilder actualBuilder) {
        this.actualBuilder = actualBuilder;
    }

    protected void unionAll() {
        this.actualBuilder.unionAll();
    }

    protected void append(String sql) {
        this.actualBuilder.append(sql);
    }

    protected void appendWhereOrAnd() {
        this.actualBuilder.appendWhereOrAnd();
    }

    protected void addLong (long bindValue) {
        this.actualBuilder.addLong(bindValue);
    }

    protected void appendWhereClause(ServerConnectionTaskStatus taskStatus) {
        taskStatus.completeFindBySqlBuilder(this.actualBuilder, clock);
        this.appendComPortPoolSql();
        this.appendDeviceTypeSql();
    }

    protected void appendJoinedTables() {
        if (!this.connectionTypes.isEmpty()) {
            this.append(" inner join ");
            this.append(TableSpecs.DDC_CONNECTIONMETHOD.name());
            this.append(" on ");
            this.append(TableSpecs.DDC_CONNECTIONMETHOD.name());
            this.append(".id = ");
            this.append(this.connectionTaskTableName());
            this.append(".connectionmethod and (");
            this.appendInClause(TableSpecs.DDC_CONNECTIONMETHOD.name() + ".connectiontypepluggableClass", this.connectionTypes);
            this.append(")");
        }
        if (this.requiresLastComSessionClause()) {
            this.appendLastComSessionJoinClause(this.connectionTaskTableName());
        }
    }

    protected String connectionTaskTableName() {
        return TableSpecs.DDC_CONNECTIONTASK.name();
    }

    private void appendComPortPoolSql() {
        if (!this.comPortPools.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (");
            this.appendInClause(this.connectionTaskTableName() + ".comportpool", this.comPortPools);
            this.append(")");
        }
    }

    private void appendDeviceTypeSql() {
        if (!this.deviceTypes.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (");
            this.append(this.connectionTaskTableName());
            this.append(".device in (select id from ");
            this.append(TableSpecs.DDC_DEVICE.name());
            this.append(" where ");
            this.appendInClause("devicetype", this.deviceTypes);
            this.append("))");
        }
    }

    private void appendInClause(String columnName, Set<? extends HasId> idBusinessObjects) {
        if (idBusinessObjects.size() == 1) {
            this.append(columnName);
            this.append(" = ");
            this.addLong(idBusinessObjects.iterator().next().getId());
        }
        else {
            List<List<? extends HasId>> chunksOfIdBusinessObjects = this.chopUp(idBusinessObjects);
            Iterator<List<? extends HasId>> chunkIterator = chunksOfIdBusinessObjects.iterator();
            while (chunkIterator.hasNext()) {
                List<? extends HasId> chunkOfIdBusinessObjects = chunkIterator.next();
                this.appendInSql(columnName);
                this.appendIds(chunkOfIdBusinessObjects);
                if (chunkIterator.hasNext()) {
                    this.append(") or ");
                }
            }
            this.append(")");
        }
    }

    /**
     * Chops the set of {@link HasId} into chunks of at most 1000 elements.
     *
     * @param idBusinessObjects The Set of HasId
     * @return The list of chunks
     */
    private List<List<? extends HasId>> chopUp (Set<? extends HasId> idBusinessObjects) {
        List<List<? extends HasId>> allChunks = new ArrayList<>();
        if (idBusinessObjects.size() <= MAX_ELEMENTS_FOR_IN_CLAUSE) {
            List<HasId> singleChuck = new ArrayList<>(idBusinessObjects);
            allChunks.add(singleChuck);
        }
        else {
            List<HasId> currentChunk = new ArrayList<>(MAX_ELEMENTS_FOR_IN_CLAUSE);
            allChunks.add(currentChunk);
            for (HasId hasId : idBusinessObjects) {
                if (currentChunk.size() == MAX_ELEMENTS_FOR_IN_CLAUSE) {
                    // Current chuck is full, take another one
                    currentChunk = new ArrayList<>(MAX_ELEMENTS_FOR_IN_CLAUSE);
                    allChunks.add(currentChunk);
                }
                currentChunk.add(hasId);
            }
        }
        return allChunks;
    }

    private void appendInSql(String columName) {
        this.append(columName);
        this.append(" in (");
    }

    private void appendIds(List<? extends HasId> idBusinessObjects) {
        Iterator<? extends HasId> iterator = idBusinessObjects.iterator();
        while (iterator.hasNext()) {
            HasId hasId = iterator.next();
            this.append(String.valueOf(hasId.getId()));
            if (iterator.hasNext()) {
                this.append(", ");
            }
        }
    }

    private boolean requiresLastComSessionClause() {
        return this.appendLastComSessionJoinClause;
    }

    protected void requiresLastComSessionClause(boolean flag) {
        this.appendLastComSessionJoinClause = flag;
    }

    private void appendLastComSessionJoinClause(String connectionTaskTableName) {
        this.appendLastComSessionJoinClauseForConnectionTask(
                SUCCESS_INDICATOR_ALIAS_NAME,
                connectionTaskTableName);
    }

    private void appendLastComSessionJoinClauseForConnectionTask(String successIndicatorAliasName, String connectionTaskTableName) {
        this.append(", (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(".startdate) ");
        this.append(successIndicatorAliasName);
        this.append(" from ");
        this.append(TableSpecs.DDC_COMSESSION.name());
        this.append(" group by connectiontask) cs");
        this.appendWhereOrAnd();
        this.append(connectionTaskTableName);
        this.append(".id = cs.connectiontask");
    }

}