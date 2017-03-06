/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import com.google.common.collect.Range;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataQualityOverviewFilterTest {

    private static final long DEVICE_GROUP_ID_1 = 12L;
    private static final long DEVICE_GROUP_ID_2 = 23L;

    private static final long DEVICE_TYPE_ID_1 = 13L;
    private static final long DEVICE_TYPE_ID_2 = 11L;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private DeviceDataQualityService deviceDataQualityService;
    @Mock
    private DataQualityKpiService dataQualityKpiService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceDataQualityService.DataQualityOverviewBuilder overviewBuilder;
    @Mock
    private DeviceDataQualityService.MetricSpecificationBuilder metricSpecificationBuilder;
    @Mock
    private EndDeviceGroup endDeviceGroup1, endDeviceGroup2;
    @Mock
    private DeviceType deviceType1, deviceType2;
    @Mock
    private Validator validator;
    @Mock
    private Estimator estimator;

    private ResourceHelper resourceHelper;

    @Before
    public void setUp() {
        resourceHelper = new ResourceHelper(meteringGroupsService, deviceConfigurationService,
                validationService, estimationService, new ExceptionFactory(NlsModule.FakeThesaurus.INSTANCE));

        when(overviewBuilder.suspects()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.confirmed()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.edited()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.estimates()).thenReturn(metricSpecificationBuilder);
        when(overviewBuilder.informatives()).thenReturn(metricSpecificationBuilder);

        // mock device groups
        when(meteringGroupsService.findEndDeviceGroup(anyInt())).thenReturn(Optional.empty());
        when(meteringGroupsService.findEndDeviceGroup(DEVICE_GROUP_ID_1)).thenReturn(Optional.of(endDeviceGroup1));
        when(meteringGroupsService.findEndDeviceGroup(DEVICE_GROUP_ID_2)).thenReturn(Optional.of(endDeviceGroup2));

        // mock device types
        when(deviceConfigurationService.findDeviceType(anyInt())).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID_1)).thenReturn(Optional.of(deviceType1));
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID_2)).thenReturn(Optional.of(deviceType2));

        // mock validators
        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(validator));

        // mock estimators
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(estimator));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deviceGroupFilter() throws Exception {
        String filter = ExtjsFilter.filter("deviceGroup", Arrays.asList(DEVICE_GROUP_ID_1, DEVICE_GROUP_ID_2));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.DEVICE_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).in(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(endDeviceGroup1, endDeviceGroup2);
    }

    @Test
    public void deviceGroupFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("deviceGroup", Arrays.asList("1", "", 12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "deviceGroup", "[<device group id>, ...]"));

        // Business method
        DataQualityOverviewFilter.DEVICE_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
    }

    @Test
    public void deviceGroupFilterNoSuchDeviceGroup() throws Exception {
        String filter = ExtjsFilter.filter("deviceGroup", Collections.singletonList(156));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_DEVICE_GROUP.getDefaultFormat(), 156));

        // Business method
        DataQualityOverviewFilter.DEVICE_GROUP.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deviceTypeFilter() throws Exception {
        String filter = ExtjsFilter.filter("deviceType", Arrays.asList(DEVICE_TYPE_ID_1, DEVICE_TYPE_ID_2));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        DataQualityOverviewFilter.DEVICE_TYPE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(overviewBuilder).of(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).containsOnly(deviceType1, deviceType2);
    }

    @Test
    public void deviceTypeFilterWrongJsonFormat() throws Exception {
        String filter = ExtjsFilter.filter("deviceType", Arrays.asList("1", "", 12));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.INVALID_FILTER_FORMAT.getDefaultFormat(), "deviceType", "[<device type id>, ...]"));

        // Business method
        DataQualityOverviewFilter.DEVICE_TYPE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
    }

    @Test
    public void deviceTypeFilterNoSuchDeviceGroup() throws Exception {
        String filter = ExtjsFilter.filter("deviceType", Collections.singletonList(156));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_DEVICE_TYPE.getDefaultFormat(), 156));

        // Business method
        DataQualityOverviewFilter.DEVICE_TYPE.apply(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
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
        verify(overviewBuilder).in(Range.closed(from, to));
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

        // Asserts exception is thrown
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

        // Asserts exception is thrown
    }

    @Test
    public void validatorFilterNoSuchValidator() throws Exception {
        String filter = ExtjsFilter.filter("validator", Collections.singletonList("unknown validator"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_VALIDATOR.getDefaultFormat(), "unknown validator"));

        // Business method
        DataQualityOverviewFilter.VALIDATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
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

        // Asserts exception is thrown
    }

    @Test
    public void estimatorFilterNoSuchValidator() throws Exception {
        String filter = ExtjsFilter.filter("estimator", Collections.singletonList("unknown estimator"));
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        exception.expectMessage(MessageFormat.format(MessageSeeds.NO_SUCH_ESTIMATOR.getDefaultFormat(), "unknown estimator"));

        // Business method
        DataQualityOverviewFilter.ESTIMATOR.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts exception is thrown
    }

    @Test
    public void amountOfSuspectsEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::suspects);
    }

    @Test
    public void amountOfConfirmedEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::confirmed);
    }

    @Test
    public void amountOfEstimatesEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::estimates);
    }

    @Test
    public void amountOfInformativesEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::informatives);
    }

    @Test
    public void amountOfEditedEqualToFilter() throws Exception {
        applyAmountEqualsToFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::edited);
    }

    private void applyAmountEqualsToFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property':'" + amountFilter.jsonName() + "', 'value': {'operator': '=', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).equalTo(10);
        assertion.run();
    }

    @Test
    public void amountOfSuspectsGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::suspects);
    }

    @Test
    public void amountOfConfirmedGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::confirmed);
    }

    @Test
    public void amountOfEstimatesGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::estimates);
    }

    @Test
    public void amountOfInformativesGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::informatives);
    }

    @Test
    public void amountOfEditedGreaterThanFilter() throws Exception {
        applyAmountGreaterThanFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::edited);
    }

    private void applyAmountGreaterThanFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property':'" + amountFilter.jsonName() + "', 'value': {'operator': '>', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.greaterThan(10L));
        assertion.run();
    }

    @Test
    public void amountOfSuspectsLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::suspects);
    }

    @Test
    public void amountOfConfirmedLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::confirmed);
    }

    @Test
    public void amountOfEstimatesLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::estimates);
    }

    @Test
    public void amountOfInformativesLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::informatives);
    }

    @Test
    public void amountOfEditedLessThanFilter() throws Exception {
        applyAmountLessThanFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::edited);
    }

    private void applyAmountLessThanFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property':'" + amountFilter.jsonName() + "', 'value': {'operator': '<', 'criteria': 10}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.lessThan(10L));
        assertion.run();
    }

    @Test
    public void amountOfSuspectsBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_SUSPECTS, overviewBuilder::suspects);
    }

    @Test
    public void amountOfConfirmedBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_CONFIRMED, overviewBuilder::confirmed);
    }

    @Test
    public void amountOfEstimatesBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_ESTIMATES, overviewBuilder::estimates);
    }

    @Test
    public void amountOfInformativesBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_INFORMATIVES, overviewBuilder::informatives);
    }

    @Test
    public void amountOfEditedBetweenFilter() throws Exception {
        applyAmountBetweenFilter(DataQualityOverviewFilter.AMOUNT_OF_EDITED, overviewBuilder::edited);
    }

    private void applyAmountBetweenFilter(DataQualityOverviewFilter amountFilter, Runnable assertion) throws Exception {
        String filter = "[{'property':'" + amountFilter.jsonName() + "', 'value': {'operator': 'BETWEEN', 'criteria': [10, 100]}}]";
        JsonQueryFilter jsonQueryFilter = jsonQueryFilter(filter);

        // Business method
        amountFilter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);

        // Asserts
        verify(metricSpecificationBuilder).inRange(Range.closed(10L, 100L));
        assertion.run();
    }

    private JsonQueryFilter jsonQueryFilter(String filter) throws UnsupportedEncodingException {
        return new JsonQueryFilter(URLDecoder.decode(filter.replace('\'', '"'), "UTF-8"));
    }
}
