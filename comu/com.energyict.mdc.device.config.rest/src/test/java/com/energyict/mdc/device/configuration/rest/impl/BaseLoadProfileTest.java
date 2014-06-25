package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.*;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.Answers;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.dlms.cosem.LoadProfile;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@Ignore("basic functionality for load profiles")
public class BaseLoadProfileTest extends JerseyTest {

    protected static MasterDataService masterDataService;
    protected static DeviceConfigurationService deviceConfigurationService;
    protected static NlsService nlsService;
    protected static Thesaurus thesaurus;

    private static ProtocolPluggableService protocolPluggableService;
    private static EngineModelService engineModelService;
    private static ValidationService validationService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        masterDataService = mock(MasterDataService.class);
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);

        protocolPluggableService = mock(ProtocolPluggableService.class);
        engineModelService = mock(EngineModelService.class);
        validationService = mock(ValidationService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(masterDataService, deviceConfigurationService, protocolPluggableService, engineModelService, validationService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                ResourceHelper.class,
                DeviceTypeResource.class,
                DeviceConfigurationResource.class,
                LoadProfileTypeResource.class,
                LoadProfileConfigurationResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(masterDataService).to(MasterDataService.class);
                bind(validationService).to(ValidationService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(nlsService).to(NlsService.class);
                bind(ResourceHelper.class).to(ResourceHelper.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(thesaurus).to(Thesaurus.class);

                bind( protocolPluggableService).to(ProtocolPluggableService.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }


    protected List<LoadProfileType> getLoadProfileTypes(int count) {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), getRandomTimeDuration(),
                    new ObisCode(i, i, i, i, i, i), getRegisterMappings(getRandomInt(4))));
        }
        return loadProfileTypes;
    }

    protected List<LoadProfileSpec> getLoadProfileSpecs(int count) {
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            loadProfileSpecs.add(mockLoadProfileSpec(1000 + i, "Name " + i));
        }
        return loadProfileSpecs;
    }

    protected List<RegisterMapping> getRegisterMappings(int count) {
        List<RegisterMapping> mappings = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            mappings.add(mockRegisterMapping(1000 + i, String.format("Register mapping %04d", i), new ObisCode(i, i, i, i, i, i)));
        }
        return mappings;
    }

    protected String getServerAnswer(Response response) {
        ByteArrayInputStream entity = (ByteArrayInputStream) response.getEntity();
        byte[] bytes = new byte[entity.available()];
        entity.read(bytes,0, entity.available());
        return new String(bytes);
    }

    protected int getRandomInt(int end) {
        return getRandomInt(0, end);
    }

    protected int getRandomInt(int start, int end) {
        int range = end - start;
        return (int) (start + new Random().nextDouble() * range);
    }

    protected TimeDuration getRandomTimeDuration(){
        return LocalizedTimeDuration.intervals.get(getRandomInt(10)).getTimeDuration();
    }


    protected NlsMessageFormat mockNlsMessageFormat() {
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        return nlsMessageFormat;
    }

    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        return deviceType;
    }

    protected DeviceConfiguration mockDeviceConfiguration(String name, long id){
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerSpec.getRegisterMapping()).thenReturn(registerMapping);
        when(registerMapping.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        return deviceConfiguration;
    }

    protected LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<RegisterMapping> mappings) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.getInterval()).thenReturn(interval);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getRegisterMappings()).thenReturn(mappings);
        return loadProfileType;
    }

    protected RegisterMapping mockRegisterMapping(long id, String name, ObisCode obisCode) {
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getId()).thenReturn(id);
        when(registerMapping.getName()).thenReturn(name);
        when(registerMapping.getObisCode()).thenReturn(obisCode);
        when(registerMapping.getTimeOfUse()).thenReturn(0);
        when(registerMapping.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(registerMapping.getReadingType()).thenReturn(readingType);
        return registerMapping;
    }

    protected LoadProfileSpec mockLoadProfileSpec(long id, String name){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        ObisCode obisCode = new ObisCode(0,1,2,3,4,5);
        ObisCode overrulledObisCode = new ObisCode(200,201,202,203,204,205);
        LoadProfileType loadProfileType = mockLoadProfileType(id, name, getRandomTimeDuration(), obisCode, getRegisterMappings(2));
        when(loadProfileSpec.getId()).thenReturn(id);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(loadProfileType);
        when(loadProfileSpec.getObisCode()).thenReturn(obisCode);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(overrulledObisCode);
        when(loadProfileSpec.getInterval()).thenReturn(getRandomTimeDuration());
        return loadProfileSpec;
    }

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
}
