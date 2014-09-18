package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.google.common.base.Optional;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class DeviceValidationResourceTest extends JerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final long DEVICE_ID = 56854L;
    public static final java.util.Date NOW = Date.from(ZonedDateTime.of(2014, 6, 14, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant());
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
    private EngineModelService engineModelService;
    @Mock
    private MdcPropertyUtils mdcPropertyUtils;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidationService validationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private AmrSystem mdcAmrSystem;
    @Mock
    private Clock clock;
    @Mock
    private ValidationEvaluator evaluator;

    @Mock
    private Device device;
    @Mock
    private Meter meter;
    @Mock
    private Register register1;
    @Mock
    private RegisterSpec registerSpec;
    @Mock
    private RegisterType registerType;
    @Mock
    private ReadingType regReadingType, channelReadingType1, channelReadingType2;
    @Mock
    private MeterActivation meterActivation1, meterActivation2, meterActivation3;
    @Mock
    private Channel channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9;
    @Mock
    private DataValidationStatus validationStatus1, validationStatus2, validationStatus3, validationStatus4, validationStatus5, validationStatus6;
    @Mock
    private ReadingQuality suspect, notSuspect;
    @Mock
    private LoadProfile loadProfile1;
    @Mock
    private com.energyict.mdc.device.data.Channel ch1, ch2;

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        MockitoAnnotations.initMocks(this);

        ResourceConfig resourceConfig = new ResourceConfig(
                DeviceValidationResource.class,
                DeviceResource.class
        );
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(resourceHelper).to(ResourceHelper.class);
                bind(deviceImportService).to(DeviceImportService.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(issueService).to(IssueService.class);
                bind(ConnectionMethodInfoFactory.class).to(ConnectionMethodInfoFactory.class);
                bind(engineModelService).to(EngineModelService.class);
                bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(validationService).to(ValidationService.class);
                bind(meteringService).to(MeteringService.class);
                bind(clock).to(Clock.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class);
        super.configureClient(config);
    }

    @Before
    public void setUp1() {
        when(resourceHelper.findDeviceByMrIdOrThrowException("MRID")).thenReturn(device);
        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(mdcAmrSystem));
        when(device.getId()).thenReturn(DEVICE_ID);
        when(mdcAmrSystem.findMeter("" + DEVICE_ID)).thenReturn(Optional.of(meter));
        when(clock.now()).thenReturn(NOW);
        when(clock.getTimeZone()).thenReturn(TimeZone.getDefault());

        doModelStubbing();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetValidationFeatureStatusCheckRegisterCount() {

        DeviceValidationStatusInfo response = target("devices/MRID/validationrulesets/validationstatus").request().get(DeviceValidationStatusInfo.class);

        assertThat(response.registerSuspectCount).isEqualTo(5);
    }

    @Test
    public void testGetValidationFeatureStatusCheckLoadProfileCount() {

        DeviceValidationStatusInfo response = target("devices/MRID/validationrulesets/validationstatus").request().get(DeviceValidationStatusInfo.class);

        assertThat(response.loadProfileSuspectCount).isEqualTo(4);
    }

    private void doModelStubbing() {
        when(device.getRegisters()).thenReturn(Arrays.asList(register1));
        when(register1.getReadingType()).thenReturn(regReadingType);
        when(regReadingType.getMRID()).thenReturn("REG1");
        when(channelReadingType1.getMRID()).thenReturn("CH1");
        when(channelReadingType2.getMRID()).thenReturn("CH2");

        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1));
        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(ch1, ch2));
        when(ch1.getReadingType()).thenReturn(channelReadingType1);
        when(ch2.getReadingType()).thenReturn(channelReadingType2);

        doReturn(Arrays.asList(meterActivation1, meterActivation2, meterActivation3)).when(meter).getMeterActivations();
        ZonedDateTime fromReg = ZonedDateTime.ofInstant(NOW.toInstant(), ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        ZonedDateTime from = ZonedDateTime.ofInstant(NOW.toInstant(), ZoneId.systemDefault()).minusYears(2);
        ZonedDateTime to = ZonedDateTime.ofInstant(NOW.toInstant(), ZoneId.systemDefault()).minusDays(10);
        when(meterActivation1.getInterval()).thenReturn(Interval.endAt(Date.from(from.toInstant())));
        when(meterActivation2.getInterval()).thenReturn(new Interval(Date.from(from.toInstant()), Date.from(to.toInstant())));
        when(meterActivation3.getInterval()).thenReturn(Interval.startAt(Date.from(to.toInstant())));
        when(meterActivation1.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(channel1.getMeterActivation()).thenReturn(meterActivation1);
        when(channel2.getMeterActivation()).thenReturn(meterActivation1);
        when(channel3.getMeterActivation()).thenReturn(meterActivation1);
        when(meterActivation2.getChannels()).thenReturn(Arrays.asList(channel4, channel5, channel6));
        when(channel4.getMeterActivation()).thenReturn(meterActivation2);
        when(channel5.getMeterActivation()).thenReturn(meterActivation2);
        when(channel6.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation3.getChannels()).thenReturn(Arrays.asList(channel7, channel8, channel9));
        when(channel7.getMeterActivation()).thenReturn(meterActivation3);
        when(channel8.getMeterActivation()).thenReturn(meterActivation3);
        when(channel9.getMeterActivation()).thenReturn(meterActivation3);
        when(validationService.getLastChecked(meterActivation1)).thenReturn(Optional.of(NOW));
        when(validationService.getLastChecked(meterActivation2)).thenReturn(Optional.of(NOW));
        when(validationService.getLastChecked(meterActivation3)).thenReturn(Optional.of(NOW));
        when(channel1.getMainReadingType()).thenReturn(regReadingType);
        when(channel2.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel3.getMainReadingType()).thenReturn(channelReadingType2);
        when(channel4.getMainReadingType()).thenReturn(regReadingType);
        when(channel5.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel6.getMainReadingType()).thenReturn(channelReadingType2);
        when(channel7.getMainReadingType()).thenReturn(regReadingType);
        when(channel8.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel9.getMainReadingType()).thenReturn(channelReadingType2);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(suspect.getTypeCode()).thenReturn("3.0.1");
        when(notSuspect.getTypeCode()).thenReturn("0");

        Interval regInterval1 = new Interval(Date.from(fromReg.toInstant()), Date.from(to.toInstant()));
        when(evaluator.getValidationStatus(channel4, regInterval1)).thenReturn(Arrays.asList(validationStatus1));
        Date toNow = Date.from(ZonedDateTime.ofInstant(NOW.toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        Interval regInterval2 = new Interval(Date.from(to.toInstant()), toNow);
        when(evaluator.getValidationStatus(channel7, regInterval2)).thenReturn(Arrays.asList(validationStatus2, validationStatus3));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus1).getReadingQualities();
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus2).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus3).getReadingQualities();

        ZonedDateTime fromCh = ZonedDateTime.ofInstant(NOW.toInstant(), ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        Interval chInterval1 = new Interval(Date.from(fromCh.toInstant()), Date.from(to.toInstant()));
        when(evaluator.getValidationStatus(channel5, chInterval1)).thenReturn(Arrays.asList(validationStatus4));
        when(evaluator.getValidationStatus(channel6, chInterval1)).thenReturn(Collections.emptyList());
        Interval chInterval2 = new Interval(Date.from(to.toInstant()), toNow);
        when(evaluator.getValidationStatus(channel8, chInterval2)).thenReturn(Collections.emptyList());
        when(evaluator.getValidationStatus(channel9, chInterval2)).thenReturn(Arrays.asList(validationStatus5, validationStatus6));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus4).getReadingQualities();
        doReturn(Arrays.asList(suspect, notSuspect)).when(validationStatus5).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus6).getReadingQualities();

    }

}