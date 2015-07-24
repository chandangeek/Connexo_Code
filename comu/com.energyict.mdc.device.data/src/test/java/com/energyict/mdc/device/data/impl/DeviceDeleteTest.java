package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    private Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
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
    private EnumeratedEndDeviceGroup.Entry entry;
    @Mock
    private MeterActivation currentActiveMeterActivation;


    @Before
    public void setup() {
        when(dataModel.mapper(DeviceImpl.class)).thenReturn(dataMapper);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.<Meter>empty());
        when(issueService.query(OpenIssue.class)).thenReturn(openIssueQuery);
        when(openIssueQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
        when(issueService.query(HistoricalIssue.class)).thenReturn(historicalIssueQuery);
        when(historicalIssueQuery.select(any(Condition.class))).thenReturn(Collections.emptyList());
        when(issueService.findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.<IssueStatus>empty());
    }

    @Test
    public void deleteDeviceTest() {
        Meter meter = mock(Meter.class);
        when(meter.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(this.amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
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
    public void deleteWithComTaskExecutionsTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        setupWithComTaskExecutions(device);
        device.delete();

        verify(comTaskExecution1).delete();
        verify(comTaskExecution2).delete();
    }

    @Test
    public void deleteWithConnectionTasksTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        setupWithConnectionTasks(device);
        device.delete();

        verify(connectionTask1).delete();
        verify(connectionTask2).delete();
    }

    @Test
    public void deleteWithMessagesTest() {
        DeviceImpl device = getNewDeviceWithMockedServices();
        setupWithMessages(device);
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(DeviceMessageId.CLOCK_SET_TIME).add();
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

        verify(currentActiveMeterActivation).endAt(now);
    }

    private void setupWithActiveMeterActivation() {
        setupMocksForKoreMeter();
        doReturn(Optional.of(currentActiveMeterActivation)).when(meter).getCurrentMeterActivation();
    }

    private void setupWithDeviceInStaticGroup() {
        setupMocksForKoreMeter();
        when(meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(meter)).thenReturn(Collections.singletonList(endDeviceGroup));
        when(meter.getId()).thenReturn(koreId);
        doReturn(Collections.singletonList(entry)).when(endDeviceGroup).getEntries();
        when(entry.getEndDevice()).thenReturn(meter);
    }

    private void setupWithMessages(DeviceImpl device) {
        when(dataModel.getInstance(DeviceMessageImpl.class)).thenReturn(deviceMessage);
        when(deviceMessage.initialize(device, DeviceMessageId.CLOCK_SET_TIME)).thenReturn(deviceMessage);
    }

    private void setupWithConnectionTasks(DeviceImpl device) {
        when(connectionTaskService.findConnectionTasksByDevice(device)).thenReturn(Arrays.asList(connectionTask1, connectionTask2));
    }

    private void setupWithComTaskExecutions(DeviceImpl device) {
        when(communicationTaskService.findAllComTaskExecutionsIncludingObsoleteForDevice(device)).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
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
        return new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, validationService,
                connectionTaskService, communicationTaskService, securityPropertyService, scheduledConnectionTaskProvider, inboundConnectionTaskProvider,
                connectionInitiationProvider, scheduledComTaskExecutionProvider, protocolPluggableService, manuallyScheduledComTaskExecutionProvider, firmwareComTaskExecutionProvider,
                meteringGroupsService);
    }

}
