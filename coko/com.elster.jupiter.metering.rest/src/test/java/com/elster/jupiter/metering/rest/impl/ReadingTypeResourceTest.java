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
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Currency;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadingTypeResourceTest extends MeteringApplicationJerseyTest{

    protected ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1,2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1,2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

    @Test
    public void testGetCalculatedReadingType_forUnexistingMrid(){
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.empty());
        Response response = target("/readingtypes/unexisting/calculated").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetCalculatedReadingType_forCorrectMrid(){
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
    public void testGetCalculatedReadingType_noCalculatedMrid(){
        ReadingType type = mockReadingType("0.0.0.1");
        when(meteringService.getReadingType(anyString())).thenReturn(Optional.of(type));
        when(type.getCalculatedReadingType()).thenReturn(Optional.empty());
        String response = target("/readingtypes/0.0.0.1/calculated").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.readingTypes")).hasSize(0);
    }

    @Test
    public void testGetCodes() throws Exception{
        String response = target("/readingtypes/codes/macroPeriod").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(Arrays.asList(MacroPeriod.values()).size()-1);
        assertThat(jsonModel.<List<?>>get("$.macroPeriodCodes")).hasSize(Arrays.asList(MacroPeriod.values()).size()-1);
    }

    @Test
    public void testCreateReadingTypeCount() throws Exception{
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
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.countReadingTypesToCreate")).isEqualTo(8);
    }

    @Test
    public void testCreateReadingType() throws Exception{
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
}
