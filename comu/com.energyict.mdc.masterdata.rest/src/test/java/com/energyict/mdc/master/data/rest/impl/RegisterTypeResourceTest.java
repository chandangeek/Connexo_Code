/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.master.data.rest.impl;

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
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterTypeResourceTest extends MasterDataApplicationJerseyTest {

    private static final long OK_VERSION = 11;
    private static final long BAD_VERSION = 8;
    private static final long REGISTER_ID = 1L;

    @Mock
    private ReadingType readingType;

    @Test
    public void testGetEmptyRegisterTypeList() throws Exception {
        Finder<RegisterType> finder = mockFinder(Collections.<RegisterType>emptyList());
        when(masterDataService.findAllRegisterTypes()).thenReturn(finder);

        Map<String, Object> map = target("/registertypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List)map.get("registerTypes")).isEmpty();
    }

    @Test
    public void testRegisterTypeInfoJavaScriptMappings() throws Exception {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(13L);
        when(registerType.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType)).thenReturn(true);
        when(registerType.getReadingType()).thenReturn(readingType);
        when(readingType.getAliasName()).thenReturn("register type");
        when(readingType.getMRID()).thenReturn("mrid");
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
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(readingType.isCumulative()).thenReturn(true);

        List<RegisterSpec> registerSpecs = mock(List.class);
        when(registerSpecs.size()).thenReturn(1);
        when(masterDataService.findRegisterType(13)).thenReturn(Optional.of(registerType));
        when(deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(any(DeviceType.class), any(RegisterType.class))).thenReturn(registerSpecs);

        Map<String, Object> map = target("/registertypes/13").request().get(Map.class);
        assertThat(map).hasSize(5)
            .containsKey("id")
            .containsKey("obisCode")
            .containsKey("isLinkedByDeviceType")
            .containsKey("readingType");
        assertThat((Map)map.get("readingType")).hasSize(25)
            .containsKey("mRID")
            .containsKey("aliasName")
            .containsKey("active")
            .containsKey("macroPeriod")
            .containsKey("aggregate")
            .containsKey("measuringPeriod")
            .containsKey("accumulation")
            .containsKey("flowDirection")
            .containsKey("commodity")
            .containsKey("isGasRelated")
            .containsKey("measurementKind")
            .containsKey("interHarmonicNumerator")
            .containsKey("interHarmonicDenominator")
            .containsKey("argumentNumerator")
            .containsKey("argumentDenominator")
            .containsKey("tou")
            .containsKey("cpp")
            .containsKey("consumptionTier")
            .containsKey("phases")
            .containsKey("metricMultiplier")
            .containsKey("unit")
            .containsKey("currency")
            .containsKey("version")
            .containsKey("isCumulative")
            .containsKey("names");
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    private RegisterType mockRegisterType() {
        when(readingType.getAliasName()).thenReturn("register type");
        when(readingType.getMRID()).thenReturn("mrid");
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
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        when(readingType.isCumulative()).thenReturn(true);
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(REGISTER_ID);
        when(registerType.getVersion()).thenReturn(OK_VERSION);
        when(registerType.getDescription()).thenReturn("Default description");
        when(registerType.getObisCode()).thenReturn(new ObisCode(1,2,3,4,5,1,false));
        when(registerType.getReadingType()).thenReturn(readingType);
        when(masterDataService.findRegisterType(REGISTER_ID)).thenReturn(Optional.of(registerType));
        when(masterDataService.findAndLockRegisterTypeByIdAndVersion(REGISTER_ID, OK_VERSION)).thenReturn(Optional.of(registerType));
        when(masterDataService.findAndLockRegisterTypeByIdAndVersion(REGISTER_ID, BAD_VERSION)).thenReturn(Optional.empty());
        when(meteringService.getReadingType(readingType.getMRID())).thenReturn(Optional.empty());
        return registerType;
    }

    @Test
    public void testUpdateRegisterTypeOkVersion() {
        RegisterType register = mockRegisterType();
        RegisterTypeInfo info = registerTypeInfoFactory.asInfo(register, false, false);
        Response response = target("/registertypes/" + REGISTER_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(register, times(1)).setObisCode(new ObisCode(1,2,3,4,5,1, false));
    }

    @Test
    public void testUpdateRegisterTypeBadVersion() {
        RegisterType register = mockRegisterType();
        RegisterTypeInfo info = registerTypeInfoFactory.asInfo(register, false, false);
        info.version = BAD_VERSION;
        Response response = target("/registertypes/" + REGISTER_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(register, never()).setObisCode(new ObisCode(1, 2, 3, 4, 5, 1));
    }

    @Test
    public void testDeleteRegisterTypeOkVersion() {
        RegisterType register = mockRegisterType();
        RegisterTypeInfo info = registerTypeInfoFactory.asInfo(register, false, false);
        Response response = target("/registertypes/" + REGISTER_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(register, times(1)).delete();
    }

    @Test
    public void testDeleteRegisterTypeBadVersion() {
        RegisterType register = mockRegisterType();
        RegisterTypeInfo info = registerTypeInfoFactory.asInfo(register, false, false);
        info.version = BAD_VERSION;
        Response response = target("/registertypes/" + REGISTER_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(register, never()).delete();
    }
}
