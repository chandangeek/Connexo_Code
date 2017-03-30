/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
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

    static final String DEVICE_STATE_ALIAS_NAME = "enddevices";

    private final String alias;
    private final Set<DefaultState> states;
    private final SetStrategy setStrategy;

    public static DeviceStateSqlBuilder forDefaultExcludedStates(String alias) {
        return forExcludeStates(alias, EnumSet.of(DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED));
    }

    public static DeviceStateSqlBuilder forExcludeStates(String alias, Set<DefaultState> state) {
        SetStrategy setStrategy;
        if (state.size() == 1) {
            setStrategy = SetStrategy.EXCLUDE_ONE;
        }
        else {
            setStrategy = SetStrategy.EXCLUDE_MULTIPLE;
        }
        return new DeviceStateSqlBuilder(alias, setStrategy, state);
    }

    private DeviceStateSqlBuilder(String alias, SetStrategy setStrategy, Set<DefaultState> states) {
        super();
        this.alias = alias;
        this.states = states;
        this.setStrategy = setStrategy;
    }

    public void appendRestrictedStatesWithClause(SqlBuilder sqlBuilder, Instant now) {
        sqlBuilder.append(this.alias);
        sqlBuilder.append(" as (");
        sqlBuilder.append("select ES.enddevice id");
        sqlBuilder.append("  from MTR_ENDDEVICESTATUS ES,");
        sqlBuilder.append("       (select FS.ID");
        sqlBuilder.append("          from FSM_STATE FS");
        sqlBuilder.append("         where FS.OBSOLETE_TIMESTAMP IS NULL");
        sqlBuilder.append("           and FS.NAME ");
        this.setStrategy.append(sqlBuilder, this.states);
        sqlBuilder.append(") FS");
        sqlBuilder.append(" where ES.STARTTIME <=");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append("   and ES.ENDTIME >");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append("   and ES.STATE = FS.ID)");
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
                if (!states.isEmpty()) {
                    sqlBuilder.append("not in (");
                    appendStates(sqlBuilder, states);
                    sqlBuilder.append(")");
                } else {
                    sqlBuilder.append(" is not null");
                }
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