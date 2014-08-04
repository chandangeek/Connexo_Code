package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.SuccessIndicator;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Builds the SQL query thats counts {@link ConnectionTask}s
 * that match this ConnectionTaskFilter for a single {@link TaskStatus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (17:22)
 */
public class ConnectionTaskFilterSqlBuilder {

    private static final String COM_TASK_SESSION_ALIAS_NAME = "ctes";
    private static final String SUCCESS_INDICATOR_ALIAS_NAME = "successindicator";
    private static final int MAX_ELEMENTS_FOR_IN_CLAUSE = 1000;
    private static final String COMSESSION_TABLENAME = "THS_COMSESSION";
    private static final String DEVICE_CONFIGURATION_TABLENAME = "DTC_DEVICECONFIG";

    private ServerConnectionTaskStatus taskStatus;
    private Set<ConnectionTypePluggableClass> connectionTypes;
    private Set<ComPortPool> comPortPools;
    private Set<DeviceType> deviceTypes;
    private Set<SuccessIndicator> successIndicators;

    public ConnectionTaskFilterSqlBuilder(ServerConnectionTaskStatus taskStatus, ConnectionTaskFilterSpecification filterSpecification) {
        super();
        this.taskStatus = taskStatus;
        this.connectionTypes = new HashSet<>(filterSpecification.connectionTypes);
        this.comPortPools = new HashSet<>(filterSpecification.comPortPools);
        this.deviceTypes = new HashSet<>(filterSpecification.deviceTypes);
        this.successIndicators = EnumSet.copyOf(filterSpecification.successIndicators);
    }

    public void appendTo(ClauseAwareSqlBuilder sqlBuilder) {
        this.appendSelectClause(sqlBuilder);
        this.appendFromClause(sqlBuilder);
        this.appendWhereClause(sqlBuilder);
    }

    private void appendSelectClause(ClauseAwareSqlBuilder sqlBuilder) {
        sqlBuilder.append("select '");
        sqlBuilder.append(this.taskStatus.getPublicStatus().name());
        sqlBuilder.append("', count(*)");
    }

    private void appendFromClause(ClauseAwareSqlBuilder sqlBuilder) {
        sqlBuilder.append(" from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        this.appendJoinedTables(sqlBuilder);
    }

    private void appendWhereClause(ClauseAwareSqlBuilder sqlBuilder) {
        this.taskStatus.completeFindBySqlBuilder(sqlBuilder);
        this.appendCompletionCodesSql(sqlBuilder);
        this.appendComPortPoolSql(sqlBuilder);
        this.appendDeviceTypeSql(sqlBuilder);
    }

    private void appendJoinedTables(ClauseAwareSqlBuilder sqlBuilder) {
        if (!this.connectionTypes.isEmpty()) {
            sqlBuilder.append(" inner join ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONMETHOD.name());
            sqlBuilder.append(" on ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONMETHOD.name());
            sqlBuilder.append(".id = ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".connectionmethod and (");
            this.appendInClause(TableSpecs.DDC_CONNECTIONMETHOD.name() + ".connectiontypepluggableClass", sqlBuilder, this.connectionTypes);
            sqlBuilder.append(")");
        }
        if (this.requiresCompletionCodesClause()) {
            this.appendCompletionCodeJoinClause(sqlBuilder, TableSpecs.DDC_CONNECTIONTASK.name());
        }
    }

    private void appendComPortPoolSql(ClauseAwareSqlBuilder sqlBuilder) {
        if (!this.comPortPools.isEmpty()) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append(" (");
            this.appendInClause(TableSpecs.DDC_CONNECTIONTASK.name() + ".comportpool", sqlBuilder, this.comPortPools);
            sqlBuilder.append(")");
        }
    }

    private void appendDeviceTypeSql(ClauseAwareSqlBuilder sqlBuilder) {
        if (!this.deviceTypes.isEmpty()) {
            sqlBuilder.appendWhereOrAnd();
            sqlBuilder.append(" (");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(".device in (select id from ");
            sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
            sqlBuilder.append(" where ");
            this.appendInClause("devicetype", sqlBuilder, this.deviceTypes);
            sqlBuilder.append("))");
        }
    }

    private void appendInClause(String columnName, ClauseAwareSqlBuilder sqlBuilder, Set<? extends HasId> idBusinessObjects) {
        if (idBusinessObjects.size() == 1) {
            sqlBuilder.append(columnName);
            sqlBuilder.append(" = ");
            sqlBuilder.addLong(idBusinessObjects.iterator().next().getId());
        }
        else {
            List<List<? extends HasId>> chunksOfIdBusinessObjects = this.chopUp(idBusinessObjects);
            Iterator<List<? extends HasId>> chunkIterator = chunksOfIdBusinessObjects.iterator();
            while (chunkIterator.hasNext()) {
                List<? extends HasId> chunkOfIdBusinessObjects = chunkIterator.next();
                this.appendInSql(columnName, sqlBuilder);
                this.appendIds(sqlBuilder, chunkOfIdBusinessObjects);
                if (chunkIterator.hasNext()) {
                    sqlBuilder.append(") or ");
                }
            }
            sqlBuilder.append(")");
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

    private void appendInSql(String columName, ClauseAwareSqlBuilder sqlBuilder) {
        sqlBuilder.append(columName);
        sqlBuilder.append(" in (");
    }

    private void appendIds(ClauseAwareSqlBuilder sqlBuilder, List<? extends HasId> idBusinessObjects) {
        Iterator<? extends HasId> iterator = idBusinessObjects.iterator();
        while (iterator.hasNext()) {
            HasId hasId = iterator.next();
            sqlBuilder.append(String.valueOf(hasId.getId()));
            if (iterator.hasNext()) {
                sqlBuilder.append(", ");
            }
        }
    }

    private boolean requiresCompletionCodesClause() {
        return !this.successIndicators.isEmpty();
    }

    private void appendCompletionCodeJoinClause(ClauseAwareSqlBuilder sqlBuilder, String connectionTaskTableName) {
        this.appendLastSessionJoinClauseForConnectionTask(
                        sqlBuilder,
                        COM_TASK_SESSION_ALIAS_NAME,
                        SUCCESS_INDICATOR_ALIAS_NAME,
                        connectionTaskTableName);
    }

    private void appendLastSessionJoinClauseForConnectionTask(ClauseAwareSqlBuilder sqlBuilder, String comTaskSessionAliasName, String successIndicatorAliasName, String connectionTaskTableName) {
        sqlBuilder.append(", (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY ");
        sqlBuilder.append(COMSESSION_TABLENAME);
        sqlBuilder.append(".startdate) ");
        sqlBuilder.append(successIndicatorAliasName);
        sqlBuilder.append(" from ");
        sqlBuilder.append(COMSESSION_TABLENAME);
        sqlBuilder.append(" group by connectiontask) ");
        sqlBuilder.append(comTaskSessionAliasName);
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append(connectionTaskTableName);
        sqlBuilder.append(".id = ");
        sqlBuilder.append(comTaskSessionAliasName);
        sqlBuilder.append(".connectiontask");
    }
    private void appendCompletionCodesSql(ClauseAwareSqlBuilder sqlBuilder) {
        if (this.requiresCompletionCodesClause()) {
            this.appendCompletionCodeClause(sqlBuilder, this.successIndicators);
        }
    }

    private void appendCompletionCodeClause(ClauseAwareSqlBuilder sqlBuilder, Set<SuccessIndicator> includedSuccessIndicators) {
        sqlBuilder.append(" and ");
        sqlBuilder.append(COM_TASK_SESSION_ALIAS_NAME);
        sqlBuilder.append(".");
        sqlBuilder.append(SUCCESS_INDICATOR_ALIAS_NAME);
        sqlBuilder.append(" in (");
        boolean notFirst = false;
        for (SuccessIndicator successIndicator : includedSuccessIndicators) {
            if (notFirst) {
                sqlBuilder.append(", ");
            }
            sqlBuilder.addInt(successIndicator.ordinal());
            notFirst = true;
        }
        sqlBuilder.append(")");
    }

}