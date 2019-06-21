/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareCampaignHandlerTest {

    @Mock
    private FirmwareServiceImpl firmwareService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Clock clock;
    @Mock
    private EventService eventService;
    @Mock
    private TaskService taskService;
    @Mock
    private JsonService jsonService;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DataModel dataModel;

    private final long deviceTypeId = 485L;

    private FirmwareCampaignServiceImpl firmwareCampaignService = mock(FirmwareCampaignServiceImpl.class);
    private ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private FirmwareCampaignHandler firmwareCampaignHandler;
    private ComTaskExecution firmwareComTaskExecution = createFirmwareTaskMock();
    private ComTaskExecution verificationComTaskExecution = createVerificationTaskMock();
    private LocalEvent event = mock(LocalEvent.class);
    private com.elster.jupiter.events.EventType eventType = mock(EventType.class);
    private ServiceCall serviceCall = mock(ServiceCall.class);
    private ThreadPrincipalService threadPrincipalService = mock(ThreadPrincipalService.class);
    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private DeviceInFirmwareCampaign firmwareItem = mock(DeviceInFirmwareCampaign.class);
    private FirmwareCampaign firmwareCampaign = createMockCampaign();
    private FirmwareCampaign firmwareCampaign2 = createMockCampaign();
    private FirmwareCampaignItemDomainExtension firmwareItemDomainExtension = mock(FirmwareCampaignItemDomainExtension.class);

    private final static String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private final static String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private final static String SCHEDULED_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/COMPLETED";
    private final static String SCHEDULED_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/scheduledcomtaskexecution/FAILED";
    private final static String FIRMWARE_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/STARTED";
    private final static String FIRMWARE_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/COMPLETED";
    private final static String FIRMWARE_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/FAILED";
    private final static String FIRMWARE_CAMPAIGN_EDITED = "com/energyict/mdc/firmware/firmwarecampaign/EDITED";
    
    @Before
    public void setUp() {
        when(firmwareService.getFirmwareCampaignServiceImpl()).thenReturn(firmwareCampaignService);
        when(firmwareComTaskExecution.isFirmware()).thenReturn(true);
        when(firmwareCampaignService.getCampaignOn(firmwareComTaskExecution)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaignService.getCampaignOn(verificationComTaskExecution)).thenReturn(Optional.of(firmwareCampaign2));
        when(serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class)).thenReturn(Optional.of(firmwareItemDomainExtension));
        when(firmwareCampaignService.findActiveFirmwareItemByDevice(any())).thenReturn(Optional.of(firmwareItem));
        when(serviceCallService.lockServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(event.getType()).thenReturn(eventType);
        when(firmwareItem.cancel()).thenReturn(serviceCall);
        when(firmwareItem.getServiceCall()).thenReturn(serviceCall);
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(firmwareItem), QueryStream.class);
        when(firmwareCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        firmwareCampaignHandler = new FirmwareCampaignHandler(firmwareService, clock, serviceCallService, thesaurus, threadPrincipalService, transactionService);
    }


    @Test
    public void testFirmwareTaskStarted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.PENDING);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_STARTED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.ONGOING);
    }

    @Test
    public void testFirmwareTaskCompleted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testFirmwareTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.FAILED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testVerificationTaskCompletedSuccessful() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareCampaignService.isWithVerification(firmwareCampaign2)).thenReturn(true);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(firmwareItemDomainExtension.deviceAlreadyHasTheSameVersion()).thenReturn(true);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testVerificationTaskCompletedFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareCampaignService.isWithVerification(firmwareCampaign2)).thenReturn(true);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(firmwareItemDomainExtension.deviceAlreadyHasTheSameVersion()).thenReturn(false);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testVerificationTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        when(firmwareCampaignService.isWithVerification(firmwareCampaign2)).thenReturn(true);
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testCampaignEdited() {
        when(eventType.getTopic()).thenReturn(FIRMWARE_CAMPAIGN_EDITED);
        when(event.getSource()).thenReturn(firmwareCampaign);
        firmwareCampaignHandler.onEvent(event);
        verify(firmwareCampaignService, timeout(500)).editCampaignItems(firmwareCampaign);
    }

    private static ComTaskExecution createFirmwareTaskMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        MessagesTask messagesTask = mock(MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(messagesTask));
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));
        when(deviceMessageCategory.getId()).thenReturn(0);
        return comTaskExecution;
    }

    private static ComTaskExecution createVerificationTaskMock() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mock(ComTask.class);
        StatusInformationTask statusInformationTask = mock(StatusInformationTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusInformationTask));
        when(deviceMessageCategory.getId()).thenReturn(0);
        return comTaskExecution;
    }

    private static FirmwareCampaign createMockCampaign() {
        FirmwareCampaign firmwareCampaign = mock(FirmwareCampaign.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(firmwareCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE;
        when(firmwareVersion.getId()).thenReturn(2L);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareCampaign.getName()).thenReturn("TestCampaign");
        when(firmwareCampaign.getDeviceType()).thenReturn(deviceType);
        when(firmwareCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(firmwareCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(firmwareCampaign.getFirmwareManagementOption()).thenReturn(protocolSupportedFirmwareOptions);
        when(firmwareCampaign.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(firmwareCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getValidationTimeout()).thenReturn(new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        when(firmwareCampaign.getId()).thenReturn(3L);
        when(firmwareCampaign.getVersion()).thenReturn(4L);
        return firmwareCampaign;
    }

    private Device createMockDevice(DeviceMessageStatus deviceMessageStatus) {
        Device device = mock(Device.class);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        FirmwareVersion firmware = mock(FirmwareVersion.class);
        when(deviceMessageCategory.getId()).thenReturn(9);
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessage.getStatus()).thenReturn(deviceMessageStatus);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getReleaseDate()).thenReturn(Instant.ofEpochSecond(3600));
        when(firmware.getId()).thenReturn(2L);
        when(device.getMessages()).thenReturn(Collections.singletonList(deviceMessage));
        when(firmwareItemDomainExtension.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        when(firmwareItem.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        return device;
    }
}