package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryType;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
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
    private static final String USAGE_POINT_NAME = ", pls have a drink & try again";
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
    private MeterActivation meterActivation;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable1, readingTypeDeliverable2;
    @Mock
    private ChannelDataValidationSummary summary1, summary2;
    @Mock
    private MeterRole meterRole;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(usagePoint.getMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getRange()).thenReturn(Range.all());
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(clock.getZone()).thenReturn(NOW.getZone());
        when(metrologyConfiguration.getMeterRoles()).thenReturn(Collections.singletonList(meterRole));

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
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No usage point with name xxx");
    }

    @Test
    public void testQueryParameterPurposeIdMissing() throws IOException {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("periodId", 5).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Metrology contract with id 0 is not found on usage point , pls have a drink & try again.");
    }

    @Test
    public void testNoSuchPurpose() throws IOException {
        mockUsagePointMetrologyConfiguration();

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 1000).queryParam("periodId", 5).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Metrology contract with id 1,000 is not found on usage point , pls have a drink & try again.");
    }

    @Test
    public void testQueryParameterPeriodIdMissing() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(1);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 1).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Relative period with id 0 is not found.");
    }

    @Test
    public void testNoSuchPeriod() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(2);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 2).queryParam("periodId", 100).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Relative period with id 100 is not found.");
    }

    @Test
    public void testFuturePeriod() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(3);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 3).queryParam("periodId", 7).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("Cannot gather validation statistics for relative period with id 7: it is in the future.");
    }

    @Test
    public void testGetValidationSummaryForToday() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(4);
        when(usagePointDataCompletionService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(ImmutableMap.of(
                        readingTypeDeliverable2, Collections.singletonList(summary2),
                        readingTypeDeliverable1, Collections.singletonList(summary1)
                ));
        when(readingTypeDeliverable1.getId()).thenReturn(1L);
        when(readingTypeDeliverable1.getName()).thenReturn("Ityvbelomplatye");
        when(readingTypeDeliverable2.getId()).thenReturn(2L);
        when(readingTypeDeliverable2.getName()).thenReturn("Vmoihobyatiah");
        when(summary1.getSum()).thenReturn(18);
        when(summary1.getType()).thenReturn(ChannelDataValidationSummaryType.GENERAL);
        when(summary1.getValues()).thenReturn(ImmutableMap.of(
                ChannelDataValidationSummaryFlag.SUSPECT, 12,
                ChannelDataValidationSummaryFlag.VALID, 6
        ));
        when(summary2.getSum()).thenReturn(22);
        when(summary2.getType()).thenReturn(ChannelDataValidationSummaryType.GENERAL);
        when(summary2.getValues()).thenReturn(ImmutableMap.of(
                ChannelDataValidationSummaryFlag.VALID, 9,
                ChannelDataValidationSummaryFlag.NOT_VALIDATED, 13
        ));

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 4).queryParam("periodId", 5).request().get();

        // Asserts
        verify(usagePointDataCompletionService).getValidationSummary(effectiveMC, metrologyContract, Range.openClosed(NOW.withMinute(0).toInstant(), NOW.toInstant()));
        verifyNoMoreInteractions(usagePointDataCompletionService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].id")).containsExactly(2, 1);
        assertThat(jsonModel.<List<String>>get("$.outputs[*].name")).containsExactly("Vmoihobyatiah", "Ityvbelomplatye");
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].total")).containsExactly(22, 18);
        assertThat(jsonModel.<List<String>>get("$.outputs[0].statistics[*].key")).containsExactly("statisticsValid", "statisticsNotValidated");
        assertThat(jsonModel.<List<String>>get("$.outputs[0].statistics[*].displayName")).containsExactly("Valid", "Not validated");
        assertThat(jsonModel.<List<Number>>get("$.outputs[0].statistics[*].count")).containsExactly(9, 13);
        assertThat(jsonModel.<List<String>>get("$.outputs[1].statistics[*].key"))
                .containsExactly("statisticsSuspect", "statisticsValid");
        assertThat(jsonModel.<List<String>>get("$.outputs[1].statistics[*].displayName"))
                .containsExactly("Suspect", "Valid");
        assertThat(jsonModel.<List<Number>>get("$.outputs[1].statistics[*].count")).containsExactly(12, 6);
    }

    @Test
    public void testGetValidationSummaryForYesterday() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(5);
        Instant meterActivated = NOW.minusDays(1).withHour(4).toInstant();
        when(meterActivation.getRange()).thenReturn(Range.closedOpen(meterActivated, NOW.toInstant()));
        when(usagePointDataCompletionService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(ImmutableMap.of(
                        readingTypeDeliverable1, Collections.singletonList(summary1)
                ));
        when(readingTypeDeliverable1.getId()).thenReturn(3L);
        when(readingTypeDeliverable1.getName()).thenReturn("DekabrJanvahrIFevral");
        when(summary1.getSum()).thenReturn(0);
        when(summary1.getType()).thenReturn(ChannelDataValidationSummaryType.GENERAL);
        when(summary1.getValues()).thenReturn(Collections.emptyMap());

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 5).queryParam("periodId", 6).request().get();

        // Asserts
        verify(usagePointDataCompletionService).getValidationSummary(effectiveMC, metrologyContract, Range.closed(meterActivated, NOW.withMinute(0).toInstant()));
        verifyNoMoreInteractions(usagePointDataCompletionService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].id")).containsExactly(3);
        assertThat(jsonModel.<List<String>>get("$.outputs[*].name")).containsExactly("DekabrJanvahrIFevral");
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].total")).containsExactly(0);
        assertThat(jsonModel.<List<List<ChannelDataValidationSummaryFlagInfo>>>get("$.outputs[*].statistics"))
                .isEqualTo(Collections.singletonList(Collections.emptyList()));
    }

    @Test
    public void testGetValidationSummaryButMeterIsNotActivated() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(5);
        Range<Instant> emptyInterval = Range.openClosed(NOW.toInstant(), NOW.toInstant());
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        when(usagePointDataCompletionService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), eq(emptyInterval)))
                .thenReturn(ImmutableMap.of(
                        readingTypeDeliverable1, Collections.singletonList(summary1)
                ));
        when(readingTypeDeliverable1.getId()).thenReturn(4L);
        when(readingTypeDeliverable1.getName()).thenReturn("Lalalala");
        when(summary1.getSum()).thenReturn(0);
        when(summary1.getType()).thenReturn(ChannelDataValidationSummaryType.GENERAL);
        when(summary1.getValues()).thenReturn(Collections.emptyMap());

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 5).queryParam("periodId", 6).request().get();

        // Asserts
        verify(usagePointDataCompletionService).getValidationSummary(effectiveMC, metrologyContract, emptyInterval);
        verifyNoMoreInteractions(usagePointDataCompletionService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].id")).containsExactly(4);
        assertThat(jsonModel.<List<String>>get("$.outputs[*].name")).containsExactly("Lalalala");
        assertThat(jsonModel.<List<Number>>get("$.outputs[*].total")).containsExactly(0);
        assertThat(jsonModel.<List<List<ChannelDataValidationSummaryFlagInfo>>>get("$.outputs[*].statistics"))
                .isEqualTo(Collections.singletonList(Collections.emptyList()));
    }

    @Test
    public void testGetValidationSummaryNoDeliverables() throws IOException {
        mockUsagePointMetrologyConfiguration();
        mockMetrologyContract(5);
        when(usagePointDataCompletionService.getValidationSummary(eq(effectiveMC), eq(metrologyContract), any()))
                .thenReturn(Collections.emptyMap());

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/validationSummary").queryParam("purposeId", 5).queryParam("periodId", 6).request().get();

        // Asserts
        verify(usagePointDataCompletionService).getValidationSummary(effectiveMC, metrologyContract, YESTERDAY.getOpenClosedInterval(NOW));
        verifyNoMoreInteractions(usagePointDataCompletionService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<ChannelDataValidationSummaryInfo>>get("$.outputs")).isEmpty();
    }

    private void mockUsagePointMetrologyConfiguration() {
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMC.getRange()).thenReturn(Range.all());
    }

    private void mockMetrologyContract(long id) {
        when(metrologyContract.getId()).thenReturn(id);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
    }
}
