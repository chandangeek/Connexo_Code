package com.energyict.mdc.device.configuration.rest.impl;

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
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;
import com.google.common.base.Optional;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {

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
        when(registerType.getName()).thenReturn("register type");
        when(registerType.getObisCode()).thenReturn(new ObisCode(1, 2, 3, 4, 5, 6));
        when(deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType)).thenReturn(true);
        when(registerType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mock(ReadingType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
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

        List<RegisterSpec> registerSpecs = mock(List.class);
        when(registerSpecs.size()).thenReturn(1);
        when(masterDataService.findRegisterType(13)).thenReturn(Optional.of(registerType));
        when(deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(any(DeviceType.class), any(RegisterType.class))).thenReturn(registerSpecs);

        Map<String, Object> map = target("/registertypes/13").request().get(Map.class);
        assertThat(map).hasSize(9)
        .containsKey("id")
        .containsKey("name")
        .containsKey("obisCode")
        .containsKey("isLinkedByDeviceType")
        .containsKey("isLinkedByActiveRegisterConfig")
        .containsKey("isLinkedByInactiveRegisterConfig")
        .containsKey("timeOfUse")
        .containsKey("unitOfMeasure")
        .containsKey("readingType");
        assertThat((Map)map.get("readingType")).hasSize(18)
        .containsKey("mrid")
        .containsKey("description")
        .containsKey("timePeriodOfInterest")
        .containsKey("dataQualifier")
        .containsKey("timeAttributeEnumerations")
        .containsKey("accumulationBehaviour")
        .containsKey("directionOfFlow")
        .containsKey("commodity")
        .containsKey("measurementKind")
        .containsKey("interharmonics")
        .containsKey("argumentReference")
        .containsKey("timeOfUse")
        .containsKey("criticalPeakPeriod")
        .containsKey("consumptionTier")
        .containsKey("phase")
        .containsKey("powerOfTenMultiplier")
        .containsKey("unitOfMeasure")
        .containsKey("currency");

    }


    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);
        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

}
