package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.time.RelativePeriod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UsagePointResourceGetValidationSummaryTest extends UsagePointDataRestApplicationJerseyTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2016, 2, 29, 0, 1, 0, 0, ZoneId.of("Europe/Brussels"));
    private static final RelativePeriod TODAY = mockRelativePeriod(5, "Today", NOW.withMinute(0), NOW.plusDays(1).withMinute(0));
    private static final RelativePeriod YESTERDAY = mockRelativePeriod(6, "Yesterday", NOW.minusDays(1).withMinute(0), NOW.withMinute(0));
    private static final RelativePeriod TOMORROW = mockRelativePeriod(7, "Tomorrow", NOW.plusDays(1).withMinute(0), NOW.plusDays(2).withMinute(0));

    @Rule
    public TestRule maltaLocale = Using.localeOfMalta();

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable1, readingTypeDeliverable2;
    @Mock
    private ChannelDataValidationSummary summary1, summary2;

    @Before
    public void before() {
        when(meteringService.findUsagePoint(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());

        when(timeService.findRelativePeriod(anyLong())).thenReturn(Optional.empty());
        when(timeService.findRelativePeriod(5)).thenReturn(Optional.of(TODAY));
        when(timeService.findRelativePeriod(6)).thenReturn(Optional.of(YESTERDAY));
        when(timeService.findRelativePeriod(7)).thenReturn(Optional.of(TOMORROW));
    }

    private static RelativePeriod mockRelativePeriod(long id, String name, ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        RelativePeriod relativePeriod = mock(RelativePeriod.class);
        when(relativePeriod.getId()).thenReturn(id);
        when(relativePeriod.getName()).thenReturn(name);
        when(relativePeriod.getOpenClosedInterval(NOW)).thenReturn(Range.openClosed(startDateTime.toInstant(), endDateTime.toInstant()));
        return relativePeriod;
    }

    @Test
    public void testNoSuchUsagePoint() throws IOException {
        // Business method
        Response response = target("usagepoints/xxx/validationSummary").queryParam("purposeId", 1000).queryParam("periodId", 5).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No usage point with MRID xxx");
    }

    @Test
    public void testQueryParameterPurposeIdMissing() throws IOException {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("periodId", 5).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Metrology purpose with id 0 is not found on usage point with MRID MRID.");
    }

    @Test
    public void testNoSuchPurpose() throws IOException {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 1000).queryParam("periodId", 5).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Metrology purpose with id 1,000 is not found on usage point with MRID MRID.");
    }

    @Test
    public void testQueryParameterPeriodIdMissing() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(1);

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 1).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Relative period with id 0 is not found.");
    }

    @Test
    public void testNoSuchPeriod() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(2);

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 2).queryParam("periodId", 100).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Relative period with id 100 is not found.");
    }

    @Test
    public void testFuturePeriod() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(3);

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 3).queryParam("periodId", 7).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Cannot gather validation statistics for relative period with id 7: it is in the future.");
    }

    @Test
    public void testGetValidationSummaryForToday() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(4);
        when(usagePointDataService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(ImmutableMap.of(
                        readingTypeDeliverable2, summary2,
                        readingTypeDeliverable1, summary1
                ));
        when(readingTypeDeliverable1.getId()).thenReturn(1L);
        when(readingTypeDeliverable1.getName()).thenReturn("Ityvbelomplatye");
        when(readingTypeDeliverable2.getId()).thenReturn(2L);
        when(readingTypeDeliverable2.getName()).thenReturn("Vmoihobyatiah");
        Range<Instant> interval = Range.openClosed(NOW.withMinute(0).toInstant(), NOW.toInstant());
        when(summary1.getSum()).thenReturn(105);
        when(summary1.getValues()).thenReturn(ImmutableMap.of(
                ChannelDataValidationSummaryFlag.MISSING, 2,
                ChannelDataValidationSummaryFlag.SUSPECT, 12,
                ChannelDataValidationSummaryFlag.ESTIMATED, 85,
                ChannelDataValidationSummaryFlag.EDITED, 0,
                ChannelDataValidationSummaryFlag.VALID, 6
        ));
        when(summary1.getTargetInterval()).thenReturn(interval);
        when(summary2.getSum()).thenReturn(22);
        when(summary2.getValues()).thenReturn(ImmutableMap.of(
                ChannelDataValidationSummaryFlag.VALID, 9,
                ChannelDataValidationSummaryFlag.NOT_VALIDATED, 13
        ));
        when(summary2.getTargetInterval()).thenReturn(interval);

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 4).queryParam("periodId", 5).request().get();

        // Asserts
        verify(usagePointDataService).getValidationSummary(effectiveMC, metrologyContract, interval);
        verifyNoMoreInteractions(usagePointDataService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].id")).containsExactly(2, 1);
        assertThat(jsonModel.<List<String>>get("$.outputs[*].name")).containsExactly("Vmoihobyatiah", "Ityvbelomplatye");
        long start = interval.lowerEndpoint().toEpochMilli();
        long end = interval.upperEndpoint().toEpochMilli();
        assertThat(jsonModel.<List<Long>>get("$.outputs[*].intervalStart")).containsExactly(start, start);
        assertThat(jsonModel.<List<Long>>get("$.outputs[*].intervalEnd")).containsExactly(end, end);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].total")).containsExactly(22, 105);
        assertThat(jsonModel.<List<String>>get("$.outputs[0].statistics[*].key")).containsExactly("statisticsValid", "statisticsNotValidated");
        assertThat(jsonModel.<List<String>>get("$.outputs[0].statistics[*].displayName")).containsExactly("Valid", "Not validated");
        assertThat(jsonModel.<List<Number>>get("$.outputs[0].statistics[*].count")).containsExactly(9, 13);
        assertThat(jsonModel.<List<String>>get("$.outputs[1].statistics[*].key"))
                .containsExactly("statisticsMissing", "statisticsSuspect", "statisticsEstimated", "statisticsEdited", "statisticsValid");
        assertThat(jsonModel.<List<String>>get("$.outputs[1].statistics[*].displayName"))
                .containsExactly("Missing", "Suspect", "Estimated", "Edited", "Valid");
        assertThat(jsonModel.<List<Number>>get("$.outputs[1].statistics[*].count")).containsExactly(2, 12, 85, 0, 6);
    }

    @Test
    public void testGetValidationSummaryForYesterday() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(5);
        when(usagePointDataService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(ImmutableMap.of(
                        readingTypeDeliverable1, summary1
                ));
        when(readingTypeDeliverable1.getId()).thenReturn(3L);
        when(readingTypeDeliverable1.getName()).thenReturn("DekabrJanvahrIFevral");
        Range<Instant> interval = YESTERDAY.getOpenClosedInterval(NOW);
        when(summary1.getSum()).thenReturn(0);
        when(summary1.getValues()).thenReturn(Collections.emptyMap());
        when(summary1.getTargetInterval()).thenReturn(interval);

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 5).queryParam("periodId", 6).request().get();

        // Asserts
        verify(usagePointDataService).getValidationSummary(effectiveMC, metrologyContract, interval);
        verifyNoMoreInteractions(usagePointDataService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].id")).containsExactly(3);
        assertThat(jsonModel.<List<String>>get("$.outputs[*].name")).containsExactly("DekabrJanvahrIFevral");
        assertThat(jsonModel.<List<Long>>get("$.outputs[*].intervalStart")).containsExactly(interval.lowerEndpoint().toEpochMilli());
        assertThat(jsonModel.<List<Long>>get("$.outputs[*].intervalEnd")).containsExactly(interval.upperEndpoint().toEpochMilli());
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].total")).containsExactly(0);
        assertThat(jsonModel.<List<List<ChannelDataValidationSummaryFlagInfo>>>get("$.outputs[*].statistics"))
                .isEqualTo(Collections.singletonList(Collections.emptyList()));
    }

    @Test
    public void testGetValidationSummaryNoDeliverables() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(5);
        when(usagePointDataService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(Collections.emptyMap());

        // Business method
        Response response = target("usagepoints/MRID/validationSummary").queryParam("purposeId", 5).queryParam("periodId", 6).request().get();

        // Asserts
        verify(usagePointDataService).getValidationSummary(effectiveMC, metrologyContract, YESTERDAY.getOpenClosedInterval(NOW));
        verifyNoMoreInteractions(usagePointDataService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<ChannelDataValidationSummaryInfo>>get("$.outputs")).isEmpty();
    }

    private void mockUsagePointMetrologyConfiguration() {
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
    }

    private void mockMetrologyContract(long id) {
        when(metrologyContract.getId()).thenReturn(id);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
    }
}
