package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.Interval;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Provides code reuse opportunities to builds SQL queries that will
 * match tasks against a filter specifications.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-22 (11:25)
 */
public abstract class AbstractTaskFilterSqlBuilder {

    protected static final int MAX_ELEMENTS_FOR_IN_CLAUSE = 1000;

    private final Clock clock;
    private ClauseAwareSqlBuilder actualBuilder;

    public AbstractTaskFilterSqlBuilder(Clock clock) {
        super();
        this.clock = clock;
    }

    protected Clock getClock() {
        return clock;
    }

    protected ClauseAwareSqlBuilder getActualBuilder() {
        return actualBuilder;
    }

    protected void setActualBuilder(ClauseAwareSqlBuilder actualBuilder) {
        this.actualBuilder = actualBuilder;
    }

    protected void unionAll() {
        this.actualBuilder.unionAll();
    }

    protected void append(SqlFragment sqlFragment) {
        this.actualBuilder.append(sqlFragment);
    }

    protected void append(String sql) {
        this.actualBuilder.append(sql);
    }

    protected void appendWhereOrAnd() {
        this.actualBuilder.appendWhereOrAnd();
    }

    protected void addString(String bindValue) {
        this.actualBuilder.addObject(bindValue);
    }

    protected void addInt(int bindValue) {
        this.actualBuilder.addInt(bindValue);
    }

    protected void addLong(long bindValue) {
        this.actualBuilder.addLong(bindValue);
    }

    protected String connectionTaskAliasName() {
        return "ct";
    }

    protected String communicationTaskAliasName() {
        return "cte";
    }

    protected void appendInClause(String columnName, Set<? extends HasId> idBusinessObjects) {
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
    protected List<List<? extends HasId>> chopUp(Set<? extends HasId> idBusinessObjects) {
        return Chopper.chopUp(idBusinessObjects).into(MAX_ELEMENTS_FOR_IN_CLAUSE);
    }

    protected void appendInSql(String columName) {
        this.append(columName);
        this.append(" in (");
    }

    protected void appendIds(Collection<? extends HasId> idBusinessObjects) {
        Iterator<? extends HasId> iterator = idBusinessObjects.iterator();
        while (iterator.hasNext()) {
            HasId hasId = iterator.next();
            this.append(String.valueOf(hasId.getId()));
            if (iterator.hasNext()) {
                this.append(", ");
            }
        }
    }

    protected void appendDeviceTypeSql(String targetTableName, Set<DeviceType> deviceTypes) {
        if (!deviceTypes.isEmpty()) {
            this.appendWhereOrAnd();
            this.append(" (");
            this.append(targetTableName);
            this.append(".device in (select id from ");
            this.append(TableSpecs.DDC_DEVICE.name());
            this.append(" where ");
            this.appendInClause("devicetype", deviceTypes);
            this.append("))");
        }
    }

    protected void appendDeviceInGroupSql(List<EndDeviceGroup> deviceGroups, QueryExecutor<Device> queryExecutor, String baseEntityAliasName) {
        if (!deviceGroups.isEmpty()) {
            this.appendWhereOrAnd();
            this.append("(");
            Iterator<EndDeviceGroup> iterator = deviceGroups.iterator();
            while (iterator.hasNext()) {
                EndDeviceGroup deviceGroup = iterator.next();
                this.append(baseEntityAliasName);
                this.append(".device in (");
                if (deviceGroup instanceof QueryEndDeviceGroup) {
                    this.append(queryExecutor.asFragment(((QueryEndDeviceGroup)deviceGroup).getCondition(), "id"));
                } else {
                    this.append(((EnumeratedEndDeviceGroup)deviceGroup).getAmrIdSubQuery().toFragment());
                }
                this.append(")");
                if (iterator.hasNext()) {
                    this.append(" or ");
                }
            }
            this.append(")");
        }
    }

    protected void appendIntervalWhereClause(String tableName, String columnName, Interval interval, IntervalBindStrategy intervalBindStrategy) {
        if (interval.getStart() != null) {
            this.append(" (");
            this.append(tableName);
            this.append(".");
            this.append(columnName);
            this.append(" >=");
            this.addLong(intervalBindStrategy.toLong(interval.getStart()));
            if (interval.getEnd() != null) {
                this.append(" and ");
            }
        }
        if (interval.getEnd() != null) {
            if (interval.getStart() == null) {
                this.append(" (");
            }
            this.append(tableName);
            this.append(".");
            this.append(columnName);
            this.append(" <");
            this.addLong(intervalBindStrategy.toLong(interval.getEnd()));
            this.append(") ");
        }
        else {
            this.append(") ");
        }
    }

    protected enum IntervalBindStrategy {
        MILLIS {
            @Override
            protected long toLong(Instant intervalDate) {
                return intervalDate.toEpochMilli();
            }
        },

        SECONDS {
            @Override
            protected long toLong(Instant intervalDate) {
                return intervalDate.getEpochSecond();
            }
        };

        protected abstract long toLong(Instant intervalDate);
    }

}