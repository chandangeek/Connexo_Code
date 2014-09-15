package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.DataValidationStatusImpl;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonModel;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.ws.rs.core.Application;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileResourceTest extends JerseyTest {

    public static final String BATTERY_LOW = "BATTERY_LOW";
    public static final Date NOW = new Date(1410786205000L);
    public static final long CHANNEL_ID1 = 151521354L;
    public static final long CHANNEL_ID2 = 7487921005L;
    private static Mocks mocks = new Mocks();
    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    @Mock
    private Device device;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileReading loadProfileReading;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private IValidationRule rule1;
    @Mock
    private ReadingQuality quality1;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ValidationEvaluator evaluator;

    public LoadProfileResourceTest() {
    }

    @BeforeClass
    public static void classSetup() {
        MockitoAnnotations.initMocks(mocks);
    }

    @Before
    public void setUpStubs() {
        when(mocks.resourceHelper.findDeviceByMrIdOrThrowException("1")).thenReturn(device);
        when(mocks.resourceHelper.findLoadProfileOrThrowException(device, 1)).thenReturn(loadProfile);
        Interval interval = new Interval(new Date(intervalStart), new Date(intervalEnd));
        when(loadProfile.getChannelData(interval)).thenReturn(asList(loadProfileReading));
        when(loadProfileReading.getInterval()).thenReturn(interval);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.BATTERY_LOW));
        when(mocks.thesaurus.getString(BATTERY_LOW, BATTERY_LOW)).thenReturn(BATTERY_LOW);
        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel1, BigDecimal.valueOf(200, 0), channel2, BigDecimal.valueOf(250, 0)));
        when(mocks.clock.now()).thenReturn(NOW);
        when(channel1.getDevice()).thenReturn(device);
        when(channel1.getId()).thenReturn(CHANNEL_ID1);
        when(channel2.getDevice()).thenReturn(device);
        when(channel2.getId()).thenReturn(CHANNEL_ID2);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);
        DataValidationStatusImpl state1 = new DataValidationStatusImpl(new Date(intervalEnd), true);
        state1.addReadingQuality(quality1, asList(rule1));
        when(rule1.getRuleSet()).thenReturn(ruleSet);
        when(ruleSet.getName()).thenReturn("ruleSetName");
        doReturn(Arrays.asList(rule1)).when(ruleSet).getRules();
        when(rule1.isActive()).thenReturn(true);
        when(loadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel1, state1));
        when(mocks.validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(rule1.getImplementation()).thenReturn("isPrime");
        when(rule1.getDisplayName()).thenReturn("Primes only");
    }

    @After
    public void tearDown() {

    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                DeviceResource.class,
                LoadProfileResource.class);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mocks.resourceHelper).to(ResourceHelper.class);
                bind(mocks.deviceImportService).to(DeviceImportService.class);
                bind(mocks.deviceDataService).to(DeviceDataService.class);
                bind(mocks.deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(mocks.issueService).to(IssueService.class);
                bind(mocks.connectionMethodInfoFactory).to(ConnectionMethodInfoFactory.class);
                bind(mocks.engineModelService).to(EngineModelService.class);
                bind(mocks.mdcPropertyUtils).to(MdcPropertyUtils.class);
                bind(mocks.exceptionFactory).to(ExceptionFactory.class);
                bind(mocks.thesaurus).to(Thesaurus.class);
                bind(mocks.validationService).to(ValidationService.class);
                bind(mocks.clock).to(Clock.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class);
        super.configureClient(config);
    }

    @Test
    public void testLoadProfileData() {
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(1410774630000L);
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(1410828630000L);
        assertThat(jsonModel.<List<?>>get("$.data[0].intervalFlags")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].intervalFlags[0]")).isEqualTo(BATTERY_LOW);
        Map values = jsonModel.<Map>get("$.data[0].channelData");
        assertThat(values).contains(entry(String.valueOf(CHANNEL_ID1), 200));
        assertThat(values).contains(entry(String.valueOf(CHANNEL_ID2), 250));
        Map validations = jsonModel.<Map>get("$.data[0].channelValidationData");
        assertThat(validations).hasSize(1).containsKey(String.valueOf(CHANNEL_ID1));
        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<List<?>>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".validationRules")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".validationRules[0].active")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".validationRules[0].implementation")).isEqualTo("isPrime");
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".validationRules[0].displayName")).isEqualTo("Primes only");
    }

    @Test
    public void testLoadProfileDataFiltered() {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testLoadProfileDataFilteredMatches() {
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
    }

    private static class Mocks {
        @Mock
        private ResourceHelper resourceHelper;
        @Mock
        private DeviceImportService deviceImportService;
        @Mock
        private DeviceDataService deviceDataService;
        @Mock
        private DeviceConfigurationService deviceConfigurationService;
        @Mock
        private IssueService issueService;
        @Mock
        private ConnectionMethodInfoFactory connectionMethodInfoFactory;
        @Mock
        private EngineModelService engineModelService;
        @Mock
        private MdcPropertyUtils mdcPropertyUtils;
        @Mock
        private Provider<ProtocolDialectResource> protocolDialectResourceProvider;
        @Mock
        private Provider<LoadProfileResource> loadProfileResourceProvider;
        @Mock
        private Provider<LogBookResource> logBookResourceProvider;
        @Mock
        private Provider<RegisterResource> registerResourceProvider;
        @Mock
        private ExceptionFactory exceptionFactory;
        @Mock
        private Provider<DeviceValidationResource> deviceValidationResourceProvider;
        @Mock
        private Provider<BulkScheduleResource> bulkScheduleResourceProvider;
        @Mock
        private Provider<DeviceScheduleResource> deviceScheduleResourceProvider;
        @Mock
        private Provider<DeviceComTaskResource> deviceComTaskResourceProvider;
        @Mock
        private Thesaurus thesaurus;
        @Mock
        private ValidationService validationService;
        @Mock
        private Clock clock;
    }


}