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
import com.elster.jupiter.metering.ReadingType;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 2/13/15.
 */
public class ReadingTypeFieldResourceTest extends MeteringApplicationJerseyTest {

    @Test
    public void testGetReadingTypesUnpaged() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(31);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(31);
    }

    @Test
    public void testGetReadingTypesFilteredWithoutMRid() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 31).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes")
            .queryParam("filter", ExtjsFilter.filter().property("tou", "0").create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
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
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
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
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(11);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    @Test
    public void testGetReadingTypesPageTwo() throws Exception {
        List<ReadingType> collect = new ArrayList<>();
        IntStream.range(0, 30).forEach(i -> collect.add(mockReadingType("power " + i)));

        when(meteringService.getAvailableReadingTypes()).thenReturn(collect);
        Response response = target("/fields/readingtypes").queryParam("start", 10).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(21);
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(10);
    }

    private ReadingType mockReadingType(String name) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getName()).thenReturn(name);
        when(readingType.getMRID()).thenReturn("mrid+"+name);
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
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK15MIN);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPEREHOUR);
        when(readingType.getVersion()).thenReturn(1L);
        return readingType;
    }
}
