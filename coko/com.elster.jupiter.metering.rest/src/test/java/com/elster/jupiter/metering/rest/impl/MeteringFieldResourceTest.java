/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeteringFieldResourceTest extends MeteringApplicationJerseyTest {

    @Test
    public void testGetReadingTypesUnpaged() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(31);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(31);
    }

    @Test
    public void testGetReadingTypesIsSortedByFullAliasName() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i, TimeAttribute.values()[i], "alias", 0, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes[*].fullAliasName")).isSorted();
    }

    @Test
    public void testGetReadingTypesFilteredWithoutMRid() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("tou", 0L).create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(31);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(31);
    }

    @Test
    public void testGetReadingTypesFilteredWithMRid() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("mRID", "30").create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(1);
    }

    @Test
    public void testGetReadingTypesPageOne() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 30).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(30);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testGetReadingTypesPageTwo() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 30).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").queryParam("start", 10).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(30);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testFilteredByName() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i, TimeAttribute.values()[i], (i % 2 == 0 ? "Bulk A+ " : "Bulk A-") + i, 0, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("name", "A+").create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes[*].aliasName")).hasSize(16);
    }

    @Test
    public void testFilteredByNameCaseInsensitive() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithAlias("power " + i, (i % 2 == 0 ? "Bulk A+ " : "Bulk A-") + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("name", "bulK").create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes[*].aliasName")).hasSize(31);
    }

    @Test
    public void testFilteredByTou() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithTou("power " + i, i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("tou", 21L).create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes[*].tou")).containsOnly(21).hasSize(1);
    }

    @Test
    public void testFilteredByUnitOfMeasure() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithUnitOfMeasure("power " + i, ReadingTypeUnit.values()[i])));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("unitOfMeasure", (long) ReadingTypeUnit.AMPERE.getId()).create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(1);
    }

    @Test
    public void testFilteredByTime() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithTime("power " + i, i < 10 ? TimeAttribute.FIXEDBLOCK10MIN : TimeAttribute.FIXEDBLOCK15MIN)));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("time", (long) TimeAttribute.FIXEDBLOCK10MIN.getId()).create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testFilteredByCombination() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithMultiplier("power " + i, i < 10 ? MetricMultiplier.KILO : MetricMultiplier.GIGA)));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("time", (long) TimeAttribute.FIXEDBLOCK15MIN.getId()).property("multiplier", (long) MetricMultiplier.KILO.getMultiplier()).create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testFilteredByNonExistingCombination() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithMultiplier("power " + i, i < 10 ? MetricMultiplier.KILO : MetricMultiplier.GIGA)));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().
                        property("time", (long) TimeAttribute.FIXEDBLOCK15MIN.getId()).
                        property("multiplier", (long) MetricMultiplier.KILO.getMultiplier()).
                        property("tou", 666L).create()).
                        request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes")).isEmpty();
    }

    @Test
    public void testFilteredByMultiplier() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingTypeWithMultiplier("power " + i, i < 10 ? MetricMultiplier.KILO : MetricMultiplier.GIGA)));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
                .queryParam("filter", ExtjsFilter.filter().property("multiplier", (long) MetricMultiplier.KILO.getMultiplier()).create()).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testGetUnitsSorted() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        collect.add(mockReadingTypeWithUnitOfMeasure("Rocket impact", ReadingTypeUnit.DEGREES));
        collect.add(mockReadingType("Bulk A+", TimeAttribute.FIXEDBLOCK15MIN, "alias", 1, ReadingTypeUnit.AMPERE, MetricMultiplier.MEGA));
        collect.add(mockReadingTypeWithUnitOfMeasure("Distance", ReadingTypeUnit.METER));
        collect.add(mockReadingTypeWithUnitOfMeasure("Weight", ReadingTypeUnit.GRAM));
        collect.add(mockReadingTypeWithUnitOfMeasure("Doel", ReadingTypeUnit.ROENTGEN));
        collect.add(mockReadingTypeWithUnitOfMeasure("Acceleration", ReadingTypeUnit.SECONDPERSECOND));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);

        Response response = target("/fields/unitsofmeasure").request().get();

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(6);
        assertThat(jsonModel.<List<String>>get("$.unitsOfMeasure[*].name")).isSortedAccordingTo(String::compareToIgnoreCase).hasSize(6);
    }

    @Test
    public void testGetUnitsSortedWithDuplicates() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        collect.add(mockReadingTypeWithUnitOfMeasure("Rocket impact", ReadingTypeUnit.DEGREES));
        collect.add(mockReadingTypeWithUnitOfMeasure("Distance", ReadingTypeUnit.METER));
        collect.add(mockReadingType("Bulk A+", TimeAttribute.FIXEDBLOCK15MIN, "alias", 1, ReadingTypeUnit.AMPERE, MetricMultiplier.ZERO));
        collect.add(mockReadingTypeWithUnitOfMeasure("Distance", ReadingTypeUnit.METER));
        collect.add(mockReadingTypeWithUnitOfMeasure("Doel", ReadingTypeUnit.ROENTGEN));
        collect.add(mockReadingTypeWithUnitOfMeasure("Weight", ReadingTypeUnit.GRAM));
        collect.add(mockReadingTypeWithUnitOfMeasure("Doel", ReadingTypeUnit.ROENTGEN));
        collect.add(mockReadingTypeWithUnitOfMeasure("Acceleration", ReadingTypeUnit.SECONDPERSECOND));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);

        Response response = target("/fields/unitsofmeasure").request().get();

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(6);
        assertThat(jsonModel.<List<String>>get("$.unitsOfMeasure[*].name")).isSortedAccordingTo(String::compareToIgnoreCase).hasSize(6);
        assertThat(jsonModel.<String>get("$.unitsOfMeasure[0].name")).isEqualTo("A");
        assertThat(jsonModel.<Integer>get("$.unitsOfMeasure[0].multiplier")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.unitsOfMeasure[0].unit")).isEqualTo(5);
    }

    @Test
    public void testGetUnitsAssembly() throws Exception {
        List<ReadingType> collect = new ArrayList<>();

        collect.add(mockReadingType("Bulk A+", TimeAttribute.FIXEDBLOCK15MIN, "alias", 1, ReadingTypeUnit.WATT, MetricMultiplier.GIGA));
        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);

        Response response = target("/fields/unitsofmeasure").request().get();

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.unitsOfMeasure[0].name")).isEqualTo("GW");
        assertThat(jsonModel.<Integer>get("$.unitsOfMeasure[0].multiplier")).isEqualTo(9);
        assertThat(jsonModel.<Integer>get("$.unitsOfMeasure[0].unit")).isEqualTo(38);
    }

    protected ReadingType mockReadingType(String name) {
        return mockReadingType(name, TimeAttribute.FIXEDBLOCK15MIN, "alias", 0, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO);
    }

    private ReadingType mockReadingTypeWithAlias(String name, String alias) {
        return mockReadingType(name, TimeAttribute.FIXEDBLOCK5MIN, alias, 0, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO);
    }

    private ReadingType mockReadingTypeWithTou(String name, int tou) {
        return mockReadingType(name, TimeAttribute.FIXEDBLOCK15MIN, "alias", tou, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO);
    }

    private ReadingType mockReadingTypeWithTime(String name, TimeAttribute time) {
        return mockReadingType(name, time, "alias", 0, ReadingTypeUnit.AMPEREHOUR, MetricMultiplier.KILO);
    }

    private ReadingType mockReadingTypeWithUnitOfMeasure(String name, ReadingTypeUnit unit) {
        return mockReadingType(name, TimeAttribute.FIXEDBLOCK15MIN, "alias", 0, unit, MetricMultiplier.KILO);
    }

    private ReadingType mockReadingTypeWithMultiplier(String name, MetricMultiplier multiplier) {
        return mockReadingType(name, TimeAttribute.FIXEDBLOCK15MIN, "alias", 0, ReadingTypeUnit.AMPEREHOUR, multiplier);
    }

    private ReadingType mockReadingType(String name, TimeAttribute timeAttribute, String aliasName, int tou, ReadingTypeUnit unit, MetricMultiplier multiplier) {
        ReadingType readingType = mock(ReadingType.class);
        Phase phasea = Phase.PHASEA;
        when(readingType.getName()).thenReturn(name);
        when(readingType.getAliasName()).thenReturn(aliasName);
        when(readingType.getMRID()).thenReturn("mrid+" + name);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getAccumulation()).thenReturn(Accumulation.CUMULATIVE);
        when(readingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(readingType.getCommodity()).thenReturn(Commodity.CO2);
        when(readingType.getConsumptionTier()).thenReturn(1);
        when(readingType.getCpp()).thenReturn(2);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("USD"));
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ALARM);
        when(readingType.getMeasuringPeriod()).thenReturn(timeAttribute);
        when(readingType.getMultiplier()).thenReturn(multiplier);
        when(readingType.getPhases()).thenReturn(phasea);
        when(readingType.getTou()).thenReturn(tou);
        when(readingType.getUnit()).thenReturn(unit);
        when(readingType.getVersion()).thenReturn(1L);
        when(readingType.getFullAliasName()).thenReturn("[" + timeAttribute + "]" + aliasName + " " + unit + " " + phasea + " " + tou);
        when(readingType.isActive()).thenReturn(true);
        return readingType;
    }

    private <T> Collector<T, List<T>, T[]> toArray() {
        return Collector.of(() -> new ArrayList<>(), (list, obj) -> list.add(obj), (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        }, list -> (T[]) list.toArray());
    }

    @Test
    public void testGetConnectionStates() throws Exception {
        Response response = target("/fields/connectionstates").request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(UsagePointConnectedKind.values().length);
        assertThat(jsonModel.<List>get("$.connectionStates[*].id"))
                .containsOnly(Arrays.stream(UsagePointConnectedKind.values()).map(connectionKind -> connectionKind.name()).collect(toArray()));
        assertThat(jsonModel.<List>get("$.connectionStates[*].displayValue"))
                .containsOnly(Arrays.stream(UsagePointConnectedKind.values()).map(connectionKind -> connectionKind.getDefaultFormat()).collect(toArray()));
    }

    @Test
    public void testGetAmiBillings() throws Exception {
        Response response = target("/fields/amibilling").request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(AmiBillingReadyKind.values().length);
        assertThat(jsonModel.<List>get("$.amiBillings[*].id"))
                .containsOnly(Arrays.stream(AmiBillingReadyKind.values()).map(billingKind -> billingKind.name()).collect(toArray()));
        assertThat(jsonModel.<List>get("$.amiBillings[*].displayValue"))
                .containsOnly(Arrays.stream(AmiBillingReadyKind.values()).map(billingKind -> billingKind.getDefaultFormat()).collect(toArray()));
    }

    @Test
    public void testGetServiceCategories() throws Exception {
        Response response = target("/fields/servicecategory").request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(ServiceKind.values().length);
        assertThat(jsonModel.<List>get("$.categories[*].id"))
                .containsOnly(Arrays.stream(ServiceKind.values()).map(serviceKind -> serviceKind.name()).collect(toArray()));
        assertThat(jsonModel.<List>get("$.categories[*].displayValue"))
                .containsOnly(Arrays.stream(ServiceKind.values()).map(serviceKind -> serviceKind.getDefaultFormat()).collect(toArray()));
    }

    @Test
    public void testGetMetrologyConfigurations() {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(13L);
        when(metrologyConfiguration.getName()).thenReturn("Metrology configuration");
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Collections.singletonList(metrologyConfiguration));

        // Business method
        String response = target("/fields/metrologyconfigurations").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.metrologyConfigurations[*].id")).containsExactly(13);
        assertThat(jsonModel.<List<String>>get("$.metrologyConfigurations[*].name")).containsExactly("Metrology configuration");
    }

    @Test
    public void testGetMetrologyPurposes() {
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyPurpose.getId()).thenReturn(12L);
        when(metrologyPurpose.getName()).thenReturn("Metrology purpose");
        when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Collections.singletonList(metrologyPurpose));

        // Business method
        String response = target("/fields/metrologypurposes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.metrologyPurposes[*].id")).containsExactly(12);
        assertThat(jsonModel.<List<String>>get("$.metrologyPurposes[*].name")).containsExactly("Metrology purpose");
    }
}
