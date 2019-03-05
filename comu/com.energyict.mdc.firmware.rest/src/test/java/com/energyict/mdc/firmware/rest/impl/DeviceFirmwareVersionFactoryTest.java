/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareManagementDeviceUtilsImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceFirmwareVersionFactoryTest extends BaseFirmwareTest {

    public static final Instant TIME = Instant.ofEpochMilli(1420608345885L);
    public static final Instant NOW = Instant.now();

    @Mock
    private Device device;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTaskExecution firmwareExecution;
    @Mock
    private StatusInformationTask statusCheckTask;
    @Mock
    private ComTask firmwareCheckComTask;
    @Mock
    private ComTaskExecution firmwareCheckExecution;
    @Mock
    private ActivatedFirmwareVersion activatedCommunicationVersion;
    @Mock
    private FirmwareVersion communicationVersion;
    @Mock
    private DeviceMessageCategory firmwareCategory;
    @Mock
    private DeviceMessageService deviceMessageService;

    private List<DeviceMessage> messages;


    @Before
    public void setupEnvironment() {
        messages = new ArrayList<>();

        ComTaskExecutionSession session = mock(ComTaskExecutionSession.class);
        when(session.getId()).thenReturn(102L);
        when(firmwareCheckComTask.getId()).thenReturn(201L);
        when(firmwareCheckComTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusCheckTask));
        when(firmwareCheckExecution.getComTask()).thenReturn(firmwareCheckComTask);
        when(firmwareCheckExecution.executesComTask(firmwareCheckComTask)).thenReturn(true);
        when(firmwareCheckExecution.getNextExecutionTimestamp()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(firmwareCheckExecution.getLastSession()).thenReturn(Optional.of(session));
        when(firmwareCheckExecution.isConfiguredToReadStatusInformation()).thenReturn(true);
        ComTask firmwareComTask = mock(ComTask.class);
        when(firmwareComTask.getId()).thenReturn(101L);
        when(taskService.findFirmwareComTask()).thenReturn(Optional.of(firmwareComTask));
        when(firmwareExecution.getComTask()).thenReturn(firmwareComTask);
        when(firmwareExecution.executesComTask(firmwareComTask)).thenReturn(true);
        when(firmwareExecution.getLastSession()).thenReturn(Optional.of(session));
        when(firmwareExecution.getNextExecutionTimestamp()).thenReturn(TIME);
        when(deviceService.findDeviceByName("upgrade")).thenReturn(Optional.of(device));
        ComTaskEnablement firmwareCheckEnablement = mock(ComTaskEnablement.class);
        when(firmwareCheckEnablement.getComTask()).thenReturn(firmwareCheckComTask);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(firmwareCheckEnablement));
        when(device.getId()).thenReturn(1L);
        when(device.getmRID()).thenReturn("upgrade");
        when(device.getMessages()).thenReturn(messages);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(firmwareExecution, firmwareCheckExecution));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(communicationVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(communicationVersion.getFirmwareVersion()).thenReturn("COM-001-ACT");
        when(communicationVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(communicationVersion.getImageIdentifier()).thenReturn("10.4.0");
        when(activatedCommunicationVersion.getDevice()).thenReturn(device);
        when(activatedCommunicationVersion.getFirmwareVersion()).thenReturn(communicationVersion);
        when(activatedCommunicationVersion.getLastChecked()).thenReturn(TIME);
        when(firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.<ActivatedFirmwareVersion>empty());
        when(firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.of(activatedCommunicationVersion));
        when(firmwareCategory.getId()).thenReturn(1);
        when(deviceMessageSpecificationService.getFirmwareCategory()).thenReturn(firmwareCategory);
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE));
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE));
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER));
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE))
                .thenReturn(Optional.empty());

        DeviceMessage custom = mock(DeviceMessage.class);
        DeviceMessageCategory messageCategory = mock(DeviceMessageCategory.class);
        when(messageCategory.getId()).thenReturn(2);
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getCategory()).thenReturn(messageCategory);
        when(custom.getSpecification()).thenReturn(messageSpec);
        when(custom.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        messages.add(custom);
        DeviceMessage firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CANCELED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        messages.add(firmwareMessage);

        when(firmwareService.getFirmwareManagementDeviceUtilsFor(any(Device.class))).thenAnswer(
                invocationOnMock -> new FirmwareManagementDeviceUtilsImpl(thesaurus, deviceMessageSpecificationService, firmwareService, taskService, deviceMessageService).initFor((Device) invocationOnMock.getArguments()[0])
        );
        when(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).thenReturn(true);
    }

    private DeviceMessage mockFirmwareMessage() {
        return mockFirmwareMessage(true);
    }

    private DeviceMessage mockFirmwareMessage(boolean withPropertySpecs) {
        DeviceMessage custom = mock(DeviceMessage.class);
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getCategory()).thenReturn(firmwareCategory);
        if (withPropertySpecs) {
            PropertySpec propertySpec = mockFirmwareVersionPropertySpec();
            when(messageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        }
        when(custom.getSpecification()).thenReturn(messageSpec);
        return custom;
    }

    @Test
    public void testActiveVersions() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<List>get("$.firmwares")).isNotEmpty();
        assertThat(model.<List>get("$.firmwares")).hasSize(2);
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<String>get("$.firmwares[1].firmwareType.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[1].activeVersion")).isNull();
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("communication");
        assertThat(model.<String>get("$.firmwares[0].firmwareType.localizedValue")).isNotEmpty();
        assertNotNull(model.get("$.firmwares[0].activeVersion"));
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo("COM-001-ACT");
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersionStatus.id")).isEqualTo("final");
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersionStatus.localizedValue")).isNotEmpty();
        assertThat(model.<Number>get("$.firmwares[0].activeVersion.lastCheckedDate")).isEqualTo(TIME.toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload and activate firmware

    private DeviceMessage mockUploadAndActivateImmediateFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");

        PropertySpec propertySpec = mockFirmwareVersionPropertySpec();
        when(fileAttr.getSpecification()).thenReturn(propertySpec);

        when(fileAttr.getValue()).thenReturn(firmwareVersion);
        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        DeviceMessage firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
        doReturn(messageAttributes).when(firmwareMessage).getAttributes();
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    @Test
    public void testPendingInstallFirmwareStateCase1() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());

        //If there's no activation date (so, immediate activation), this field is set to the the plannedDate.
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedActivationDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testPendingInstallFirmwareStateCase2() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
    }

    @Test
    public void testPendingInstallFirmwareStateCase3() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.SENT);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
    }

    @Test
    public void testOngoingInstallFirmwareState() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].ongoingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testFailedInstallFirmwareStateCase1() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testFailedInstallFirmwareStateCase2() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testSuccessInstallFirmwareState() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNull(model.get("$.firmwares[0].needActivationVersion"));
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testNeedVerificationForInstallEvenIfStatusCheckIsPassed() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.minusSeconds(10));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testInstallVerificationOngoing() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getStatus()).thenReturn(TaskStatus.Busy);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].ongoingVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testInstallVerificationFailed() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareCheckExecution.isLastExecutionFailed()).thenReturn(true);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareComTaskId")).isEqualTo(201);
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }


    @Test
    public void testInstallVerificationMismatch() {
        DeviceMessage firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareCheckExecution.isLastExecutionFailed()).thenReturn(true);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].wrongVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].wrongVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware with activation date

    private DeviceMessage mockUploadWithActivationDateFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");

        PropertySpec propertySpec = mockFirmwareVersionPropertySpec();
        when(fileAttr.getSpecification()).thenReturn(propertySpec);

        when(fileAttr.getValue()).thenReturn(firmwareVersion);
        DeviceMessageAttribute dateAttr = mock(DeviceMessageAttribute.class);
        when(dateAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.activationdate");
        PropertySpec activationDatePropertySpec = mockActivationDatePropertySpec();
        when(dateAttr.getSpecification()).thenReturn(activationDatePropertySpec);

        when(dateAttr.getValue()).thenReturn(new Date(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli()));
        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        messageAttributes.add(dateAttr);
        DeviceMessage firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);
        doReturn(messageAttributes).when(firmwareMessage).getAttributes();
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    private PropertySpec mockFirmwareVersionPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(BaseFirmwareVersion.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        return propertySpec;
    }

    private PropertySpec mockActivationDatePropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(Date.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getName()).thenReturn("FirmwareDeviceMessage.upgrade.activationdate");
        return propertySpec;
    }

    @Test
    public void testPendingUploadWithActivationDateFirmwareStateCase1() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedActivationDate")).isEqualTo(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testPendingUploadWithActivationDateFirmwareStateCase2() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
    }

    @Test
    public void testOngoingUploadWithActivationDateFirmwareState() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].ongoingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.plannedActivationDate")).isEqualTo(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testUploadedButNotActivatedYetFirmwareState() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        // Set activation date in future
        DeviceMessageAttribute dateAttr = mock(DeviceMessageAttribute.class);
        when(dateAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.activationdate");
        PropertySpec activationDatePropertySpec = mockActivationDatePropertySpec();
        when(dateAttr.getSpecification()).thenReturn(activationDatePropertySpec);
        when(dateAttr.getValue()).thenReturn(new Date(NOW.plus(1, ChronoUnit.DAYS).toEpochMilli()));
        firmwareMessage.getAttributes().remove(1);
        List<com.energyict.mdc.upl.messages.DeviceMessageAttribute> attributes = (List<com.energyict.mdc.upl.messages.DeviceMessageAttribute>) firmwareMessage.getAttributes();
        attributes.add(dateAttr);
        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].activatingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].activatingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].activatingVersion.plannedActivationDate")).isEqualTo(NOW.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testFailedUploadWithActivationDateFirmwareStateCase1() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Failed);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testFailedUploadWithActivationDateFirmwareStateCase2() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVersion"));
    }

    @Test
    public void testSuccessUploadWithActivationDateFirmwareState() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }


    @Test
    public void testSuccessUploadWithActivationDateFirmwareStateEvenIfStatusCheckWasPassedBefore() {
        DeviceMessage firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.minusSeconds(10));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware (activate later)

    private DeviceMessage mockInstallAndActivateLaterFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        when(fileAttr.getValue()).thenReturn(firmwareVersion);

        PropertySpec propertySpec = mockFirmwareVersionPropertySpec();
        when(fileAttr.getSpecification()).thenReturn(propertySpec);

        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        DeviceMessage firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        doReturn(messageAttributes).when(firmwareMessage).getAttributes();
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    @Test
    public void testPendingInstallAndActivateLaterFirmware() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].pendingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testOngoingInstallAndActivateLaterFirmware() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].ongoingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testFailedInstallAndActivateLaterFirmwareState() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testUploadedWaitingActivationFirmwareStateCase1() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needActivationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needActivationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testUploadedWaitingActivationFirmwareStateCase2() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1002");
        messages.add(activationMessage);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needActivationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needActivationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testOngoingActivationFirmwareState() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage activationMessage = mockFirmwareMessage(false);
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].ongoingActivatingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1002);
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testFailedActivationFirmwareState() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage activationMessage = mockFirmwareMessage(false);
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].failedActivatingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1002);
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testSuccessActivationFirmwareState() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getReleaseDate()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testSuccessActivationFirmwareStateEvenIfStatusCheckWasPassedBefore() {
        DeviceMessage firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getReleaseDate()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.minusSeconds(10));

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[0].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Custom scenarios

    /*
     - upload meter firmware
     - activate meter firmware
     - activation failed
     - upload communication firmware
     - activate communication firmware
     - activation ongoing
     Expected: only 'Activating version ...' for communication firmware should be present
    */
    @Test
    public void customScenarioWithUploadAndActivateLaterCase1() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        // successfully uploaded meter firmware
        FirmwareVersion meterFirmwareVersion = mock(FirmwareVersion.class);
        when(meterFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(meterFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFirmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute meterFileAttr = mock(DeviceMessageAttribute.class);
        when(meterFileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        when(meterFileAttr.getValue()).thenReturn(meterFirmwareVersion);
        PropertySpec firmwareVersionPropertySpec = mockFirmwareVersionPropertySpec();
        when(meterFileAttr.getSpecification()).thenReturn(firmwareVersionPropertySpec);
        List<DeviceMessageAttribute> meterMessageAttributes = new ArrayList<>();
        meterMessageAttributes.add(meterFileAttr);
        DeviceMessage uploadMeterFirmware = mockFirmwareMessage();
        when(uploadMeterFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        doReturn(meterMessageAttributes).when(uploadMeterFirmware).getAttributes();
        when(uploadMeterFirmware.getReleaseDate()).thenReturn(TIME);
        when(uploadMeterFirmware.getModTime()).thenReturn(TIME);
        when(uploadMeterFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadMeterFirmware.getId()).thenReturn(1001L);
        messages.add(uploadMeterFirmware);

        // failed activation for meter firmware
        DeviceMessage meterActivationMessage = mockFirmwareMessage();
        when(meterActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(meterActivationMessage.getTrackingId()).thenReturn("1001");
        when(meterActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(1));
        when(meterActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(1));
        when(meterActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING); //Task was failed due to connection for example
        when(meterActivationMessage.getId()).thenReturn(1002L);
        messages.add(meterActivationMessage);

        // successfully uploaded communication firmware
        FirmwareVersion communicationFirmwareVersion = mock(FirmwareVersion.class);
        when(communicationFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(communicationFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(communicationFirmwareVersion.getFirmwareVersion()).thenReturn("COMU-002-UPGR");
        DeviceMessageAttribute communicationFileAttr = mock(DeviceMessageAttribute.class);
        when(communicationFileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        when(communicationFileAttr.getValue()).thenReturn(communicationFirmwareVersion);
        PropertySpec firmwareVersionPropertySpec2 = mockFirmwareVersionPropertySpec();
        when(communicationFileAttr.getSpecification()).thenReturn(firmwareVersionPropertySpec2);
        List<DeviceMessageAttribute> communicationMessageAttributes = new ArrayList<>();
        communicationMessageAttributes.add(communicationFileAttr);
        DeviceMessage uploadCommunicationFirmware = mockFirmwareMessage();
        when(uploadCommunicationFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        doReturn(communicationMessageAttributes).when(uploadCommunicationFirmware).getAttributes();
        when(uploadCommunicationFirmware.getReleaseDate()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadCommunicationFirmware.getId()).thenReturn(1003L);
        messages.add(uploadCommunicationFirmware);

        // ongoing activation for communication firmware
        DeviceMessage communicationActivationMessage = mockFirmwareMessage(false);
        when(communicationActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(communicationActivationMessage.getTrackingId()).thenReturn("1003");
        when(communicationActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(communicationActivationMessage.getId()).thenReturn(1004L);
        messages.add(communicationActivationMessage);

        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(2));

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertNull(model.get("$.firmwares[1].failedActivatingVersion"));
        assertNotNull(model.get("$.firmwares[0].ongoingActivatingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1004);
    }

    /*
     - upload meter firmware
     - activate meter firmware
     - activation was successful, waiting verification
     - upload communication firmware
     - activate communication firmware
     - activation ongoing
     Expected:  'Activating version ..., version will be checked...' for meter firmware
                and 'Activating version ...' for communication firmware
    */
    @Test
    public void customScenarioWithUploadAndActivateLaterCase2() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        // successfully uploaded meter firmware
        FirmwareVersion meterFirmwareVersion = mock(FirmwareVersion.class);
        when(meterFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(meterFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFirmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute meterFileAttr = mock(DeviceMessageAttribute.class);
        when(meterFileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        when(meterFileAttr.getValue()).thenReturn(meterFirmwareVersion);
        PropertySpec firmwareVersionPropertySpec = mockFirmwareVersionPropertySpec();
        when(meterFileAttr.getSpecification()).thenReturn(firmwareVersionPropertySpec);
        List<DeviceMessageAttribute> meterMessageAttributes = new ArrayList<>();
        meterMessageAttributes.add(meterFileAttr);
        DeviceMessage uploadMeterFirmware = mockFirmwareMessage();
        when(uploadMeterFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        doReturn(meterMessageAttributes).when(uploadMeterFirmware).getAttributes();
        when(uploadMeterFirmware.getReleaseDate()).thenReturn(TIME);
        when(uploadMeterFirmware.getModTime()).thenReturn(TIME);
        when(uploadMeterFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadMeterFirmware.getId()).thenReturn(1001L);
        messages.add(uploadMeterFirmware);

        // successful activation for meter firmware
        DeviceMessage meterActivationMessage = mockFirmwareMessage();
        when(meterActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(meterActivationMessage.getTrackingId()).thenReturn("1001");
        when(meterActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(1));
        when(meterActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(1));
        when(meterActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(meterActivationMessage.getId()).thenReturn(1002L);
        messages.add(meterActivationMessage);

        // successfully uploaded communication firmware
        FirmwareVersion communicationFirmwareVersion = mock(FirmwareVersion.class);
        when(communicationFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(communicationFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(communicationFirmwareVersion.getFirmwareVersion()).thenReturn("COMU-002-UPGR");
        DeviceMessageAttribute communicationFileAttr = mock(DeviceMessageAttribute.class);
        when(communicationFileAttr.getName()).thenReturn("FirmwareDeviceMessage.upgrade.userfile");
        when(communicationFileAttr.getValue()).thenReturn(communicationFirmwareVersion);
        PropertySpec firmwareVersionPropertySpec2 = mockFirmwareVersionPropertySpec();
        when(communicationFileAttr.getSpecification()).thenReturn(firmwareVersionPropertySpec2);
        List<DeviceMessageAttribute> communicationMessageAttributes = new ArrayList<>();
        communicationMessageAttributes.add(communicationFileAttr);
        DeviceMessage uploadCommunicationFirmware = mockFirmwareMessage();
        when(uploadCommunicationFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES);
        doReturn(communicationMessageAttributes).when(uploadCommunicationFirmware).getAttributes();
        when(uploadCommunicationFirmware.getReleaseDate()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadCommunicationFirmware.getId()).thenReturn(1003L);
        messages.add(uploadCommunicationFirmware);

        // ongoing activation for communication firmware
        DeviceMessage communicationActivationMessage = mockFirmwareMessage(false);
        when(communicationActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(communicationActivationMessage.getTrackingId()).thenReturn("1003");
        when(communicationActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(communicationActivationMessage.getId()).thenReturn(1004L);
        messages.add(communicationActivationMessage);

        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(2));

        JsonModel model = JsonModel.model(target("/devices/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertNotNull(model.get("$.firmwares[1].needVerificationVersion"));
        assertThat(model.<Number>get("$.firmwares[1].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertNotNull(model.get("$.firmwares[0].ongoingActivatingVersion"));
        assertThat(model.<Number>get("$.firmwares[0].ongoingActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1004);
    }
}