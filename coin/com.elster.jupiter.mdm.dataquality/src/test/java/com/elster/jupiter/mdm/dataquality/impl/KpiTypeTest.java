/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class KpiTypeTest {

    private static final String WITH_CLAUSE_ALIAS = "withClauseAlias";
    private static final String KPI_TABLE_NAME = "kpiTableName";
    private static final String KPI_TYPE = "kpiType";

    private KpiType kpiType = new KpiType(WITH_CLAUSE_ALIAS, KPI_TABLE_NAME, KPI_TYPE);

    private SqlBuilder sqlBuilder;

    @Before
    public void setUp() {
        this.sqlBuilder = new SqlBuilder();
    }

    @Test
    public void appendWithClauseTo() {
        // Business method
        kpiType.appendWithClauseTo(sqlBuilder);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                ", " + WITH_CLAUSE_ALIAS + " (usagepoint, channelscontainer, value, latest) as" +
                        " (select usagepoint, channelscontainer, value, latest from allData where kpitype ='" + KPI_TYPE + "')");
    }

    @Test
    public void appendSelectTo() {
        // Business method
        kpiType.appendSelectTo(sqlBuilder);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                ", nvl(max(" + KPI_TABLE_NAME + ".value), 0) as " + WITH_CLAUSE_ALIAS);
    }

    @Test
    public void appendSelectToForSuspects() {
        // Business method
        KpiType.SUSPECT.appendSelectTo(sqlBuilder);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                ", max(totalSuspectsKpi.latest), nvl(max(totalSuspectsKpi.value), 0) as totalSuspectValues");
    }

    @Test
    public void appendJoinTo() {
        // Business method
        kpiType.appendJoinTo(sqlBuilder);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                " left join " + WITH_CLAUSE_ALIAS + " " + KPI_TABLE_NAME + " on "
                        + KPI_TABLE_NAME + ".usagepoint = up.id and kpiTableName.channelscontainer = allKpi.channelscontainer"
        );
    }

    @Test
    public void appendHavingTo() {
        // Business method
        kpiType.appendHavingTo(sqlBuilder, MetricValueRange.AT_LEAST_ONE);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo("max(nvl(" + KPI_TABLE_NAME + ".value, 0)) > ? ");
    }

    @Test
    public void appendHavingToOfTotalEdited() {
        KpiType kpiType = KpiType.TOTAL_EDITED;

        // Business method
        kpiType.appendHavingTo(sqlBuilder, MetricValueRange.AT_LEAST_ONE);

        // Asserts
        // @formatter:off
        assertThat(sqlBuilder.toString()).isEqualTo(
                "(max(nvl(" + KpiType.ADDED.kpiTableName() + ".value, 0)) + " +
                 "max(nvl(" + KpiType.EDITED.kpiTableName() + ".value, 0)) + " +
                 "max(nvl(" + KpiType.REMOVED.kpiTableName() + ".value, 0))) > ? ");
        // @formatter::on
    }
}
