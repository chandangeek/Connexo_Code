/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.mdm.dataquality.UsagePointDataQualityService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.Range;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataQualityOverviewFilterTest {

    private static final long USAGEPOINT_GROUP_ID_1 = 11L;
    private static final long USAGEPOINT_GROUP_ID_2 = 12L;

    private static final long METROLOGY_CONFIG_ID_1 = 31L;
    private static final long METROLOGY_CONFIG_ID_2 = 32L;

    private static final long METROLOGY_PURPOSE_ID_1 = 41L;
    private static final long METROLOGY_PURPOSE_ID_2 = 42L;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private UsagePointDataQualityService usagePointDataQualityService;
    @Mock
    private DataQualityKpiService dataQualityKpiService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private UsagePointDataQualityService.DataQualityOverviewBuilder overviewBuilder;
    @Mock
    private UsagePointDataQualityService.MetricSpecificationBuilder metricSpecificationBuilder;
    @Mock
    private UsagePointGroup usagePointGroup1, usagePointGroup2;
    @Mock
    private MetrologyConfiguration metrologyConfiguration1, metrologyConfiguration2;
    @Mock
    private MetrologyPurpose metrologyPurpose1, metrologyPurpose2;
    @Mock
    private Validator validator;
    @Mock
    private Estimator estimator;

    private ResourceHelper resourceHelper;

    @Before
    public void setUp() {
        resourceHelper = new ResourceHelper(meteringGroupsService, metrologyConfigurationService,
                validationService, estimationService, new ExceptionFactory(NlsModule.FakeThesaurus.INSTANCE));

        when(overviewBuilder.withSuspectsAmount()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.withConfirmedAmount()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.withEditedAmount()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.withEstimatesAmount()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.withInformativesAmount()).thenReturn(metricSpecificationBuilder);

        // mock usage point groups
        when(meteringGroupsService.findUsagePointGroup(anyLong())).thenReturn(Optional.empty());
        when(meteringGroupsService.findUsagePointGroup(USAGEPOINT_GROUP_ID_1)).thenReturn(Optional.of(usagePointGroup1));
        when(meteringGroupsService.findUsagePointGroup(USAGEPOINT_GROUP_ID_2)).thenReturn(Optional.of(usagePointGroup2));

        // mock metrology configurations
        when(metrologyConfigurationService.findMetrologyConfiguration(anyLong())).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findMetrologyConfiguration(METROLOGY_CONFIG_ID_1)).thenReturn(Optional.of(metrologyConfiguration1));
        when(metrologyConfigurationService.findMetrologyConfiguration(METROLOGY_CONFIG_ID_2)).thenReturn(Optional.of(metrologyConfiguration2));

        // mock metrology purposes
        when(metrologyConfigurationService.findMetrologyPurpose(anyLong())).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findMetrologyPurpose(METROLOGY_PURPOSE_ID_1)).thenReturn(Optional.of(metrologyPurpose1));
        when(metrologyConfigurationService.findMetrologyPurpose(METROLOGY_PURPOSE_ID_2)).thenReturn(Optional.of(metrologyPurpose2));


        // mock validators
        when(validationService.getAvailableValidators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(validator));

        // mock estimators
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(estimator));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usagePointGroupFilter() throws Exception {
        String filter = ExtjsFilter.filter("usagePointGroup", Arrays.asList(USAGEPOINT_GROUP_ID_1, USAGEPOINT_GROUP_ID_2));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.USAGEPOINT_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).in(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(usagePointGroup1, usagePointGroup2);
    }

    @Test
    public void usagePointGroupFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("usagePointGroup", Arrays.asList("1", "", 12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "usagePointGroup", "[<usage point group id>, ...]"));

        // Business method
        DataQualityOverviewFilter.USAGEPOINT_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void usagePointGroupFilterNoSuchUsagePointGroup() throws Exception {
        String filter = ExtjsFilter.filter("usagePointGroup", Collections.singletonList(156));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_USAGEPOINT_GROUP.getDefaultFormat(), 156));

        // Business method
        DataQualityOverviewFilter.USAGEPOINT_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }


    @Test
    @SuppressWarnings("unchecked")
    public void metrologyConfigurationFilter() throws Exception {
        String filter = ExtjsFilter.filter("metrologyConfiguration", Arrays.asList(METROLOGY_CONFIG_ID_1, METROLOGY_CONFIG_ID_2));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.METROLOGY_CONFIGURATION.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).of(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(metrologyConfiguration1, metrologyConfiguration2);
    }

    @Test
    public void metrologyConfigurationFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("metrologyConfiguration", Arrays.asList("1", "", 12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "metrologyConfiguration", "[<metrology configuration id>, ...]"));

        // Business method
        DataQualityOverviewFilter.METROLOGY_CONFIGURATION.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void metrologyConfigurationFilterNoSuchUsagePointGroup() throws Exception {
        String filter = ExtjsFilter.filter("metrologyConfiguration", Collections.singletonList(156));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION.getDefaultFormat(), 156));

        // Business method
        DataQualityOverviewFilter.METROLOGY_CONFIGURATION.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    @SuppressWarnings("unchecked")
    public void metrologyPurposeFilter() throws Exception {
        String filter = ExtjsFilter.filter("metrologyPurpose", Arrays.asList(METROLOGY_PURPOSE_ID_1, METROLOGY_PURPOSE_ID_2));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.METROLOGY_PURPOSE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).with(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(metrologyPurpose1, metrologyPurpose2);
    }

    @Test
    public void metrologyPurposeFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("metrologyPurpose", Arrays.asList("1", "", 12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "metrologyPurpose", "[<metrology purpose id>, ...]"));

        // Business method
        DataQualityOverviewFilter.METROLOGY_PURPOSE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void metrologyPurposeFilterNoSuchUsagePointGroup() throws Exception {
        String filter = ExtjsFilter.filter("metrologyPurpose", Collections.singletonList(156));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE.getDefaultFormat(), 156));

        // Business method
        DataQualityOverviewFilter.METROLOGY_PURPOSE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void periodFilter() throws Exception {
        Instant from = Instant.now();
        Instant to = Instant.now();
        String filter = ExtjsFilter.filter()
                .property("from", from.toEpochMilli())
                .property("to", to.toEpochMilli())
                .create();
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.PERIOD.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(overviewBuilder).in(Range.openClosed(from, to));
    }

    @Test
    public void periodFilterOnlyFromSpecified() throws Exception {
        Instant from = Instant.now();
        String filter = ExtjsFilter.filter()
                .property("from", from.toEpochMilli())
                .create();
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.PERIOD.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(overviewBuilder).in(Range.greaterThan(from));
    }

    @Test
    public void periodFilterOnlyToSpecified() throws Exception {
        Instant to = Instant.now();
        String filter = ExtjsFilter.filter()
                .property("to", to.toEpochMilli())
                .create();
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.PERIOD.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(overviewBuilder).in(Range.atMost(to));
    }

    @Test
    public void readingQualityFilter() throws Exception {
        String filter = ExtjsFilter.filter("readingQuality", Arrays.asList("suspects", "confirmed", "estimates", "informatives", "edited"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.READING_QUALITY.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(overviewBuilder).havingSuspects();
        verify(overviewBuilder).havingConfirmed();
        verify(overviewBuilder).havingEstimates();
        verify(overviewBuilder).havingInformatives();
        verify(overviewBuilder).havingEdited();
    }

    @Test
    public void readingQualityFilterUnknownType() throws Exception {
        String filter = ExtjsFilter.filter("readingQuality", Collections.singletonList("suspects-123"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "readingQuality",
                "[suspects, confirmed, estimates, informatives, edited]"));

        // Business method
        DataQualityOverviewFilter.READING_QUALITY.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    @SuppressWarnings("unchecked")
    public void validatorFilter() throws Exception {
        String filter = ExtjsFilter.filter("validator", Collections.singletonList(validator.getClass().getName()));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.VALIDATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).suspectedBy(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(validator);
    }

    @Test
    public void validatorFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("validator", Collections.singletonList(12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "validator", "[<validator>, ...]"));

        // Business method
        DataQualityOverviewFilter.VALIDATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void validatorFilterNoSuchValidator() throws Exception {
        String filter = ExtjsFilter.filter("validator", Collections.singletonList("unknown validator"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_VALIDATOR.getDefaultFormat(), "unknown validator"));

        // Business method
        DataQualityOverviewFilter.VALIDATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    @SuppressWarnings("unchecked")
    public void estimatorFilter() throws Exception {
        String filter = ExtjsFilter.filter("estimator", Collections.singletonList(estimator.getClass().getName()));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.ESTIMATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).estimatedBy(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(estimator);
    }

    @Test
    public void estimatorFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("estimator", Collections.singletonList(12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "estimator", "[<estimator>, ...]"));

        // Business method
        DataQualityOverviewFilter.ESTIMATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void estimatorFilterNoSuchValidator() throws Exception {
        String filter = ExtjsFilter.filter("estimator", Collections.singletonList("unknown estimator"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_ESTIMATOR.getDefaultFormat(), "unknown estimator"));

        // Business method
        DataQualityOverviewFilter.ESTIMATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::withSuspectsAmount);
    }

    @Test
    public void amountOfConfirmedEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::withConfirmedAmount);
    }

    @Test
    public void amountOfEstimatesEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::withEstimatesAmount);
    }

    @Test
    public void amountOfInformativesEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::withInformativesAmount);
    }

    @Test
    public void amountOfEditedEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::withEditedAmount);
    }

    private void applyAmountEqualsToFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property': '" + amountFilter.jsonName() + "', 'value': {'operator': '=', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).equalTo(10);
        assertion.run();
    }

    @Test
    public void amountOfSuspectsGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::withSuspectsAmount);
    }

    @Test
    public void amountOfConfirmedGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::withConfirmedAmount);
    }

    @Test
    public void amountOfEstimatesGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::withEstimatesAmount);
    }

    @Test
    public void amountOfInformativesGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::withInformativesAmount);
    }

    @Test
    public void amountOfEditedGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::withEditedAmount);
    }

    private void applyAmountGreaterThanFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property': '" + amountFilter.jsonName() + "', 'value': {'operator': '>', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.greaterThan(10L));
        assertion.run();
    }

    @Test
    public void amountOfSuspectsLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::withSuspectsAmount);
    }

    @Test
    public void amountOfConfirmedLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::withConfirmedAmount);
    }

    @Test
    public void amountOfEstimatesLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::withEstimatesAmount);
    }

    @Test
    public void amountOfInformativesLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::withInformativesAmount);
    }

    @Test
    public void amountOfEditedLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::withEditedAmount);
    }

    private void applyAmountLessThanFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property': '" + amountFilter.jsonName() + "', 'value': {'operator': '<', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.lessThan(10L));
        assertion.run();
    }

    @Test
    public void amountOfSuspectsBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::withSuspectsAmount);
    }

    @Test
    public void amountOfConfirmedBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::withConfirmedAmount);
    }

    @Test
    public void amountOfEstimatesBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::withEstimatesAmount);
    }

    @Test
    public void amountOfInformativesBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::withInformativesAmount);
    }

    @Test
    public void amountOfEditedBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::withEditedAmount);
    }

    private void applyAmountBetweenFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property': '" + amountFilter.jsonName() + "', 'value': {'operator': 'BETWEEN', 'criteria': [10, 100]}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.open(10L, 100L));
        assertion.run();
    }

    private JsonQueryFilter jsonQueryFilter(String filter) throws UnsupportedEncodingException {
        return new JsonQueryFilter(URLDecoder.decode(filter.replace('\'', '"'), "UTF-8"));
    }

    @Test
    public void amountOfSuspectsFilterWrongJsonFormat() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': 'BETWEEN'}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(),
                "amountOfSuspects", "{operator: " + Stream.of(DataQualityOverviewFilter.Operator.values())
                        .map(DataQualityOverviewFilter.Operator::jsonName).collect(Collectors.joining("|")) + ", criteria: <values>}"));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsFilterUnsupportedRelationalOperator() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': '>=<', 'criteria': 100}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.UNSUPPORTED_OPERATOR.getDefaultFormat(),
                "amountOfSuspects", ">=<", Stream.of(DataQualityOverviewFilter.Operator.values())
                        .map(DataQualityOverviewFilter.Operator::jsonName)
                        .collect(Collectors.joining(", "))));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsEqualsFilterCriteriaNodeIsNotNumber() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': '=', 'criteria': 'hundred'}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_OPERATOR_CRITERIA.getDefaultFormat(), "=", "amountOfSuspects"));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsLessThanFilterCriteriaNodeIsNotNumber() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': '<', 'criteria': 'hundred'}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_OPERATOR_CRITERIA.getDefaultFormat(), "<", "amountOfSuspects"));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsGreaterThanFilterCriteriaNodeIsNotNumber() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': '>', 'criteria': 'hundred'}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_OPERATOR_CRITERIA.getDefaultFormat(), ">", "amountOfSuspects"));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }

    @Test
    public void amountOfSuspectsBetweenFilterCriteriaNodeIsNotArray() throws Exception {
        String filter = "[{'property': 'amountOfSuspects', 'value': {'operator': 'BETWEEN', 'criteria': 100}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_OPERATOR_CRITERIA.getDefaultFormat(), "BETWEEN", "amountOfSuspects"));

        // Business method
        DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        // exception is thrown
    }
}
