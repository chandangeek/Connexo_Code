/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFieldsFactory;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadingTypeResourceTest extends MeteringApplicationJerseyTest {


    @Test
    public void testGetCalculatedReadingType_forUnexistingMrid() {
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.empty());
        Response response = target("/readingtypes/unexisting/calculated").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetCalculatedReadingType_forCorrectMrid() {
        ReadingType type = mockReadingType("0.0.0.1");
        ReadingType calcType = mockReadingType("0.0.0.4");
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.of(type));
        when(type.getCalculatedReadingType()).thenReturn(Optional.of(calcType));
        String response = target("/readingtypes/0.0.0.1/calculated").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.readingTypes")).hasSize(1);
        assertThat(model.<String>get("$.readingTypes[0].mRID")).isEqualTo("0.0.0.4");
    }

    @Test
    public void testGetCalculatedReadingType_noCalculatedMrid() {
        ReadingType type = mockReadingType("0.0.0.1");
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.of(type));
        when(type.getCalculatedReadingType()).thenReturn(Optional.empty());
        String response = target("/readingtypes/0.0.0.1/calculated").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.readingTypes")).hasSize(0);
    }

    @Test
    public void testGetCodes() throws Exception {
        ReadingTypeFieldsFactory fieldsFactory = mock(ReadingTypeFieldsFactory.class);
        when(fieldsFactory.getCodeFields("macroPeriod"))
                .thenReturn(Arrays.stream(MacroPeriod.values())
                        .filter(e -> e.getId() != 0)
                        .collect(Collectors.toMap(MacroPeriod::getId, MacroPeriod::getDescription)));
        when(meteringService.getReadingTypeFieldCodesFactory()).thenReturn(fieldsFactory);
        String response = target("/readingtypes/codes/macroPeriod").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(Arrays.asList(MacroPeriod.values()).size() - 1);
        assertThat(jsonModel.<List<?>>get("$.macroPeriodCodes")).hasSize(Arrays.asList(MacroPeriod.values()).size() - 1);
    }

    @Test
    public void testCreateReadingTypeCount() throws Exception {
        CreateReadingTypeInfo info = new CreateReadingTypeInfo();
        info.accumulation = new ArrayList<>();
        info.aggregate = new ArrayList<>();
        info.argumentDenominator = new ArrayList<>();
        info.argumentNumerator = new ArrayList<>();
        info.commodity = new ArrayList<>();
        info.consumptionTier = new ArrayList<>();
        info.cpp = new ArrayList<>();
        info.currency = new ArrayList<>();
        info.flowDirection = new ArrayList<>();
        info.interHarmonicDenominator = new ArrayList<>();
        info.interHarmonicNumerator = new ArrayList<>();
        info.macroPeriod = Arrays.asList(8, 11, 13, 22);
        info.measurementKind = new ArrayList<>();
        info.measuringPeriod = Arrays.asList(1, 2);
        info.metricMultiplier = new ArrayList<>();
        info.phases = new ArrayList<>();
        info.tou = new ArrayList<>();
        info.unit = new ArrayList<>();
        info.aliasName = "test";

        Response response = target("/readingtypes/count").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.countReadingTypesToCreate")).isEqualTo(8);
    }

    @Test
    public void testCreateReadingType() throws Exception {
        ReadingType type = mockReadingType("0.0.0.1");
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.of(type));
        CreateReadingTypeInfo info = new CreateReadingTypeInfo();
        info.accumulation = new ArrayList<>();
        info.aggregate = new ArrayList<>();
        info.argumentDenominator = new ArrayList<>();
        info.argumentNumerator = new ArrayList<>();
        info.commodity = new ArrayList<>();
        info.consumptionTier = new ArrayList<>();
        info.cpp = new ArrayList<>();
        info.currency = new ArrayList<>();
        info.flowDirection = new ArrayList<>();
        info.interHarmonicDenominator = new ArrayList<>();
        info.interHarmonicNumerator = new ArrayList<>();
        info.macroPeriod = Arrays.asList(8, 11, 13, 22);
        info.measurementKind = new ArrayList<>();
        info.measuringPeriod = Arrays.asList(1, 2);
        info.metricMultiplier = new ArrayList<>();
        info.phases = new ArrayList<>();
        info.tou = new ArrayList<>();
        info.unit = new ArrayList<>();
        info.aliasName = "test";

        Response response = target("/readingtypes").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testFilterReadingTypesByMetrologyConfiguration() throws UnsupportedEncodingException {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfigurationService.findMetrologyConfiguration(13L)).thenReturn(Optional.of(metrologyConfiguration));
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        List<ReadingTypeDeliverable> readingTypeDeliverables = Collections.singletonList(deliverable);
        when(metrologyConfiguration.getDeliverables()).thenReturn(readingTypeDeliverables);
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(deliverable.getReadingType()).thenReturn(readingType);
        Finder finder = mock(Finder.class);
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.empty());

        String filter = ExtjsFilter.filter().property("metrologyConfiguration", 13L).create();

        // Business method
        Response response = target("/readingtypes").queryParam("filter", filter).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Condition condition = readingTypeFilterArgumentCaptor.getValue().getCondition();
        assertThat(condition).isNotNull();
        assertThat(condition).isInstanceOf(Contains.class);
        Contains contains = (Contains) condition;
        assertThat(contains.getFieldName()).isEqualTo("mRID");
        assertThat(contains.getOperator()).isEqualTo(ListOperator.IN);
        assertThat(contains.getCollection()).contains("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
    }

    @Test
    public void testFilterReadingTypesByMetrologyPurpose() throws UnsupportedEncodingException {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Collections.singletonList(metrologyConfiguration));
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(deliverable.getReadingType()).thenReturn(readingType);

        MetrologyPurpose purpose1 = mock(MetrologyPurpose.class);
        MetrologyPurpose purpose2 = mock(MetrologyPurpose.class);
        MetrologyContract contract1 = mock(MetrologyContract.class);
        when(contract1.getMetrologyPurpose()).thenReturn(purpose1);
        MetrologyContract contract2 = mock(MetrologyContract.class);
        when(contract2.getMetrologyPurpose()).thenReturn(purpose2);
        List<ReadingTypeDeliverable> readingTypeDeliverables = Collections.singletonList(deliverable);
        when(contract2.getDeliverables()).thenReturn(readingTypeDeliverables);
        when(metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(contract1, contract2));
        when(metrologyConfigurationService.findMetrologyPurpose(12L)).thenReturn(Optional.of(purpose2));

        Finder finder = mock(Finder.class);
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.empty());

        String filter = ExtjsFilter.filter().property("metrologyPurpose", 12L).create();

        // Business method
        Response response = target("/readingtypes").queryParam("filter", filter).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Condition condition = readingTypeFilterArgumentCaptor.getValue().getCondition();
        assertThat(condition).isNotNull();
        assertThat(condition).isInstanceOf(Contains.class);
        Contains contains = (Contains) condition;
        assertThat(contains.getFieldName()).isEqualTo("mRID");
        assertThat(contains.getOperator()).isEqualTo(ListOperator.IN);
        assertThat(contains.getCollection()).contains("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
    }

    @Test
    public void testFilterReadingTypesByMetrologyConfigurationAndMetrologyPurpose() throws UnsupportedEncodingException {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfigurationService.findMetrologyConfiguration(13L)).thenReturn(Optional.of(metrologyConfiguration));
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(deliverable.getReadingType()).thenReturn(readingType);
        List<ReadingTypeDeliverable> readingTypeDeliverables = Collections.singletonList(deliverable);
        when(metrologyConfiguration.getDeliverables()).thenReturn(readingTypeDeliverables);

        MetrologyPurpose purpose1 = mock(MetrologyPurpose.class);
        MetrologyPurpose purpose2 = mock(MetrologyPurpose.class);
        MetrologyContract contract1 = mock(MetrologyContract.class);
        when(contract1.getMetrologyPurpose()).thenReturn(purpose1);
        MetrologyContract contract2 = mock(MetrologyContract.class);
        when(contract2.getMetrologyPurpose()).thenReturn(purpose2);
        when(contract2.getDeliverables()).thenReturn(readingTypeDeliverables);
        when(metrologyConfiguration.getContracts()).thenReturn(Arrays.asList(contract1, contract2));
        when(metrologyConfigurationService.findMetrologyPurpose(12L)).thenReturn(Optional.of(purpose2));

        Finder finder = mock(Finder.class);
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.empty());

        String filter = ExtjsFilter.filter()
                .property("metrologyConfiguration", 13L)
                .property("metrologyPurpose", 12L)
                .create();

        // Business method
        Response response = target("/readingtypes").queryParam("filter", filter).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        Condition condition = readingTypeFilterArgumentCaptor.getValue().getCondition();
        assertThat(condition).isNotNull();
        assertThat(condition).isInstanceOf(And.class);
        And and = (And) condition;
        and.getConditions().stream().map(Contains.class::cast)
                .forEach(contains -> {
                            assertThat(contains.getFieldName()).isEqualTo("mRID");
                            assertThat(contains.getOperator()).isEqualTo(ListOperator.IN);
                            assertThat(contains.getCollection()).contains("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
                        }
                );
    }
}
