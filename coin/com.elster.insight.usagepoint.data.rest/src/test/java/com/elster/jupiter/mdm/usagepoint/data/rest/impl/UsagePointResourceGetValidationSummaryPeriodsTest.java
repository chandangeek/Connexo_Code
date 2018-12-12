/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointResourceGetValidationSummaryPeriodsTest extends UsagePointDataRestApplicationJerseyTest {
    private static final String USAGE_POINT_NAME = "Il nome";
    private static final String LAST_7_DAYS = "Last 7 days";
    private static final String PREVIOUS_MONTH = "Previous month";
    private static final String THIS_MONTH = "This month";
    private static final String THIS_YEAR = "This year";
    private static final String TODAY = "Today";
    private static final String YESTERDAY = "Yesterday";
    private static final String YEAR_AGO = "1 year ago";

    private ZonedDateTime referenceTime = ZonedDateTime.of(2016, 7, 15, 12, 0, 0, 0, ZoneId.of("Europe/Brussels"));

    @Mock
    private UsagePoint usagePoint;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(clock.instant()).thenReturn(referenceTime.toInstant());
        when(clock.getZone()).thenReturn(referenceTime.getZone());

        Query relativePeriodsQuery = mock(Query.class);
        doReturn(relativePeriodsQuery).when(timeService).getRelativePeriodQuery();
        List<RelativePeriod> relativePeriods = mockRelativePeriods();
        when(relativePeriodsQuery.select(any(Condition.class))).thenReturn(relativePeriods);
    }

    private List<RelativePeriod> mockRelativePeriods() {
        List<RelativePeriod> periods = new ArrayList<>();
        periods.add(mockRelativePeriod(1, LAST_7_DAYS, referenceTime.minus(Period.ofDays(7)), referenceTime));
        periods.add(mockRelativePeriod(2, PREVIOUS_MONTH, referenceTime.minusMonths(1).withDayOfMonth(1).withHour(0), referenceTime.withDayOfMonth(1).withHour(0)));
        periods.add(mockRelativePeriod(3, THIS_MONTH, referenceTime.withDayOfMonth(1).withHour(0), referenceTime.plusDays(1).withHour(0)));
        periods.add(mockRelativePeriod(4, THIS_YEAR, referenceTime.withDayOfYear(1).withHour(0), referenceTime.plusDays(1).withHour(0)));
        periods.add(mockRelativePeriod(5, TODAY, referenceTime.withHour(0), referenceTime.plusDays(1).withHour(0)));
        periods.add(mockRelativePeriod(6, YESTERDAY, referenceTime.minusDays(1).withHour(0), referenceTime.withHour(0)));
        periods.add(mockRelativePeriod(7, YEAR_AGO, referenceTime.minusYears(2).withDayOfYear(1).withHour(0), referenceTime.minusYears(1).withDayOfYear(1).withHour(0)));
        return periods;
    }

    private RelativePeriod mockRelativePeriod(long id, String name, ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        RelativePeriod relativePeriod = mock(RelativePeriod.class);
        when(relativePeriod.getId()).thenReturn(id);
        when(relativePeriod.getName()).thenReturn(name);
        when(relativePeriod.getOpenClosedZonedInterval(referenceTime)).thenReturn(Range.openClosed(startDateTime, endDateTime));
        return relativePeriod;
    }

    @Test
    public void testNoSuchUsagePoint() {
        when(meteringService.findUsagePointByName("xxx")).thenReturn(Optional.empty());

        // Business method
        Response response = target("usagepoints/xxx/validationSummaryPeriods").queryParam("purposeId", 1000).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNoSuchPurpose() {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 1000).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testQueryParameterPurposeIdMissing() {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRelativePeriodsNoDeliverables() {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockUsagePointMetrologyConfiguration();
        List<MetrologyContract> metrologyContracts = Arrays.asList(mockMetrologyContract(1), mockMetrologyContract(2));
        when(metrologyConfiguration.getContracts()).thenReturn(metrologyContracts);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List<Number>>get("$.relativePeriods[*].id")).containsExactly(7, 4, 2, 3, 1, 6, 5);
        assertThat(jsonModel.<List<String>>get("$.relativePeriods[*].name")).containsExactly(
                YEAR_AGO, THIS_YEAR, PREVIOUS_MONTH, THIS_MONTH, LAST_7_DAYS, YESTERDAY, TODAY
        );
    }

    @Test
    public void testGetRelativePeriods1MinMaximumChannel() {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockUsagePointMetrologyConfiguration();
        MetrologyContract metrologyContract = mockMetrologyContract(2);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        List<ReadingTypeDeliverable> deliverables = Collections.singletonList(
                mockReadingTypeDeliverable(Duration.ofMinutes(1))
        );
        when(metrologyContract.getDeliverables()).thenReturn(deliverables);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List<Number>>get("$.relativePeriods[*].id")).containsExactly(6, 5, 1, 3, 2, 4, 7);
        assertThat(jsonModel.<List<String>>get("$.relativePeriods[*].name")).containsExactly(
                YESTERDAY, TODAY, LAST_7_DAYS, THIS_MONTH, PREVIOUS_MONTH, THIS_YEAR, YEAR_AGO
        );
    }

    @Test
    public void testGetRelativePeriods15MinMaximumChannel() {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockUsagePointMetrologyConfiguration();
        MetrologyContract metrologyContract = mockMetrologyContract(2);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        List<ReadingTypeDeliverable> deliverables = Arrays.asList(
                mockReadingTypeDeliverable(Duration.ofMinutes(15)),
                mockReadingTypeDeliverable(Duration.ofMinutes(5))
        );
        when(metrologyContract.getDeliverables()).thenReturn(deliverables);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List<Number>>get("$.relativePeriods[*].id")).containsExactly(3, 1, 6, 5, 2, 4, 7);
        assertThat(jsonModel.<List<String>>get("$.relativePeriods[*].name")).containsExactly(
                THIS_MONTH, LAST_7_DAYS, YESTERDAY, TODAY, PREVIOUS_MONTH, THIS_YEAR, YEAR_AGO
        );
    }

    @Test
    public void testGetRelativePeriodsMonthlyMaximumChannel() {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockUsagePointMetrologyConfiguration();
        MetrologyContract metrologyContract = mockMetrologyContract(2);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        List<ReadingTypeDeliverable> deliverables = Arrays.asList(
                mockReadingTypeDeliverable(Period.ofMonths(1)),
                mockReadingTypeDeliverable(Duration.ofDays(1))
        );
        when(metrologyContract.getDeliverables()).thenReturn(deliverables);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List<Number>>get("$.relativePeriods[*].id")).containsExactly(7, 4, 2, 3, 1, 6, 5);
        assertThat(jsonModel.<List<String>>get("$.relativePeriods[*].name")).containsExactly(
                YEAR_AGO, THIS_YEAR, PREVIOUS_MONTH, THIS_MONTH, LAST_7_DAYS, YESTERDAY, TODAY
        );
    }

    @Test
    public void testGetRelativePeriodsRegistersInDeliverables() {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockUsagePointMetrologyConfiguration();
        MetrologyContract metrologyContract = mockMetrologyContract(2);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        List<ReadingTypeDeliverable> deliverables = Arrays.asList(
                mockReadingTypeDeliverable(null),
                mockReadingTypeDeliverable(null)
        );
        when(metrologyContract.getDeliverables()).thenReturn(deliverables);

        // Business method
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummaryPeriods").queryParam("purposeId", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<List<Number>>get("$.relativePeriods[*].id")).containsExactly(7, 4, 2, 3, 1, 6, 5);
        assertThat(jsonModel.<List<String>>get("$.relativePeriods[*].name")).containsExactly(
                YEAR_AGO, THIS_YEAR, PREVIOUS_MONTH, THIS_MONTH, LAST_7_DAYS, YESTERDAY, TODAY
        );
    }

    private UsagePointMetrologyConfiguration mockUsagePointMetrologyConfiguration() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        return metrologyConfiguration;
    }

    private MetrologyContract mockMetrologyContract(long id) {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        when(metrologyContract.getId()).thenReturn(id);
        return metrologyContract;
    }

    private ReadingTypeDeliverable mockReadingTypeDeliverable(TemporalAmount intervalLength) {
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getIntervalLength()).thenReturn(Optional.ofNullable(intervalLength));
        when(readingType.isRegular()).thenReturn(intervalLength != null);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        return readingTypeDeliverable;
    }
}
