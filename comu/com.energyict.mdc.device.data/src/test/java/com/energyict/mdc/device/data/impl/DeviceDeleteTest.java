package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 01/07/15
 * Time: 10:37
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceDeleteTest {

    private static final long koreId = 1531536L;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private IssueService issueService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private SecurityPropertyService securityPropertyService;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Provider<ComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private MdcReadingTypeUtilService readingTypeUtilService;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private Query<OpenIssue> openIssueQuery;
    @Mock
    private Query<HistoricalIssue> historicalIssueQuery;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private DataMapper<DeviceImpl> dataMapper;
    @Mock
    private OpenIssue openIssue1;
    @Mock
    private OpenIssue openIssue2;
    @Mock
    private HistoricalIssue historicalIssue1;
    @Mock
    private HistoricalIssue historicalIssue2;
    @Mock
    private IssueStatus wontFix;
    @Mock
    private ComTaskExecutionImpl comTaskExecution1;
    @Mock
    private ComTaskExecutionImpl comTaskExecution2;
    @Mock
    private ConnectionTaskImpl connectionTask1;
    @Mock
    private ConnectionTaskImpl connectionTask2;
    @Mock
    private DeviceMessageImpl deviceMessage;
    @Mock
    private EnumeratedEndDeviceGroup endDeviceGroup;
    @Mock
    private EnumeratedGroup.Entry<EndDevice> entry;
    @Mock
    private MeterActivation currentActiveMeterActivation;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserPreferencesService userPreferencesService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceLifeCycle deviceLifeCycle;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private MeterBuilder meterBuilder;
    @Mock
    private LifecycleDates lifecycleDates;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private MultiplierType defaultMultiplierType;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EstimationService estimationService;
    @Mock
    private MeterRole meterRole;
    @Mock
    private LockService lockService;

    @Before
    public void setup() {
        when(dataModel.mapper(DeviceImpl.class)).thenReturn(dataMapper);
        when(this.dataModel.getInstance(DeviceImpl.DeviceEstimationImpl.class)).thenReturn(new DeviceImpl.DeviceEstimationImpl(this.dataModel, this.estimationService));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(deviceService.findDefaultMultiplierType()).thenReturn(defaultMultiplierType);
        when(defaultMultiplierType.getName()).thenReturn(SyncDeviceWithKoreMeter.MULTIPLIER_TYPE);

        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
        when(amrSystem.newMeter(anyString(), anyString())).thenReturn(meterBuilder);

        when(meterBuilder.setAmrId(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setMRID(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setSerialNumber(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setStateMachine(any(FiniteStateMachine.class))).thenReturn(meterBuilder);
        when(meterBuilder.setReceivedDate(any(Instant.class))).thenReturn(meterBuilder);
        when(meterBuilder.create()).thenReturn(meter);

        when(meter.getLifecycleDates()).thenReturn(lifecycleDates);
        when(meter.getConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));

        when(issueService.query(OpenIssue.class)).thenReturn(openIssueQuery);
        when(openIssueQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
        when(issueService.query(HistoricalIssue.class)).thenReturn(historicalIssueQuery);
        when(historicalIssueQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
        when(issueService.findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.empty());
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(deviceLifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.MIN);
        when(deviceLifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.MAX);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.getId()).thenReturn(633L);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
    }

    @Test
    public void deleteDeviceTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        device.delete();

        verify(eventService).postEvent(EventType.DEVICE_BEFORE_DELETE.topic(), device);
        verify(securityPropertyService).deleteSecurityPropertiesFor(device);
        verify(meter).makeObsolete();
        verify(dataMapper).remove(device);
    }

    @Test
    public void deleteWhenThereAreOpenIssuesTest() {
        setupMocksForOpenIssues();
        DeviceImpl device = getNewDeviceWithMockedServices();
        device.delete();

        verify(openIssue1).close(wontFix);
        verify(openIssue2).close(wontFix);

        verify(historicalIssue1).delete();
        verify(historicalIssue2).delete();

        verify(dataMapper).remove(device);
    }

    @Test
    public void deleteWithMessagesTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        setupWithMessages(device);
        DeviceMessage deviceMessage = device.newDeviceMessage(DeviceMessageId.CLOCK_SET_TIME).add();
        device.delete();

        verify(deviceMessage).delete();
    }

    @Test
    public void deleteWhenInStaticGroupTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        setupWithDeviceInStaticGroup();
        device.delete();

        verify(endDeviceGroup).remove(entry);
    }

    @Test
    public void deleteWithActiveMeterActivationTest() {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        setupWithActiveMeterActivation();
        DeviceImpl device = getNewDeviceWithMockedServices();

        device.delete();

        verify(currentActiveMeterActivation).endAt(now.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    public void deleteWithCustomPropertySets() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(this.deviceType.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        DeviceImpl device = getNewDeviceWithMockedServices();
        device.delete();

        verify(customPropertySetService).removeValuesFor(customPropertySet, device);
    }

    private void setupWithActiveMeterActivation() {
        setupMocksForKoreMeter();
        doReturn(Optional.of(currentActiveMeterActivation)).when(meter).getCurrentMeterActivation();
        when(currentActiveMeterActivation.getUsagePoint()).thenReturn(Optional.empty());
    }

    private void setupWithDeviceInStaticGroup() {
        setupMocksForKoreMeter();
        when(meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(meter)).thenReturn(Collections.singletonList(endDeviceGroup));
        when(meter.getId()).thenReturn(koreId);
        doReturn(Collections.singletonList(entry)).when(endDeviceGroup).getEntries();
        when(entry.getMember()).thenReturn(meter);
    }

    private void setupWithMessages(DeviceImpl device) {
        when(dataModel.getInstance(DeviceMessageImpl.class)).thenReturn(deviceMessage);
        when(deviceMessage.initialize(device, DeviceMessageId.CLOCK_SET_TIME)).thenReturn(deviceMessage);
    }

    private void setupMocksForOpenIssues() {
        setupMocksForKoreMeter();
        when(issueService.findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
        when(issueService.query(OpenIssue.class)).thenReturn(openIssueQuery);
        when(openIssueQuery.select(any(Condition.class))).thenReturn(Arrays.asList(openIssue1, openIssue2));
        when(issueService.query(HistoricalIssue.class)).thenReturn(historicalIssueQuery);
        when(historicalIssueQuery.select(any(Condition.class))).thenReturn(Arrays.asList(historicalIssue1, historicalIssue2));
    }

    private void setupMocksForKoreMeter() {
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
        when(meter.getCurrentMeterActivation()).thenReturn(Optional.empty());
    }

    private DeviceImpl getNewDeviceWithMockedServices() {
        DeviceImpl device = new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, validationService, securityPropertyService, scheduledConnectionTaskProvider, inboundConnectionTaskProvider, connectionInitiationProvider, scheduledComTaskExecutionProvider, meteringGroupsService, customPropertySetService, readingTypeUtilService, threadPrincipalService, userPreferencesService, deviceConfigurationService, deviceService, lockService);
        device.initialize(this.deviceConfiguration, "For testing purposes", Instant.now());
        device.save();
        return device;
    }
}
