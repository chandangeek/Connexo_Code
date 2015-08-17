package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.StatusInformationTask;
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

    private List<DeviceMessage<Device>> messages;


    @Before
    public void setupEnvironment() {
        messages = new ArrayList<>();

        ComTaskExecutionSession session = mock(ComTaskExecutionSession.class);
        when(session.getId()).thenReturn(102L);
        when(firmwareCheckComTask.getId()).thenReturn(201L);
        when(firmwareCheckComTask.getProtocolTasks()).thenReturn(Collections.singletonList(statusCheckTask));
        when(firmwareCheckExecution.getComTasks()).thenReturn(Collections.singletonList(firmwareCheckComTask));
        when(firmwareCheckExecution.getNextExecutionTimestamp()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(firmwareCheckExecution.getLastSession()).thenReturn(Optional.of(session));
        when(firmwareCheckExecution.isConfiguredToReadStatusInformation()).thenReturn(true);
        ComTask firmwareComTask = mock(ComTask.class);
        when(firmwareComTask.getId()).thenReturn(101L);
        when(taskService.findFirmwareComTask()).thenReturn(Optional.of(firmwareComTask));
        when(firmwareExecution.getComTasks()).thenReturn(Collections.singletonList(firmwareComTask));
        when(firmwareExecution.getLastSession()).thenReturn(Optional.of(session));
        when(deviceService.findByUniqueMrid("upgrade")).thenReturn(Optional.of(device));
        ComTaskEnablement firmwareCheckEnablement = mock(ComTaskEnablement.class);
        when(firmwareCheckEnablement.getComTask()).thenReturn(firmwareCheckComTask);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(firmwareCheckEnablement));
        when(device.getId()).thenReturn(1L);
        when(device.getmRID()).thenReturn("upgrade");
        when(device.getMessages()).thenReturn(messages);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(firmwareExecution, firmwareCheckExecution));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(communicationVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(communicationVersion.getFirmwareVersion()).thenReturn("COM-001-ACT");
        when(communicationVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
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
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER))
                .thenReturn(Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER));
        when(deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE))
                .thenReturn(Optional.empty());

        DeviceMessage<Device> custom = mock(DeviceMessage.class);
        DeviceMessageCategory messageCategory = mock(DeviceMessageCategory.class);
        when(messageCategory.getId()).thenReturn(2);
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getCategory()).thenReturn(messageCategory);
        when(custom.getSpecification()).thenReturn(messageSpec);
        when(custom.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        messages.add(custom);
        DeviceMessage<Device> firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.REVOKED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        messages.add(firmwareMessage);
    }

    private DeviceMessage<Device> mockFirmwareMessage() {
        DeviceMessage<Device> custom = mock(DeviceMessage.class);
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getCategory()).thenReturn(firmwareCategory);
        when(custom.getSpecification()).thenReturn(messageSpec);
        return custom;
    }

    @Test
    public void testActiveVersions() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<List>get("$.firmwares")).isNotEmpty();
        assertThat(model.<List>get("$.firmwares")).hasSize(2);
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<String>get("$.firmwares[1].firmwareType.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[1].activeVersion")).isNull();
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("communication");
        assertThat(model.<String>get("$.firmwares[0].firmwareType.localizedValue")).isNotEmpty();
        assertThat(model.<Object>get("$.firmwares[0].activeVersion")).isNotNull();
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo("COM-001-ACT");
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersionStatus.id")).isEqualTo("final");
        assertThat(model.<String>get("$.firmwares[0].activeVersion.firmwareVersionStatus.localizedValue")).isNotEmpty();
        assertThat(model.<Number>get("$.firmwares[0].activeVersion.lastCheckedDate")).isEqualTo(TIME.toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload and activate firmware

    private DeviceMessage<Device> mockUploadAndActivateImmediateFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(fileAttr.getValue()).thenReturn(firmwareVersion);
        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        DeviceMessage<Device> firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);
        when(firmwareMessage.getAttributes()).thenReturn(messageAttributes);
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    @Test
    public void testPendingInstallFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testPendingInstallFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
    }

    @Test
    public void testPendingInstallFirmwareStateCase3() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.SENT);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
    }

    @Test
    public void testOngoingInstallFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testFailedInstallFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testFailedInstallFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testSuccessInstallFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needActivationVersion")).isNull();
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testNeedVerificationForInstallEvenIfStatusCheckIsPassed() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.minusSeconds(10));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testInstallVerificationOngoing() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getStatus()).thenReturn(TaskStatus.Busy);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testInstallVerificationFailed() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.plusSeconds(10));
        when(firmwareCheckExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareCheckExecution.isLastExecutionFailed()).thenReturn(true);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareComTaskId")).isEqualTo(201);
        assertThat(model.<Number>get("$.firmwares[0].failedVerificationVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }


    @Test
    public void testInstallVerificationMismatch() {
        DeviceMessage<Device> firmwareMessage = mockUploadAndActivateImmediateFirmwareMessage();

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

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].wrongVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].wrongVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].wrongVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware with activation date

    private DeviceMessage<Device> mockUploadWithActivationDateFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(fileAttr.getValue()).thenReturn(firmwareVersion);
        DeviceMessageAttribute dateAttr = mock(DeviceMessageAttribute.class);
        when(dateAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName);
        when(dateAttr.getValue()).thenReturn(new Date(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli()));
        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        messageAttributes.add(dateAttr);
        DeviceMessage<Device> firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);
        when(firmwareMessage.getAttributes()).thenReturn(messageAttributes);
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    @Test
    public void testPendingUploadWithActivationDateFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedActivationDate")).isEqualTo(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testPendingUploadWithActivationDateFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
    }

    @Test
    public void testOngoingUploadWithActivationDateFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.plannedActivationDate")).isEqualTo(NOW.minus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testUploadedButNotActivatedYetFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        // Set activation date in future
        DeviceMessageAttribute dateAttr = mock(DeviceMessageAttribute.class);
        when(dateAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName);
        when(dateAttr.getValue()).thenReturn(new Date(NOW.plus(1, ChronoUnit.DAYS).toEpochMilli()));
        firmwareMessage.getAttributes().remove(1);
        firmwareMessage.getAttributes().add(dateAttr);
        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(1));
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].activatingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].activatingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].activatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].activatingVersion.plannedActivationDate")).isEqualTo(NOW.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testFailedUploadWithActivationDateFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testFailedUploadWithActivationDateFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
    }

    @Test
    public void testSuccessUploadWithActivationDateFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }


    @Test
    public void testSuccessUploadWithActivationDateFirmwareStateEvenIfStatusCheckWasPassedBefore() {
        DeviceMessage<Device> firmwareMessage = mockUploadWithActivationDateFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.minusSeconds(1));
        when(firmwareCheckExecution.getLastExecutionStartTimestamp()).thenReturn(TIME.minusSeconds(10));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware (activate later)

    private DeviceMessage<Device> mockInstallAndActivateLaterFirmwareMessage() {
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute fileAttr = mock(DeviceMessageAttribute.class);
        when(fileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(fileAttr.getValue()).thenReturn(firmwareVersion);
        List<DeviceMessageAttribute> messageAttributes = new ArrayList<>();
        messageAttributes.add(fileAttr);
        DeviceMessage<Device> firmwareMessage = mockFirmwareMessage();
        when(firmwareMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);
        when(firmwareMessage.getAttributes()).thenReturn(messageAttributes);
        when(firmwareMessage.getId()).thenReturn(1001L);
        when(firmwareMessage.getReleaseDate()).thenReturn(TIME);
        return firmwareMessage;
    }

    @Test
    public void testPendingInstallAndActivateLaterFirmware() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testOngoingInstallAndActivateLaterFirmware() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(TIME.toEpochMilli());
    }

    @Test
    public void testFailedInstallAndActivateLaterFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastExecutionStartTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testUploadedWaitingActivationFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needActivationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needActivationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testUploadedWaitingActivationFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage<Device> activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1002");
        messages.add(activationMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needActivationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needActivationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needActivationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testOngoingActivationFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage<Device> activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingActivatingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1002);
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingActivatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
    }

    @Test
    public void testFailedActivationFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage<Device> activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.isLastExecutionFailed()).thenReturn(true);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedActivatingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1002);
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedActivatingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedActivatingVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testSuccessActivationFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage<Device> activationMessage = mockFirmwareMessage();
        when(activationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(activationMessage.getTrackingId()).thenReturn("1001");
        when(activationMessage.getModTime()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getReleaseDate()).thenReturn(TIME.plus(1, ChronoUnit.DAYS));
        when(activationMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(activationMessage.getId()).thenReturn(1002L);
        messages.add(activationMessage);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareManagementOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.lastCheckedDate")).isEqualTo(TIME.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    @Test
    public void testSuccessActivationFirmwareStateEvenIfStatusCheckWasPassedBefore() {
        DeviceMessage<Device> firmwareMessage = mockInstallAndActivateLaterFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(TIME);
        messages.add(firmwareMessage);

        DeviceMessage<Device> activationMessage = mockFirmwareMessage();
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

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
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
    public void customScenarioWithUploadAndActivateLaterCase1(){
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        // successfully uploaded meter firmware
        FirmwareVersion meterFirmwareVersion = mock(FirmwareVersion.class);
        when(meterFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(meterFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFirmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute meterFileAttr = mock(DeviceMessageAttribute.class);
        when(meterFileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(meterFileAttr.getValue()).thenReturn(meterFirmwareVersion);
        List<DeviceMessageAttribute> meterMessageAttributes = new ArrayList<>();
        meterMessageAttributes.add(meterFileAttr);
        DeviceMessage<Device> uploadMeterFirmware = mockFirmwareMessage();
        when(uploadMeterFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);
        when(uploadMeterFirmware.getAttributes()).thenReturn(meterMessageAttributes);
        when(uploadMeterFirmware.getReleaseDate()).thenReturn(TIME);
        when(uploadMeterFirmware.getModTime()).thenReturn(TIME);
        when(uploadMeterFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadMeterFirmware.getId()).thenReturn(1001L);
        messages.add(uploadMeterFirmware);

        // failed activation for meter firmware
        DeviceMessage<Device> meterActivationMessage = mockFirmwareMessage();
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
        when(communicationFileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(communicationFileAttr.getValue()).thenReturn(communicationFirmwareVersion);
        List<DeviceMessageAttribute> communicationMessageAttributes = new ArrayList<>();
        communicationMessageAttributes.add(communicationFileAttr);
        DeviceMessage<Device> uploadCommunicationFirmware = mockFirmwareMessage();
        when(uploadCommunicationFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);
        when(uploadCommunicationFirmware.getAttributes()).thenReturn(communicationMessageAttributes);
        when(uploadCommunicationFirmware.getReleaseDate()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadCommunicationFirmware.getId()).thenReturn(1003L);
        messages.add(uploadCommunicationFirmware);

        // ongoing activation for communication firmware
        DeviceMessage<Device> communicationActivationMessage = mockFirmwareMessage();
        when(communicationActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(communicationActivationMessage.getTrackingId()).thenReturn("1003");
        when(communicationActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(communicationActivationMessage.getId()).thenReturn(1004L);
        messages.add(communicationActivationMessage);

        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(2));

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[1].failedActivatingVersion")).isNull();
        assertThat(model.<Object>get("$.firmwares[0].ongoingActivatingVersion")).isNotNull();
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
    public void customScenarioWithUploadAndActivateLaterCase2(){
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        // successfully uploaded meter firmware
        FirmwareVersion meterFirmwareVersion = mock(FirmwareVersion.class);
        when(meterFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.TEST);
        when(meterFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(meterFirmwareVersion.getFirmwareVersion()).thenReturn("MTR-001-UPGR");
        DeviceMessageAttribute meterFileAttr = mock(DeviceMessageAttribute.class);
        when(meterFileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(meterFileAttr.getValue()).thenReturn(meterFirmwareVersion);
        List<DeviceMessageAttribute> meterMessageAttributes = new ArrayList<>();
        meterMessageAttributes.add(meterFileAttr);
        DeviceMessage<Device> uploadMeterFirmware = mockFirmwareMessage();
        when(uploadMeterFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);
        when(uploadMeterFirmware.getAttributes()).thenReturn(meterMessageAttributes);
        when(uploadMeterFirmware.getReleaseDate()).thenReturn(TIME);
        when(uploadMeterFirmware.getModTime()).thenReturn(TIME);
        when(uploadMeterFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadMeterFirmware.getId()).thenReturn(1001L);
        messages.add(uploadMeterFirmware);

        // successful activation for meter firmware
        DeviceMessage<Device> meterActivationMessage = mockFirmwareMessage();
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
        when(communicationFileAttr.getName()).thenReturn(DeviceMessageConstants.firmwareUpdateFileAttributeName);
        when(communicationFileAttr.getValue()).thenReturn(communicationFirmwareVersion);
        List<DeviceMessageAttribute> communicationMessageAttributes = new ArrayList<>();
        communicationMessageAttributes.add(communicationFileAttr);
        DeviceMessage<Device> uploadCommunicationFirmware = mockFirmwareMessage();
        when(uploadCommunicationFirmware.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);
        when(uploadCommunicationFirmware.getAttributes()).thenReturn(communicationMessageAttributes);
        when(uploadCommunicationFirmware.getReleaseDate()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getModTime()).thenReturn(TIME.plusSeconds(2));
        when(uploadCommunicationFirmware.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(uploadCommunicationFirmware.getId()).thenReturn(1003L);
        messages.add(uploadCommunicationFirmware);

        // ongoing activation for communication firmware
        DeviceMessage<Device> communicationActivationMessage = mockFirmwareMessage();
        when(communicationActivationMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        when(communicationActivationMessage.getTrackingId()).thenReturn("1003");
        when(communicationActivationMessage.getReleaseDate()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getModTime()).thenReturn(TIME.plusSeconds(3));
        when(communicationActivationMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(communicationActivationMessage.getId()).thenReturn(1004L);
        messages.add(communicationActivationMessage);

        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(TIME.plusSeconds(2));

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[1].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[1].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<Object>get("$.firmwares[0].ongoingActivatingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingActivatingVersion.firmwareDeviceMessageId")).isEqualTo(1004);
    }
}