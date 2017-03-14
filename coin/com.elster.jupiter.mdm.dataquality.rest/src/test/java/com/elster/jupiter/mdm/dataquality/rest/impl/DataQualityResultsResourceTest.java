/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.dataquality.DataQualityKpiResults;
import com.elster.jupiter.mdm.dataquality.DataQualityOverview;
import com.elster.jupiter.mdm.dataquality.DataQualityOverviews;
import com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.Validator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class DataQualityResultsResourceTest extends DeviceDataQualityRestApplicationJerseyTest {

    private static final String USAGEPOINT_NAME = "UP0001";
    private static final ServiceKind SERVICE_KIND = ServiceKind.ELECTRICITY;
    private static final Long METROLOGY_CONFIGURATION_ID = 123L;
    private static final String METROLOGY_CONFIGURATION_NAME = "Metrology configuration";
    private static final Long METROLOGY_PURPOSE_ID = 124L;
    private static final String METROLOGY_PURPOSE_NAME = "Billing";

    private static final Long REGISTER_SUSPECTS = 11L;
    private static final Long CHANNEL_SUSPECTS = 12L;
    private static final Long TOTAL_SUSPECTS = 13L;
    private static final Long AMOUNT_OF_CONFIRMED = 21L;
    private static final Long AMOUNT_OF_ESTIMATES = 22L;
    private static final Long AMOUNT_OF_INFORMATIVES = 23L;
    private static final Long AMOUNT_OF_ADDED = 31L;
    private static final Long AMOUNT_OF_EDITED = 32L;
    private static final Long AMOUNT_OF_REMOVED = 33L;
    private static final Long SUSPECTS_BY_VALIDATOR_1 = 101L;
    private static final Long SUSPECTS_BY_VALIDATOR_2 = 102L;
    private static final Long ESTIMATES_BY_ESTIMATOR_1 = 201L;
    private static final Long ESTIMATES_BY_ESTIMATOR_2 = 202L;
    private static final Instant LAST_SUSPECT = Instant.now();

    private static final String VALIDATOR_1 = "V1";
    private static final String VALIDATOR_2 = "V2";
    private static final String ESTIMATOR_1 = "E1";
    private static final String ESTIMATOR_2 = "E2";

    private static final Long USAGEPOINT_GROUP_ID = 123L;

    private UsagePointDataQualityService.DataQualityOverviewBuilder overviewBuilder;

    @Mock
    private DataQualityOverviews dataQualityOverviews;
    @Mock
    private DataQualityOverview dataQualityOverview;
    @Mock
    private Validator validator_1, validator_2;
    @Mock
    private Estimator estimator_1, estimator_2;

    @Before
    public void initMocks() throws Exception {
        overviewBuilder = FakeBuilder.initBuilderStub(dataQualityOverviews,
                UsagePointDataQualityService.DataQualityOverviewBuilder.class,
                UsagePointDataQualityService.MetricSpecificationBuilder.class);
        when(usagePointDataQualityService.forAllUsagePoints()).thenReturn(overviewBuilder);
        when(dataQualityOverviews.allOverviews()).thenReturn(Collections.singletonList(dataQualityOverview));
        DataQualityKpiResults dataQualityKpiResults = mockDataQualityKpiResults();
        when(dataQualityOverview.getUsagePointName()).thenReturn(USAGEPOINT_NAME);
        when(dataQualityOverview.getServiceKind()).thenReturn(SERVICE_KIND);
        HasId metrologyConfiguration = mockIdWithName(METROLOGY_CONFIGURATION_ID, METROLOGY_CONFIGURATION_NAME);
        when(dataQualityOverview.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(dataQualityOverview.getMetrologyPurposeId()).thenReturn(METROLOGY_PURPOSE_ID);
        when(dataQualityOverview.getDataQualityKpiResults()).thenReturn(dataQualityKpiResults);

        mockServiceCategory();
        mockMetrologyPurpose();
        mockValidators();
        mockEstimators();
    }

    private ServiceCategory mockServiceCategory() {
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(SERVICE_KIND)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getName()).thenReturn(SERVICE_KIND.getDefaultFormat());
        return serviceCategory;
    }

    private MetrologyPurpose mockMetrologyPurpose() {
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyPurpose.getId()).thenReturn(METROLOGY_PURPOSE_ID);
        when(metrologyPurpose.getName()).thenReturn(METROLOGY_PURPOSE_NAME);
        when(metrologyConfigurationService.findMetrologyPurpose(METROLOGY_PURPOSE_ID)).thenReturn(Optional.of(metrologyPurpose));
        return metrologyPurpose;
    }

    private <H extends HasId & HasName> H mockIdWithName(long id, String name) {
        Object mock = mock(Object.class, withSettings().extraInterfaces(HasId.class, HasName.class));
        when(((HasId) mock).getId()).thenReturn(id);
        when(((HasName) mock).getName()).thenReturn(name);
        return (H) mock;
    }

    private void mockValidators() {
        when(validator_1.getDisplayName()).thenReturn(VALIDATOR_1);
        when(validator_2.getDisplayName()).thenReturn(VALIDATOR_2);
        when(validationService.getAvailableValidators(QualityCodeSystem.MDM)).thenReturn(Arrays.asList(validator_1, validator_2));
    }

    private void mockEstimators() {
        when(estimator_1.getDisplayName()).thenReturn(ESTIMATOR_1);
        when(estimator_2.getDisplayName()).thenReturn(ESTIMATOR_2);
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDM)).thenReturn(Arrays.asList(estimator_1, estimator_2));
    }

    @Test
    public void getDataQualityOverview() {
        // Business method
        String response = target("/dataQualityResults").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.dataQualityResults[0].usagePointName")).isEqualTo(USAGEPOINT_NAME);
        assertThat(jsonModel.<String>get("$.dataQualityResults[0].serviceCategory")).isEqualTo(SERVICE_KIND.getDefaultFormat());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].metrologyConfiguration.id")).isEqualTo(METROLOGY_CONFIGURATION_ID.intValue());
        assertThat(jsonModel.<String>get("$.dataQualityResults[0].metrologyConfiguration.name")).isEqualTo(METROLOGY_CONFIGURATION_NAME);
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].metrologyPurpose.id")).isEqualTo(METROLOGY_PURPOSE_ID.intValue());
        assertThat(jsonModel.<String>get("$.dataQualityResults[0].metrologyPurpose.name")).isEqualTo(METROLOGY_PURPOSE_NAME);
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfSuspects")).isEqualTo(TOTAL_SUSPECTS.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfConfirmed")).isEqualTo(AMOUNT_OF_CONFIRMED.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfEstimates")).isEqualTo(AMOUNT_OF_ESTIMATES.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfInformatives")).isEqualTo(AMOUNT_OF_INFORMATIVES.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfAdded")).isEqualTo(AMOUNT_OF_ADDED.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfEdited")).isEqualTo(AMOUNT_OF_EDITED.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfRemoved")).isEqualTo(AMOUNT_OF_REMOVED.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].amountOfTotalEdited"))
                .isEqualTo(AMOUNT_OF_ADDED.intValue() + AMOUNT_OF_EDITED.intValue() + AMOUNT_OF_REMOVED.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].channelSuspects")).isEqualTo(CHANNEL_SUSPECTS.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].registerSuspects")).isEqualTo(REGISTER_SUSPECTS.intValue());
        assertThat(jsonModel.<Number>get("$.dataQualityResults[0].lastSuspect")).isEqualTo(LAST_SUSPECT.toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$.dataQualityResults[0].suspectsPerValidator[*].name")).containsExactly("V1", "V2");
        assertThat(jsonModel.<List<Number>>get("$.dataQualityResults[0].suspectsPerValidator[*].value"))
                .containsExactly(SUSPECTS_BY_VALIDATOR_1.intValue(), SUSPECTS_BY_VALIDATOR_2.intValue());
        assertThat(jsonModel.<List<String>>get("$.dataQualityResults[0].estimatesPerEstimator[*].name")).containsExactly("E1", "E2");
        assertThat(jsonModel.<List<Number>>get("$.dataQualityResults[0].estimatesPerEstimator[*].value"))
                .containsExactly(ESTIMATES_BY_ESTIMATOR_1.intValue(), ESTIMATES_BY_ESTIMATOR_2.intValue());
    }

    private DataQualityKpiResults mockDataQualityKpiResults() {
        DataQualityKpiResults results = mock(DataQualityKpiResults.class);

        when(results.getRegisterSuspects()).thenReturn(REGISTER_SUSPECTS);
        when(results.getChannelSuspects()).thenReturn(CHANNEL_SUSPECTS);
        when(results.getAmountOfSuspects()).thenReturn(TOTAL_SUSPECTS);

        when(results.getAmountOfConfirmed()).thenReturn(AMOUNT_OF_CONFIRMED);
        when(results.getAmountOfEstimates()).thenReturn(AMOUNT_OF_ESTIMATES);
        when(results.getAmountOfInformatives()).thenReturn(AMOUNT_OF_INFORMATIVES);

        when(results.getAmountOfAdded()).thenReturn(AMOUNT_OF_ADDED);
        when(results.getAmountOfEdited()).thenReturn(AMOUNT_OF_EDITED);
        when(results.getAmountOfRemoved()).thenReturn(AMOUNT_OF_REMOVED);

        when(results.getAmountOfSuspectsBy(validator_1)).thenReturn(SUSPECTS_BY_VALIDATOR_1);
        when(results.getAmountOfSuspectsBy(validator_2)).thenReturn(SUSPECTS_BY_VALIDATOR_2);

        when(results.getAmountOfEstimatesBy(estimator_1)).thenReturn(ESTIMATES_BY_ESTIMATOR_1);
        when(results.getAmountOfEstimatesBy(estimator_2)).thenReturn(ESTIMATES_BY_ESTIMATOR_2);

        when(results.getLastSuspect()).thenReturn(LAST_SUSPECT);
        return results;
    }

    @Test
    public void getDataQualityOverviewFiltered() throws UnsupportedEncodingException {
        UsagePointGroup usagePointGroup = mockUsagePointGroup(USAGEPOINT_GROUP_ID);
        MetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(METROLOGY_CONFIGURATION_ID);
        MetrologyPurpose metrologyPurpose = mockMetrologyPurpose(METROLOGY_PURPOSE_ID);

        ExtjsFilter.FilterBuilder filter = ExtjsFilter.filter()
                .property("usagePointGroup", Collections.singletonList(USAGEPOINT_GROUP_ID))
                .property("metrologyConfiguration", Collections.singletonList(METROLOGY_CONFIGURATION_ID))
                .property("metrologyPurpose", Collections.singletonList(METROLOGY_PURPOSE_ID))
                .property("from", Instant.EPOCH.toEpochMilli())
                .property("to", Instant.EPOCH.plusSeconds(1).toEpochMilli())
                .property("readingQuality", Arrays.asList("suspects", "estimates"))
                .property("validator", Collections.singletonList(validator_1.getClass().getName()))
                .property("estimator", Collections.singletonList(estimator_1.getClass().getName()));

        // Business method
        Response response = target("/dataQualityResults")
                .queryParam("filter", filter.create())
                .queryParam("start", 10)
                .queryParam("limit", 12)
                .request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(overviewBuilder).in(eq(new HashSet<>(Collections.singleton(usagePointGroup))));
        verify(overviewBuilder).of(eq(new HashSet<>(Collections.singleton(metrologyConfiguration))));
        verify(overviewBuilder).with(eq(new HashSet<>(Collections.singleton(metrologyPurpose))));
        verify(overviewBuilder).in(eq(Ranges.closed(Instant.EPOCH, Instant.EPOCH.plusSeconds(1))));
        verify(overviewBuilder).havingSuspects();
        verify(overviewBuilder).havingEstimates();
        verify(overviewBuilder).suspectedBy(eq(Arrays.asList(validator_1)));
        verify(overviewBuilder).estimatedBy(eq(Arrays.asList(estimator_1)));
        verify(overviewBuilder).paged(11, 23);
    }

    private UsagePointGroup mockUsagePointGroup(long usagePointGroupId) {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(meteringGroupsService.findUsagePointGroup(usagePointGroupId)).thenReturn(Optional.of(usagePointGroup));
        return usagePointGroup;
    }

    private MetrologyConfiguration mockMetrologyConfiguration(Long metrologyConfigurationId) {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigurationId)).thenReturn(Optional.of(metrologyConfiguration));
        return metrologyConfiguration;
    }

    private MetrologyPurpose mockMetrologyPurpose(long metrologyPurposeId) {
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyConfigurationService.findMetrologyPurpose(metrologyPurposeId)).thenReturn(Optional.of(metrologyPurpose));
        return metrologyPurpose;
    }

    @Test
    public void getDataQualityOverviewFilteredByAmounts() throws UnsupportedEncodingException {
        //@formatter:off
        String filter = asJsonFilter(
                "[" +
                        "{'property':'amountOfSuspects','value':{'operator':'=','criteria':10}}," +
                        "{'property':'amountOfConfirmed','value':{'operator':'=','criteria':10}}," +
                        "{'property':'amountOfEstimates','value':{'operator':'=','criteria':10}}," +
                        "{'property':'amountOfInformatives','value':{'operator':'=','criteria':10}}," +
                        "{'property':'amountOfEdited','value':{'operator':'=','criteria':10}}" +
                "]");
        //@formatter:on

        // Business method
        Response response = target("/dataQualityResults")
                .queryParam("filter", filter)
                .request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(overviewBuilder).withSuspectsAmount();
        verify(overviewBuilder).withConfirmedAmount();
        verify(overviewBuilder).withEstimatesAmount();
        verify(overviewBuilder).withInformativesAmount();
        verify(overviewBuilder).withEditedAmount();
        verify((UsagePointDataQualityService.MetricSpecificationBuilder) overviewBuilder, times(5)).equalTo(10);
    }

    private String asJsonFilter(String filter) throws UnsupportedEncodingException {
        return URLEncoder.encode(filter.replace('\'', '"'), "UTF-8");
    }
}