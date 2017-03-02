/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;

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
                ", " + WITH_CLAUSE_ALIAS + " (devicegroup, device, value, timestamp) as" +
                        " (select devicegroup, device, value, timestamp from allData where kpitype ='" + KPI_TYPE + "')");
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
                ", max(totalSuspectsKpi.timestamp), nvl(max(totalSuspectsKpi.value), 0) as totalSuspectValues");
    }

    @Test
    public void appendJoinTo() {
        // Business method
        kpiType.appendJoinTo(sqlBuilder);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                " join " + WITH_CLAUSE_ALIAS + " " + KPI_TABLE_NAME + " on "
                        + KPI_TABLE_NAME + ".device = dev.meterid and " + KPI_TABLE_NAME + ".devicegroup = allKpi.devicegroup"
        );
    }

    @Test
    public void appendJoinIfIncluded() {
        // Business method
        kpiType.appendJoinIfIncluded(sqlBuilder, ImmutableSet.of(kpiType));

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                " join " + WITH_CLAUSE_ALIAS + " " + KPI_TABLE_NAME + " on "
                        + KPI_TABLE_NAME + ".device = dev.meterid and " + KPI_TABLE_NAME + ".devicegroup = allKpi.devicegroup"
        );
    }

    @Test
    public void appendJoinIfNotIncluded() {
        // Business method
        kpiType.appendJoinIfIncluded(sqlBuilder, Collections.emptySet());

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                " left join " + WITH_CLAUSE_ALIAS + " " + KPI_TABLE_NAME + " on "
                        + KPI_TABLE_NAME + ".device = dev.meterid and " + KPI_TABLE_NAME + ".devicegroup = allKpi.devicegroup"
        );
    }

    @Test
    public void appendHavingTo() {
        // Business method
        kpiType.appendHavingTo(sqlBuilder, new MetricValueRange.IgnoreRange());

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo("max(nvl(" + KPI_TABLE_NAME + ".value, 0)) >= 0");
    }

    @Test
    public void appendSumOfKpiTypes() {
        KpiType kpiType_2 = new KpiType(WITH_CLAUSE_ALIAS + "_2", KPI_TABLE_NAME + "_2", KPI_TYPE + "_2");

        // Business method
        KpiType.appendKpisSumHavingTo(sqlBuilder, new MetricValueRange.IgnoreRange(), kpiType, kpiType_2);

        // Asserts
        assertThat(sqlBuilder.toString()).isEqualTo(
                "(max(nvl(" + KPI_TABLE_NAME + ".value, 0)) + max(nvl(" + KPI_TABLE_NAME + "_2.value, 0))) >= 0");
    }
}
