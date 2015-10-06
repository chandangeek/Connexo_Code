package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.time.Clock;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Append sql conditions to a SqlBuilder that will include or exclude
 * results that relate to devices that are in a specified state.
 * The sql that is produced will be similar to (exclude case):
 * <pre><code>
 * alias.device IN (
 *   SELECT ED.amrid
 *     FROM MTR_ENDDEVICESTATUS ES,
 *          (SELECT FS.ID
 *             FROM FSM_STATE FS
 *            WHERE FS.OBSOLETE_TIMESTAMP IS NULL
 *              AND FS.NAME NOT IN ('dlc.default.inStock', 'dlc.default.decommissioned')) FS,
 *          MTR_ENDDEVICE ED
 *    WHERE ES.STARTTIME <= 1436517667000
 *      AND ES.ENDTIME > 1436517667000
 *      AND ED.ID = ES.ENDDEVICE
 *      AND ES.STATE = FS.ID)
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-30 (10:23)
 */
@LiteralSql
public class DeviceStateSqlBuilder {

    private final String alias;
    private final Set<DefaultState> states;
    private final SetStrategy setStrategy;

    public static DeviceStateSqlBuilder forDefaultExcludedStates(String alias) {
        return new DeviceStateSqlBuilder(alias, SetStrategy.EXCLUDE_MULTIPLE, EnumSet.of(DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED));
    }

    public static DeviceStateSqlBuilder includeStates(String alias, Set<DefaultState> state) {
        SetStrategy setStrategy;
        if (state.size() == 1) {
            setStrategy = SetStrategy.INCLUDE_ONE;
        }
        else {
            setStrategy = SetStrategy.INCLUDE_MULTIPLE;
        }
        return new DeviceStateSqlBuilder(alias, setStrategy, state);
    }

    private DeviceStateSqlBuilder(String alias, SetStrategy setStrategy, Set<DefaultState> states) {
        super();
        this.alias = alias;
        this.states = states;
        this.setStrategy = setStrategy;
    }

    public void appendRestrictedStatesCondition(SqlBuilder sqlBuilder, Clock clock) {
        this.appendRestrictedStatesCondition(sqlBuilder, clock.instant().toEpochMilli());
    }

    public void appendRestrictedStatesCondition(SqlBuilder sqlBuilder, long timeInMillis) {
        sqlBuilder.append(" and ");
        sqlBuilder.append(this.alias);
        sqlBuilder.append(".device in (");
        sqlBuilder.append("select ED.amrid");
        sqlBuilder.append("  from MTR_ENDDEVICESTATUS ES,");
        sqlBuilder.append("       (select FS.ID from FSM_STATE FS where FS.OBSOLETE_TIMESTAMP IS NULL and FS.NAME ");
        this.setStrategy.append(sqlBuilder, this.states);
        sqlBuilder.append(") FS, MTR_ENDDEVICE ED where ES.STARTTIME <= ");
        sqlBuilder.addLong(timeInMillis);
        sqlBuilder.append(" and ES.ENDTIME > ");
        sqlBuilder.addLong(timeInMillis);
        sqlBuilder.append(" and ED.ID = ES.ENDDEVICE and ES.STATE = FS.ID)");
    }

    public int appendRestrictedStatesCondition(StringBuilder sqlBuilder) {
        sqlBuilder.append(" and ");
        sqlBuilder.append(this.alias);
        sqlBuilder.append(".device in (");
        sqlBuilder.append("select ED.amrid");
        sqlBuilder.append("  from MTR_ENDDEVICESTATUS ES,");
        sqlBuilder.append("       (select FS.ID from FSM_STATE FS where FS.OBSOLETE_TIMESTAMP IS NULL and FS.NAME ");
        this.setStrategy.append(sqlBuilder, this.states);
        sqlBuilder.append(") FS, MTR_ENDDEVICE ED");
        sqlBuilder.append(" where ES.STARTTIME <= ?");
        sqlBuilder.append("   and ES.ENDTIME > ?");
        sqlBuilder.append("   and ED.ID = ES.ENDDEVICE");
        sqlBuilder.append("   and ES.STATE = FS.ID)");
        return 2;
    }

    private enum SetStrategy {
        INCLUDE_MULTIPLE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("in (");
                appendStates(sqlBuilder, states);
                sqlBuilder.append(")");
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("in (");
                appendStates(sqlBuilder, states);
                sqlBuilder.append(")");
            }
        },

        EXCLUDE_MULTIPLE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("not in (");
                appendStates(sqlBuilder, states);
                sqlBuilder.append(")");
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("not in (");
                appendStates(sqlBuilder, states);
                sqlBuilder.append(")");
            }
        },

        INCLUDE_ONE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("= ");
                appendStates(sqlBuilder, states);
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("= ");
                appendStates(sqlBuilder, states);
            }
        },

        EXCLUDE_ONE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("<> ");
                appendStates(sqlBuilder, states);
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<DefaultState> states) {
                sqlBuilder.append("<> ");
                appendStates(sqlBuilder, states);
            }
        };

        abstract void append(SqlBuilder sqlBuilder, Set<DefaultState> states);

        abstract void append(StringBuilder sqlBuilder, Set<DefaultState> states);

        protected void appendStates(SqlBuilder sqlBuilder, Set<DefaultState> states) {
            if (states.size() == 1) {
                sqlBuilder.append("'");
                sqlBuilder.append(states.iterator().next().getKey());
                sqlBuilder.append("'");
            }
            else {
                sqlBuilder.append(
                        states
                            .stream()
                            .map(state -> "'" + state.getKey() + "'")
                            .collect(Collectors.joining(", ")));
            }
        }

        protected void appendStates(StringBuilder sqlBuilder, Set<DefaultState> states) {
            if (states.size() == 1) {
                sqlBuilder.append("'").append(states.iterator().next().getKey()).append("'");
            }
            else {
                sqlBuilder.append(
                        states
                            .stream()
                            .map(state -> "'" + state.getKey() + "'")
                            .collect(Collectors.joining(", ")));
            }
        }
    }

}