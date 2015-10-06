package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.time.Interval;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    protected void append(CharSequence sql) {
        this.actualBuilder.append(sql.toString());
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

    protected <T> void appendInClause(String columnName, Collection<T> objects, Function<T, ? extends CharSequence> objectMapper) {
        if (objects.size() == 1) {
            this.append(columnName);
            this.append(" = ");
            this.append(objectMapper.apply(objects.iterator().next()));
        } else {
            this.append(DecoratedStream.decorate(objects.stream())
                    .partitionPer(MAX_ELEMENTS_FOR_IN_CLAUSE)
                    .map(chunk -> chunk.stream().filter(Objects::nonNull).map(objectMapper).collect(Collectors.joining(", ")))
                    .collect(Collectors.joining(") OR " + columnName + " IN (" , columnName + " IN (", ") ")));
        }
    }

    protected <T extends HasId> void appendInClause(String columnName, Set<T> idBusinessObjects) {
        this.appendInClause(columnName, idBusinessObjects, obj -> String.valueOf(obj.getId()));
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

    protected void appendDeviceInStateSql(String targetTableName, Collection<DefaultState> allowedDeviceStates) {
        DeviceStateSqlBuilder deviceStateSqlBuilder;
        if (allowedDeviceStates.isEmpty()){
            deviceStateSqlBuilder = DeviceStateSqlBuilder.forDefaultExcludedStates(targetTableName);
        }
        else {
            EnumSet<DefaultState> states = EnumSet.noneOf(DefaultState.class);
            states.addAll(allowedDeviceStates);
            deviceStateSqlBuilder = DeviceStateSqlBuilder.includeStates(targetTableName, states);
        }
        this.appendWhereOrAnd();
        this.append(" (");
        deviceStateSqlBuilder.appendRestrictedStatesCondition(this.actualBuilder.toSqlWhereBuilder(), this.clock);
        this.append(")");
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