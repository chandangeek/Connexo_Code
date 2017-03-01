/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@LiteralSql
public class DeviceStageSqlBuilder {

    static final String DEVICE_STAGE_ALIAS_NAME = "enddevices";

    private final String alias;
    private final Set<EndDeviceStage> stages;
    private final SetStrategy setStrategy;

    public static DeviceStageSqlBuilder forDefaultExcludedStages(String alias) {
        return forExcludeStages(alias, EnumSet.of(EndDeviceStage.POST_OPERATIONAL, EndDeviceStage.PRE_OPERATIONAL));
    }

    public static DeviceStageSqlBuilder forExcludeStages(String alias, Set<EndDeviceStage> stages) {
        SetStrategy setStrategy;
        if (stages.size() == 1) {
            setStrategy = SetStrategy.EXCLUDE_ONE;
        }
        else {
            setStrategy = SetStrategy.EXCLUDE_MULTIPLE;
        }
        return new DeviceStageSqlBuilder(alias, setStrategy, stages);
    }

    private DeviceStageSqlBuilder(String alias, SetStrategy setStrategy, Set<EndDeviceStage> stages) {
        super();
        this.alias = alias;
        this.stages = stages;
        this.setStrategy = setStrategy;
    }

    public void appendRestrictedStagesWithClause(SqlBuilder sqlBuilder, Instant now) {
        sqlBuilder.append(this.alias);
        sqlBuilder.append(" as (");
        sqlBuilder.append("select ES.enddevice id");
        sqlBuilder.append("  from MTR_ENDDEVICESTATUS ES,");
        sqlBuilder.append("       (select FS.ID");
        sqlBuilder.append("          from FSM_STATE FS");
        sqlBuilder.append("         where FS.OBSOLETE_TIMESTAMP IS NULL");
        sqlBuilder.append("           and FS.STAGE ");
        this.setStrategy.append(sqlBuilder, this.stages);
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
            void append(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("in (");
                appendStages(sqlBuilder, stages);
                sqlBuilder.append(")");
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("in (");
                appendStages(sqlBuilder, stages);
                sqlBuilder.append(")");
            }
        },

        EXCLUDE_MULTIPLE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                if (!stages.isEmpty()) {
                    sqlBuilder.append("not in (");
                    appendStages(sqlBuilder, stages);
                    sqlBuilder.append(")");
                } else {
                    sqlBuilder.append(" is not null");
                }
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("not in (");
                appendStages(sqlBuilder, stages);
                sqlBuilder.append(")");
            }
        },

        INCLUDE_ONE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("= ");
                appendStages(sqlBuilder, stages);
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("= ");
                appendStages(sqlBuilder, stages);
            }
        },

        EXCLUDE_ONE {
            @Override
            void append(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("<> ");
                appendStages(sqlBuilder, stages);
            }

            @Override
            void append(StringBuilder sqlBuilder, Set<EndDeviceStage> stages) {
                sqlBuilder.append("<> ");
                appendStages(sqlBuilder, stages);
            }
        };

        abstract void append(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages);

        abstract void append(StringBuilder sqlBuilder, Set<EndDeviceStage> stages);

        protected void appendStages(SqlBuilder sqlBuilder, Set<EndDeviceStage> stages) {
            if (stages.size() == 1) {
                sqlBuilder.append("(SELECT TOP 1 FSTG.ID FROM FSM_STAGE FSTG WHERE FSTG.NAME = '");
                sqlBuilder.append(stages.iterator().next().name());
                sqlBuilder.append("')");
            }
            else {
                sqlBuilder.append("SELECT FSTG.ID FROM FSM_STAGE FSTG WHERE FSTG.NAME in (");
                sqlBuilder.append(
                        stages
                            .stream()
                            .map(stage -> "'" + stage.name() + "'")
                            .collect(Collectors.joining(", ")));
                sqlBuilder.append(")");
            }
        }

        protected void appendStages(StringBuilder sqlBuilder, Set<EndDeviceStage> stages) {
            if (stages.size() == 1) {
                sqlBuilder.append("(SELECT TOP 1 FSTG.ID FROM FSM_STAGE FSTG WHERE FSTG.NAME = '");
                sqlBuilder.append(stages.iterator().next().name());
                sqlBuilder.append("')");
            }
            else {
                sqlBuilder.append("SELECT FSTG.ID FROM FSM_STAGE FSTG WHERE FSTG.NAME in (");
                sqlBuilder.append(
                        stages
                                .stream()
                                .map(stage -> "'" + stage.name() + "'")
                                .collect(Collectors.joining(", ")));
                sqlBuilder.append(")");
            }
        }
    }

}