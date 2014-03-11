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
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class RegisterTypeResourceTest extends JerseyTest {

    private static DeviceConfigurationService deviceConfigurationService;
    private static MeteringService meteringService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        meteringService = mock(MeteringService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(deviceConfigurationService, meteringService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(RegisterTypeResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(meteringService).to(MeteringService.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Test
    public void testGetEmptyRegisterTypeList() throws Exception {
        Finder<RegisterMapping> finder = mockFinder(Collections.<RegisterMapping>emptyList());
        when(deviceConfigurationService.findAllRegisterMappings()).thenReturn(finder);

        Map<String, Object> map = target("/registertypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List)map.get("registerTypes")).isEmpty();
    }
    
    @Test
    public void testRegisterTypeInfoJavaScriptMappings() throws Exception {
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getId()).thenReturn(13L);
        when(registerMapping.getName()).thenReturn("register type");
        when(registerMapping.getObisCode()).thenReturn(new ObisCode(1,2,3,4,5,6));
        when(registerMapping.isLinkedByDeviceType()).thenReturn(true);
        when(registerMapping.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mock(ReadingType.class);
        when(registerMapping.getReadingType()).thenReturn(readingType);
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
        when(deviceConfigurationService.findRegisterMapping(13)).thenReturn(registerMapping);
        when(deviceConfigurationService.findRegisterSpecsByDeviceTypeAndRegisterMapping(any(DeviceType.class), any(RegisterMapping.class))).thenReturn(registerSpecs);

        Map<String, Object> map = target("/registertypes/13").request().get(Map.class);
        assertThat(map).hasSize(8)
        .containsKey("id")
        .containsKey("name")
        .containsKey("obisCode")
        .containsKey("isLinkedByDeviceType")
        .containsKey("isLinkedByRegisterConfig")
        .containsKey("timeOfUse")
        .containsKey("unit")
        .containsKey("readingType");
        assertThat((Map)map.get("readingType")).hasSize(17)
        .containsKey("mrid")
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
