/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataQualityOverviewSqlBuilderTest {

    @Mock
    private Clock clock;
    @Mock
    private Connection connection;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private UsagePointGroup usagePointGroup1, usagePointGroup2;
    @Mock
    private MetrologyConfiguration metrologyConfiguration1, metrologyConfiguration2;
    @Mock
    private MetrologyPurpose metrologyPurpose1, metrologyPurpose2;

    private DataQualityOverviewSpecificationImpl specification;

    @Before
    public void setUp() {
        specification = new DataQualityOverviewSpecificationImpl(KpiType.predefinedKpiTypes);
        when(clock.instant()).thenReturn(Instant.now());
    }

    private String prepareSql(DataQualityOverviewSpecificationImpl specification) throws SQLException {
        DataQualityOverviewSqlBuilder sqlBuilder = new DataQualityOverviewSqlBuilder(specification, clock);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);

        sqlBuilder.prepare(connection);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sql.capture());
        return sql.getValue();
    }

    //    @Test
    public void printSql() throws Exception {
        System.out.println(prepareSql(this.specification));
    }

    @Test
    public void conditionByUsagePointGroups() throws Exception {
        this.specification.addUsagePointGroups(Arrays.asList(usagePointGroup1, usagePointGroup2));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("and dqkpi.usagepointgroup in ( ? , ? )");
    }

    @Test
    public void conditionByMetrologyConfigurations() throws Exception {
        this.specification.addMetrologyConfigurations(Arrays.asList(metrologyConfiguration1, metrologyConfiguration2));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("and mc.id in ( ? , ? )");
    }

    @Test
    public void conditionByMetrologyPurpose() throws Exception {
        this.specification.addMetrologyPurposes(Arrays.asList(metrologyPurpose1, metrologyPurpose2));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("and purpose.id in ( ? , ? )");
    }

    @Test
    public void conditionByOpenPeriod() throws SQLException {
        this.specification.setPeriod(Range.open(Instant.EPOCH, Instant.now()));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("where kpivalues.utcstamp > ? and kpivalues.utcstamp < ?");
    }

    @Test
    public void conditionByOpenClosedPeriod() throws SQLException {
        this.specification.setPeriod(Range.openClosed(Instant.EPOCH, Instant.now()));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("where kpivalues.utcstamp > ? and kpivalues.utcstamp <= ?");
    }

    @Test
    public void conditionByClosedOpenPeriod() throws SQLException {
        this.specification.setPeriod(Range.closedOpen(Instant.EPOCH, Instant.now()));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("where kpivalues.utcstamp >= ? and kpivalues.utcstamp < ?");
    }

    @Test
    public void conditionByClosedPeriod() throws SQLException {
        this.specification.setPeriod(Range.closed(Instant.EPOCH, Instant.now()));

        // Business method
        String sql = prepareSql(this.specification);

        // Asserts
        assertThat(sql).contains("where kpivalues.utcstamp >= ? and kpivalues.utcstamp <= ?");
    }
}
