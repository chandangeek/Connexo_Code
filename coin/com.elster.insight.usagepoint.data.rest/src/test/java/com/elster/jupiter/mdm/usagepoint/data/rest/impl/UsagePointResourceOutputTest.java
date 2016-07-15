package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UsagePointResourceOutputTest extends UsagePointDataRestApplicationJerseyTest {
    public static final Instant NOW = ZonedDateTime.of(2016, 6, 1, 12, 40, 30, 0, ZoneId.systemDefault()).toInstant();
    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private DataValidationTask validationTask;
    @Mock
    private Query<DataValidationTask> dataValidationTaskQuery;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingType readingType;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private Channel channel;
    @Mock
    private MetrologyContract.Status status;

    @Before
    public void setStubs() {
        when(clock.instant()).thenReturn(NOW);
        when(meteringService.findUsagePoint("UP")).thenReturn(Optional.of(usagePoint));
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        setDataValidationTaskStub();
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getMRID()).thenReturn("UP");
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        setMetrologyContractStub();
    }

    private void setDataValidationTaskStub() {
        when(validationService.findValidationTasksQuery()).thenReturn(dataValidationTaskQuery);
        when(dataValidationTaskQuery.select(any(Condition.class))).thenReturn(Collections.singletonList(validationTask));
        when(validationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(validationTask.getMetrologyContract()).thenReturn(Optional.of(metrologyContract));
        when(validationTask.getId()).thenReturn(1L);
        when(validationTask.getName()).thenReturn("Validation Task");
        when(validationTask.getScheduleExpression()).thenReturn(new TemporalExpression(TimeDuration.days(5)));
        when(validationTask.getLastRun()).thenReturn(Optional.empty());
        when(validationTask.getLastOccurrence()).thenReturn(Optional.empty());
    }

    private void setMetrologyContractStub() {
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyPurpose.getId()).thenReturn(1L);
        when(metrologyPurpose.getName()).thenReturn(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyContract.getId()).thenReturn(1L);
        when(metrologyContract.getStatus(usagePoint)).thenReturn(status);
        when(metrologyContract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(status.isComplete()).thenReturn(false);
        when(status.getKey()).thenReturn("INCOMPLETE");
        when(status.getName()).thenReturn("Incomplete");
    }

    @Test
    public void testDataValidationTaskInfoOnMetrologyContract() {
        String json = target("/usagepoints/UP/purposes").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Object>get("$.purposes[0].dataValidationTasks")).isNotNull();
        assertThat(jsonModel.<Number>get("$.purposes[0].dataValidationTasks[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.purposes[0].dataValidationTasks[0].name")).isEqualTo("Validation Task");
        assertThat(jsonModel.<Number>get("$.purposes[0].dataValidationTasks[0].metrologyContract.id")).isEqualTo(1);
        assertThat(jsonModel.<Object>get("$.purposes[0].dataValidationTasks[0].schedule")).isNotNull();
        assertThat(jsonModel.<Number>get("$.purposes[0].dataValidationTasks[0].schedule.count")).isEqualTo(5);
        assertThat(jsonModel.<String>get("$.purposes[0].dataValidationTasks[0].schedule.timeUnit")).isEqualTo("days");
    }

}
