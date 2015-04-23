package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceFirmwareVersionFactoryTest extends BaseFirmwareTest {

    public static final Instant NOW = Instant.ofEpochMilli(1420608345885L);
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private FirmwareComTaskExecution firmwareExecution;
    @Mock
    private BasicCheckTask basicCheckTask;
    @Mock
    private ComTask basicCheckComTask;
    @Mock
    private ComTaskExecution basicCheckExecution;
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

        when(basicCheckComTask.getProtocolTasks()).thenReturn(Collections.singletonList(basicCheckTask));
        when(basicCheckExecution.getComTasks()).thenReturn(Collections.singletonList(basicCheckComTask));
        when(basicCheckExecution.getNextExecutionTimestamp()).thenReturn(NOW.plus(1, ChronoUnit.DAYS));
        ComTask firmwareComTask = mock(ComTask.class);
        when(firmwareComTask.getId()).thenReturn(101L);
        ComTaskExecutionSession session = mock(ComTaskExecutionSession.class);
        when(session.getId()).thenReturn(102L);
        when(firmwareExecution.getComTask()).thenReturn(firmwareComTask);
        when(firmwareExecution.getLastSession()).thenReturn(Optional.of(session));
        when(deviceService.findByUniqueMrid("upgrade")).thenReturn(Optional.of(device));
        when(device.getId()).thenReturn(1L);
        when(device.getmRID()).thenReturn("upgrade");
        when(device.getMessages()).thenReturn(messages);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(firmwareExecution, basicCheckExecution));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(communicationVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(communicationVersion.getFirmwareVersion()).thenReturn("COM-001-ACT");
        when(communicationVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(activatedCommunicationVersion.getDevice()).thenReturn(device);
        when(activatedCommunicationVersion.getFirmwareVersion()).thenReturn(communicationVersion);
        when(activatedCommunicationVersion.getLastChecked()).thenReturn(NOW);
        when(firmwareService.getCurrentMeterFirmwareVersionFor(device)).thenReturn(Optional.<ActivatedFirmwareVersion>empty());
        when(firmwareService.getCurrentCommunicationFirmwareVersionFor(device)).thenReturn(Optional.of(activatedCommunicationVersion));
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
        when(firmwareMessage.getReleaseDate()).thenReturn(NOW);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
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
        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<List>get("$.firmwares")).isNotEmpty();
        assertThat(model.<List>get("$.firmwares")).hasSize(2);
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<String>get("$.firmwares[0].firmwareType.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].activeVersion")).isNull();
        assertThat(model.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("communication");
        assertThat(model.<String>get("$.firmwares[1].firmwareType.localizedValue")).isNotEmpty();
        assertThat(model.<Object>get("$.firmwares[1].activeVersion")).isNotNull();
        assertThat(model.<String>get("$.firmwares[1].activeVersion.firmwareVersion")).isEqualTo("COM-001-ACT");
        assertThat(model.<String>get("$.firmwares[1].activeVersion.firmwareVersionStatus.id")).isEqualTo("final");
        assertThat(model.<String>get("$.firmwares[1].activeVersion.firmwareVersionStatus.localizedValue")).isNotEmpty();
        assertThat(model.<Number>get("$.firmwares[1].activeVersion.lastCheckedDate")).isEqualTo(NOW.toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload and activate firmware

    private DeviceMessage<Device> mockInstallFirmwareMessage() {
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
        when(firmwareMessage.getReleaseDate()).thenReturn(NOW);
        return firmwareMessage;
    }

    @Test
    public void testPendingInstallFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareUpgradeOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareUpgradeOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].pendingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].pendingVersion.plannedDate")).isEqualTo(NOW.toEpochMilli());
    }

    @Test
    public void testPendingInstallFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
    }

    @Test
    public void testPendingInstallFirmwareStateCase3() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.SENT);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Pending);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<Object>get("$.firmwares[0].pendingVersion")).isNotNull();
    }

    @Test
    public void testOngoingInstallFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(NOW);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].ongoingVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareUpgradeOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareUpgradeOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].ongoingVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].ongoingVersion.uploadStartDate")).isEqualTo(NOW.toEpochMilli());
    }

    @Test
    public void testFailedInstallFirmwareStateCase1() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Failed);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(NOW);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareUpgradeOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareUpgradeOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }

    @Test
    public void testFailedInstallFirmwareStateCase2() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.FAILED);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(NOW);
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].failedVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareUpgradeOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareUpgradeOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].failedVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskId")).isEqualTo(101);
        assertThat(model.<Number>get("$.firmwares[0].failedVersion.firmwareComTaskSessionId")).isEqualTo(102);
    }


    @Test
    public void testSuccessInstallFirmwareState() {
        DeviceMessage<Device> firmwareMessage = mockInstallFirmwareMessage();

        when(firmwareMessage.getStatus()).thenReturn(DeviceMessageStatus.CONFIRMED);
        when(firmwareMessage.getModTime()).thenReturn(NOW);
        when(firmwareExecution.getStatus()).thenReturn(TaskStatus.Waiting);
        when(firmwareExecution.getExecutionStartedTimestamp()).thenReturn(NOW.minusSeconds(1));
        when(firmwareExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(NOW.minusSeconds(1));
        messages.add(firmwareMessage);

        JsonModel model = JsonModel.model(target("/device/upgrade/firmwares").request().get(String.class));
        assertThat(model.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(model.<Object>get("$.firmwares[0].needVerificationVersion")).isNotNull();
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.firmwareDeviceMessageId")).isEqualTo(1001);
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareUpgradeOption.id")).isEqualTo(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE.getId());
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareUpgradeOption.localizedValue")).isNotEmpty();
        assertThat(model.<String>get("$.firmwares[0].needVerificationVersion.firmwareVersion")).isEqualTo("MTR-001-UPGR");
        assertThat(model.<Number>get("$.firmwares[0].needVerificationVersion.checkDate")).isEqualTo(NOW.plus(1, ChronoUnit.DAYS).toEpochMilli());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware with activation date
    // TODO

    // -----------------------------------------------------------------------------------------------------------------
    // Test upload firmware (activate later)
    // TODO
}