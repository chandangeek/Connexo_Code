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
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;

@Ignore("basic functionality for load profiles")
public class BaseLoadProfileTest extends JerseyTest {

    @Mock
    protected MasterDataService masterDataService;
    @Mock
    protected DeviceConfigurationService deviceConfigurationService;
    @Mock
    protected NlsService nlsService;
    @Mock
    protected Thesaurus thesaurus;

    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private EngineModelService engineModelService;
    @Mock
    private ValidationService validationService;
    @Mock
    private PropertyUtils propertyUtils;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(masterDataService, deviceConfigurationService, protocolPluggableService, engineModelService, validationService);
    }

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);
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
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
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
            TimeDuration randomTimeDuration = getRandomTimeDuration();
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), randomTimeDuration,
                    new ObisCode(i, i, i, i, i, i), getChannelTypes(getRandomInt(4), randomTimeDuration)));
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

    protected List<RegisterType> getRegisterTypes(int count) {
        List<RegisterType> registerTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            registerTypes.add(mockRegisterType(1000 + i, String.format("Register type %04d", i), new ObisCode(i, i, i, i, i, i)));
        }
        return registerTypes;
    }


    protected List<ChannelType> getChannelTypes(int count, TimeDuration interval) {
        List<ChannelType> channelTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            channelTypes.add(mockChannelType(1000 + i, String.format("Channel type %04d", i), new ObisCode(i, i, i, i, i, i), interval));
        }
        return channelTypes;
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
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));
        return deviceConfiguration;
    }

    protected LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<ChannelType> channelTypes) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.getInterval()).thenReturn(interval);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getChannelTypes()).thenReturn(channelTypes);
        return loadProfileType;
    }

    protected RegisterType mockRegisterType(long id, String name, ObisCode obisCode) {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(id);
        when(registerType.getName()).thenReturn(name);
        when(registerType.getObisCode()).thenReturn(obisCode);
        when(registerType.getTimeOfUse()).thenReturn(0);
        when(registerType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(registerType.getReadingType()).thenReturn(readingType);
        return registerType;
    }

    protected ChannelType mockChannelType(long id, String name, ObisCode obisCode, TimeDuration interval) {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(id);
        when(channelType.getName()).thenReturn(name);
        when(channelType.getObisCode()).thenReturn(obisCode);
        when(channelType.getTimeOfUse()).thenReturn(0);
        when(channelType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72." + id);
        when(channelType.getReadingType()).thenReturn(readingType);
        when(channelType.getInterval()).thenReturn(interval);
        RegisterType templateRegister = mockRegisterType(id, name, obisCode);
        when(channelType.getTemplateRegister()).thenReturn(templateRegister);
        return channelType;
    }

    protected LoadProfileSpec mockLoadProfileSpec(long id, String name){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        ObisCode obisCode = new ObisCode(0,1,2,3,4,5);
        ObisCode overrulledObisCode = new ObisCode(200,201,202,203,204,205);
        TimeDuration randomTimeDuration = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(id, name, randomTimeDuration, obisCode, getChannelTypes(2, randomTimeDuration));
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
