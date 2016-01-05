package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.impl.ChannelSpecImpl;
import com.energyict.mdc.device.config.impl.LoadProfileSpecImpl;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.issues.impl.IssueCollectorDefaultImplementation;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MasterDataService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceImplTest {

    private static final ZonedDateTime IRREGULAR_BASE = ZonedDateTime.of(2002, 10, 12, 22, 0, 31, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime REGULAR_BASE = ZonedDateTime.of(2002, 10, 12, 22, 30, 0, 0, TimeZoneNeutral.getMcMurdo());
    public static final String BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String REGISTER = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private IssueService issueService;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Clock clock;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidationService validationService;
    @Mock
    private SecurityPropertyService securityPropertyService;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    @Mock
    private Provider<ScheduledComTaskExecutionImpl> scheduledComTaskProvider;
    @Mock
    private Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionTaskProvider;
    @Mock
    private Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private ChannelType channelTypeBulk, channelTypeRegister;
    @Mock
    private ReadingType readingTypeBulk, readingTypeRegister;
    @Mock
    private com.energyict.mdc.issues.IssueService mdcIssueService;
    @Mock
    private CustomPropertySetService customPropertySetService;

    @Before
    public void setUp() {
        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
        when(channelTypeBulk.getReadingType()).thenReturn(readingTypeBulk);
        when(channelTypeRegister.getReadingType()).thenReturn(readingTypeRegister);
        when(readingTypeBulk.getMRID()).thenReturn(BULK);
        when(readingTypeRegister.getMRID()).thenReturn(REGISTER);

        when(dataModel.getInstance(LoadProfileImpl.class)).thenAnswer(invocation -> new LoadProfileImpl(dataModel));
        when(dataModel.getInstance(OverflowCheck.class)).thenAnswer(invocation -> new OverflowCheck(mdcIssueService));
        when(mdcIssueService.newIssueCollector()).thenAnswer(invocation -> new IssueCollectorDefaultImplementation(clock, thesaurus));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testStoreWithOverflowCheckNoOverflowsApply() {

        BigDecimal registerOverflow = BigDecimal.valueOf(10_000);
        BigDecimal bulkOverflow = BigDecimal.valueOf(10_000);
        DeviceImpl device = createDevice(registerOverflow, bulkOverflow);
        MeterReadingImpl meterReading = createMeterReading();

        List<Warning> warnings = device.store(meterReading);

        ArgumentCaptor<MeterReading> meterReadingCaptor = ArgumentCaptor.forClass(MeterReading.class);
        verify(meter).store(meterReadingCaptor.capture());
        MeterReading captured = meterReadingCaptor.getValue();
        assertThat(captured).isSameAs(meterReading);

        assertThat(warnings.isEmpty());
    }

    @Test
    public void testStoreWithOverflowCheckOverflowsApplyToSomeIntervalReadings() {

        BigDecimal registerOverflow = BigDecimal.valueOf(1_000);
        BigDecimal bulkOverflow = BigDecimal.valueOf(1_000);
        DeviceImpl device = createDevice(registerOverflow, bulkOverflow);
        MeterReadingImpl meterReading = createMeterReading();

        List<Warning> warnings = device.store(meterReading);

        ArgumentCaptor<MeterReading> meterReadingCaptor = ArgumentCaptor.forClass(MeterReading.class);
        verify(meter).store(meterReadingCaptor.capture());
        MeterReading captured = meterReadingCaptor.getValue();

        assertThat(captured.getReadings()).isEqualTo(meterReading.getReadings());
        assertThat(captured.getIntervalBlocks().get(0)).isSameAs(meterReading.getIntervalBlocks().get(0));
        IntervalBlock intervalBlock = captured.getIntervalBlocks().get(1);
        assertThat(intervalBlock.getReadingTypeCode()).isEqualTo(BULK);
        assertThat(intervalBlock.getIntervals()).hasSize(2);
        assertThat(intervalBlock.getIntervals().get(0).getValue()).isEqualTo(BigDecimal.valueOf(410));
        assertThat(intervalBlock.getIntervals().get(1).getValue()).isEqualTo(BigDecimal.valueOf(413));

        assertThat(warnings).hasSize(2);
    }

    @Test
    public void testStoreWithOverflowCheckOverflowsApplyToRegisterReadings() {

        BigDecimal registerOverflow = BigDecimal.valueOf(400);
        BigDecimal bulkOverflow = BigDecimal.valueOf(10_000);
        DeviceImpl device = createDevice(registerOverflow, bulkOverflow);
        MeterReadingImpl meterReading = createMeterReading();

        List<Warning> warnings = device.store(meterReading);

        ArgumentCaptor<MeterReading> meterReadingCaptor = ArgumentCaptor.forClass(MeterReading.class);
        verify(meter).store(meterReadingCaptor.capture());
        MeterReading captured = meterReadingCaptor.getValue();

        assertThat(captured.getReadings().get(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(captured.getReadings().get(1).getValue()).isEqualTo(BigDecimal.valueOf(131));

        assertThat(captured.getIntervalBlocks().get(0)).isSameAs(meterReading.getIntervalBlocks().get(0));
        assertThat(captured.getIntervalBlocks().get(1)).isSameAs(meterReading.getIntervalBlocks().get(1));

        assertThat(warnings).hasSize(2);
    }

    private DeviceImpl createDevice(BigDecimal registerOverflow, BigDecimal bulkOverflow) {
        DeviceImpl device = new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, validationService, securityPropertyService,
                scheduledConnectionTaskProvider, inboundConnectionTaskProvider, connectionInitiationTaskProvider, scheduledComTaskProvider,
                manuallyScheduledComTaskExecutionTaskProvider, firmwareComTaskExecutionProvider, meteringGroupsService, customPropertySetService);
        LoadProfileSpecImpl loadProfileSpec1 = new LoadProfileSpecImpl(dataModel, eventService, thesaurus);
        ChannelSpecImpl channelSpec1 = new ChannelSpecImpl(dataModel, eventService, thesaurus);
        channelSpec1.setOverflow(registerOverflow);
        channelSpec1.setChannelType(channelTypeRegister);
        loadProfileSpec1.addChannelSpec(channelSpec1);
        LoadProfileSpecImpl loadProfileSpec2 = new LoadProfileSpecImpl(dataModel, eventService, thesaurus);
        ChannelSpecImpl channelSpec2 = new ChannelSpecImpl(dataModel, eventService, thesaurus);
        channelSpec2.setOverflow(bulkOverflow);
        channelSpec2.setChannelType(channelTypeBulk);
        loadProfileSpec2.addChannelSpec(channelSpec2);
        device.addLoadProfiles(Arrays.asList(loadProfileSpec1, loadProfileSpec2));
        return device;
    }

    private MeterReadingImpl createMeterReading() {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        ReadingImpl reading1 = ReadingImpl.of(REGISTER, BigDecimal.valueOf(500), IRREGULAR_BASE.toInstant());
        ReadingImpl reading2 = ReadingImpl.of(REGISTER, BigDecimal.valueOf(531), IRREGULAR_BASE.plusSeconds(156165).toInstant());
        meterReading.addReading(reading1);
        meterReading.addReading(reading2);

        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of(BULK);

        IntervalReadingImpl intervalReading1 = IntervalReadingImpl.of(REGULAR_BASE.toInstant(), BigDecimal.valueOf(210));
        IntervalReadingImpl intervalReading2 = IntervalReadingImpl.of(REGULAR_BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(213));

        intervalBlock1.addIntervalReading(intervalReading1);
        intervalBlock1.addIntervalReading(intervalReading2);

        meterReading.addIntervalBlock(intervalBlock1);

        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of(BULK);

        IntervalReadingImpl intervalReading3 = IntervalReadingImpl.of(REGULAR_BASE.toInstant(), BigDecimal.valueOf(1410));
        IntervalReadingImpl intervalReading4 = IntervalReadingImpl.of(REGULAR_BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(1413));

        intervalBlock2.addIntervalReading(intervalReading3);
        intervalBlock2.addIntervalReading(intervalReading4);

        meterReading.addIntervalBlock(intervalBlock2);
        return meterReading;
    }


}