/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareCampaignHandlerTest {

    @Mock
    private FirmwareServiceImpl firmwareService;
    @Mock
    private Clock clock;
    @Mock
    private FirmwareVersion firmwareVersion;
    @Mock
    private ScheduledConnectionTask connectionTask;

    private final FirmwareCampaignServiceImpl firmwareCampaignService = mock(FirmwareCampaignServiceImpl.class);
    private final ServiceCallService serviceCallService = mock(ServiceCallService.class);
    private final Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private FirmwareCampaignHandler firmwareCampaignHandler;
    private final ComTaskExecution firmwareComTaskExecution = createFirmwareTaskMock();
    private final ComTaskExecution verificationComTaskExecution = createVerificationTaskMock();
    private final LocalEvent event = mock(LocalEvent.class);
    private final com.elster.jupiter.events.EventType eventType = mock(EventType.class);
    private final ServiceCall serviceCall = mock(ServiceCall.class);
    private final ThreadPrincipalService threadPrincipalService = mock(ThreadPrincipalService.class);
    private final TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private final DeviceMessageService deviceMessageService = mock(DeviceMessageService.class);
    private final DeviceMessageSpecificationService deviceMessageSpecificationService = mock(DeviceMessageSpecificationService.class);
    private final FirmwareCampaignItemDomainExtension firmwareItem = mock(FirmwareCampaignItemDomainExtension.class);
    private final FirmwareCampaignDomainExtension firmwareCampaign = createMockCampaign();
    private final ServiceCall parentSc = mock(ServiceCall.class);

    private static final String MANUAL_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/manualcomtaskexecution/COMPLETED";
    private static final String MANUAL_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/manualcomtaskexecution/FAILED";
    private static final String FIRMWARE_COMTASKEXECUTION_STARTED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/STARTED";
    private static final String FIRMWARE_COMTASKEXECUTION_COMPLETED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/COMPLETED";
    private static final String FIRMWARE_COMTASKEXECUTION_FAILED = "com/energyict/mdc/device/data/firmwarecomtaskexecution/FAILED";
    private static final String FIRMWARE_CAMPAIGN_EDITED = "com/energyict/mdc/firmware/firmwarecampaign/EDITED";
    private static final Instant MESSAGE_RELEASE_DATE = Instant.ofEpochSecond(3600);
    private static final Instant MESSAGE_SENT_DATE = Instant.ofEpochSecond(3700);
    private static final Instant MESSAGE_CONFIRMED_DATE = Instant.ofEpochSecond(3800);
    private static final Instant ACTIVATION_DATE = Instant.ofEpochSecond(5000);
    private static final TimeDuration VALIDATION_TIMEOUT = new TimeDuration(1, TimeDuration.TimeUnit.MINUTES);

    @Before
    public void setUp() {
        when(firmwareVersion.getId()).thenReturn(2L);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareCampaign.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(firmwareService.getFirmwareCampaignService()).thenReturn(firmwareCampaignService);
        when(firmwareComTaskExecution.isFirmware()).thenReturn(true);
        when(firmwareCampaignService.getCampaignOn(firmwareComTaskExecution)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaignService.getCampaignOn(verificationComTaskExecution)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaignService.getFirmwareService()).thenReturn(firmwareService);
        when(firmwareCampaignService.findActiveFirmwareItemByDevice(any())).thenReturn(Optional.of(firmwareItem));
        when(serviceCallService.lockServiceCall(anyLong())).thenReturn(Optional.of(serviceCall));
        when(event.getType()).thenReturn(eventType);
        when(firmwareItem.getServiceCall()).thenReturn(serviceCall);
        when(firmwareItem.getFirmwareCampaign()).thenReturn(firmwareCampaign);
        when(firmwareItem.findOrCreateFirmwareComTaskExecution()).thenReturn(Optional.of(firmwareComTaskExecution));
        when(firmwareItem.findOrCreateVerificationComTaskExecution()).thenReturn(Optional.of(verificationComTaskExecution));
        QueryStream<FirmwareCampaignItemDomainExtension> queryStream = FakeBuilder.initBuilderStub(Optional.of(firmwareItem), QueryStream.class);
        when(firmwareCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        when(parentSc.getExtension(any())).thenReturn(Optional.of(firmwareCampaign));
        firmwareCampaignHandler = new FirmwareCampaignHandler(firmwareService, clock, serviceCallService, thesaurus, threadPrincipalService, transactionService, deviceMessageService, deviceMessageSpecificationService);
    }

    @Test
    public void testFirmwareTaskStarted() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.PENDING);
        when(serviceCall.canTransitionTo(DefaultState.ONGOING)).thenReturn(true);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_STARTED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.ONGOING);
    }

    @Test
    public void testFirmwareTaskCompletedWithoutVerification() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testFirmwareTaskCompletedWithVerification() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        when(firmwareCampaign.isWithVerification()).thenReturn(true);
        when(verificationComTaskExecution.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(connectionTask.isActive()).thenReturn(true);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall, never()).transitionWithLockIfPossible(any(DefaultState.class));
        verify(verificationComTaskExecution).schedule(MESSAGE_CONFIRMED_DATE.plus(VALIDATION_TIMEOUT.asTemporalAmount()));
    }

    @Test
    public void testFirmwareTaskCompletedWithVerificationAfterActivation() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        when(firmwareCampaign.isWithVerification()).thenReturn(true);
        when(verificationComTaskExecution.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(firmwareCampaign.getValidationConnectionStrategy()).thenReturn(Optional.of(ConnectionStrategy.AS_SOON_AS_POSSIBLE));
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(connectionTask.isActive()).thenReturn(true);
        when(firmwareItem.getDeviceMessage().get().getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE));

        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall, never()).transitionWithLockIfPossible(any(DefaultState.class));
        verify(verificationComTaskExecution).schedule(ACTIVATION_DATE.plus(VALIDATION_TIMEOUT.asTemporalAmount()));
    }

    @Test
    public void testFirmwareTaskCompletedButMessageFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(firmwareItem.getDeviceMessage().get().getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testFirmwareTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.FAILED);
        when(firmwareComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(FIRMWARE_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(firmwareComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testVerificationTaskCompletedSuccessful() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareCampaign.isWithVerification()).thenReturn(true);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(firmwareItem.doesDeviceAlreadyHaveTheSameVersion()).thenReturn(true);
        when(firmwareItem.getDevice()).thenReturn(device);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testVerificationTaskCompletedFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(firmwareCampaign.isWithVerification()).thenReturn(true);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_COMPLETED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(firmwareItem.doesDeviceAlreadyHaveTheSameVersion()).thenReturn(false);
        when(firmwareService.getActiveFirmwareVersion(device, firmwareCampaign.getFirmwareType())).thenReturn(Optional.empty());
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testVerificationTaskFailed() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(6000));
        when(firmwareCampaign.isWithVerification()).thenReturn(true);
        Device device = createMockDevice(DeviceMessageStatus.CONFIRMED);
        when(verificationComTaskExecution.getDevice()).thenReturn(device);
        when(eventType.getTopic()).thenReturn(MANUAL_COMTASKEXECUTION_FAILED);
        when(event.getSource()).thenReturn(verificationComTaskExecution);
        when(serviceCall.getParent()).thenReturn(Optional.ofNullable(parentSc));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        firmwareCampaignHandler.onEvent(event);
        verify(serviceCall).transitionWithLockIfPossible(DefaultState.FAILED);
    }

    @Test
    public void testCampaignEdited() {
        when(eventType.getTopic()).thenReturn(FIRMWARE_CAMPAIGN_EDITED);
        when(event.getSource()).thenReturn(firmwareCampaign);
        firmwareCampaignHandler.onEvent(event);
        verify(firmwareCampaignService, timeout(500)).handleCampaignUpdate(firmwareCampaign);
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

    private FirmwareCampaignDomainExtension createMockCampaign() {
        FirmwareCampaignDomainExtension firmwareCampaign = mock(FirmwareCampaignDomainExtension.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(firmwareCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        ProtocolSupportedFirmwareOptions protocolSupportedFirmwareOptions = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE;
        when(firmwareCampaign.getName()).thenReturn("TestCampaign");
        when(firmwareCampaign.getDeviceType()).thenReturn(deviceType);
        when(firmwareCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(firmwareCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(firmwareCampaign.getFirmwareManagementOption()).thenReturn(protocolSupportedFirmwareOptions);
        when(firmwareCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(firmwareCampaign.getValidationTimeout()).thenReturn(VALIDATION_TIMEOUT);
        when(firmwareCampaign.getId()).thenReturn(3L);
        when(firmwareCampaign.getVersion()).thenReturn(4L);
        when(firmwareCampaign.getFirmwareUploadConnectionStrategy()).thenReturn(Optional.empty());
        when(firmwareCampaign.getValidationConnectionStrategy()).thenReturn(Optional.empty());
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
        when(deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
        when(deviceMessage.getStatus()).thenReturn(deviceMessageStatus);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getReleaseDate()).thenReturn(MESSAGE_RELEASE_DATE);
        when(deviceMessage.getSentDate()).thenReturn(Optional.of(MESSAGE_SENT_DATE));
        when(deviceMessage.getModTime()).thenReturn(MESSAGE_CONFIRMED_DATE);
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE));
        when(firmware.getId()).thenReturn(2L);
        when(device.getMessages()).thenReturn(Collections.singletonList(deviceMessage));
        when(firmwareItem.getDeviceMessage()).thenReturn(Optional.of(deviceMessage));
        PropertySpec firmwareSpec = mock(PropertySpec.class);
        DeviceMessageAttribute firmwareAttribute = mock(DeviceMessageAttribute.class);
        when(firmwareSpec.getValueFactory()).thenReturn(mock(ValueFactory.class));
        when(firmwareSpec.getName()).thenReturn("firmware");
        when(firmwareAttribute.getName()).thenReturn("firmware");
        when(firmwareAttribute.getValue()).thenReturn(firmwareVersion);
        PropertySpec dateSpec = mock(PropertySpec.class);
        DeviceMessageAttribute dateAttribute = mock(DeviceMessageAttribute.class);
        when(dateSpec.getValueFactory()).thenReturn(new DateFactory());
        when(dateSpec.getName()).thenReturn("activation");
        when(dateAttribute.getName()).thenReturn("activation");
        when(dateAttribute.getValue()).thenReturn(Date.from(ACTIVATION_DATE));
        doReturn(Arrays.asList(firmwareSpec, dateSpec)).when(deviceMessageSpec).getPropertySpecs();
        doReturn(Arrays.asList(firmwareAttribute, dateAttribute)).when(deviceMessage).getAttributes();
        return device;
    }
}
