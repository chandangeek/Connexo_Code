package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.rest.impl.DeviceMessagePreferredComTaskTest.Progress.OnHold;
import static com.energyict.mdc.device.data.rest.impl.DeviceMessagePreferredComTaskTest.Progress.Planned;
import static com.energyict.mdc.device.data.rest.impl.DeviceMessagePreferredComTaskTest.RunMode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/31/14.
 */
public class DeviceMessagePreferredComTaskTest extends DeviceDataRestApplicationJerseyTest {

    private Device device;
    private DeviceConfiguration deviceConfiguration;
    private DeviceMessageCategory wrongCategory = new DeviceMessageCategoryImpl(DeviceMessageCategories.ACTIVITY_CALENDAR, thesaurus, propertySpecService);
    private DeviceMessageCategory deviceMessageCategory = new DeviceMessageCategoryImpl(DeviceMessageCategories.DEVICE_ACTIONS, thesaurus, propertySpecService);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(Optional.of(device));

        when(deviceMessageSpecificationService.filteredCategoriesForUserSelection()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class).stream().map(deviceMessageCategory -> new DeviceMessageCategoryImpl(deviceMessageCategory, thesaurus, propertySpecService)).collect(Collectors.toList()));
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", created, created.plusSeconds(10), null, deviceMessageCategory);
        when(device.getMessages()).thenReturn(Arrays.asList(command1));
        EnumSet<DeviceMessageId> userAuthorizedDeviceMessages = EnumSet.of(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_CLOSE,DeviceMessageId.CONTACTOR_ARM);

        DeviceMessageEnablement deviceMessageEnablement1 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement1.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        DeviceMessageEnablement deviceMessageEnablement2 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement2.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE);
        DeviceMessageEnablement deviceMessageEnablement3 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement3.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_ARM);

        deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(deviceMessageEnablement1, deviceMessageEnablement2, deviceMessageEnablement3));
        when(deviceConfiguration.isAuthorized(anyObject())).thenAnswer(invocationOnMock -> userAuthorizedDeviceMessages.contains(invocationOnMock.getArguments()[0]));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

    }

    @Test
    public void testGetPreferredComTaskIfAnOnHoldAdHocExists() throws Exception {

        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", deviceMessageCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("AdHoc on hold");

    }

    @Test
    public void testGetPreferredComTaskIfDanglingComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", deviceMessageCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Merely enabled");

    }

    @Test
    public void testGetPreferredComTaskIfPlannedAdHocComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(3);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("AdHoc planned");

    }

    @Test
    public void testGetPreferredComTaskIfOnHoldManuallyScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(4);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Manually scheduled on hold");

    }

    @Test
    public void testGetPreferredComTaskIfRunningManuallyScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(5);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Manually scheduled running");

    }

    @Test
    public void testGetPreferredComTaskIfOnHoldSharedScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(6);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Scheduled on hold");

    }

    @Test
    public void testGetPreferredComTaskIfRunningSharedScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(7);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Scheduled running");

    }

    @Test
    public void testGetPreferredComTaskIfThereIsNoComTaskAvailableForTheCategory() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(false);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(false);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask")).isNull();

    }

    @Test
    public void testWillBePickedUpIfThereAreOnlyOnHoldAhHocComTasksAvailable() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(false);
    }

    @Test
    public void testWillBePickedUpIfThereIsOnlyDanglingComTaskAvailable() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", deviceMessageCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(false);
        assertThat(model.<Integer>get("$.deviceMessages[0].preferredComTask.id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceMessages[0].preferredComTask.name")).isEqualTo("Merely enabled");
    }

    @Test
    public void testWillBePickedUpIfThereIsOnlyAdHocPlannedAdHoc() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
    }

    @Test
    public void testWillBePickedUpIfThereIsOnlyOnHoldManuallyScheduled() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(false);
    }

    @Test
    public void testWillBePickedUpIfThereIsOnePlannedManuallyScheduled() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
    }

    @Test
    public void testWillBePickedUpIfThereIsOnlyOnHoldScheduledComTaskAvailable() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(false);
    }

    @Test
    public void testWillBePickedUpIfThereIsOnePlannedSharedScheduled() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        ComTask comTask2 = mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

        ComTask comTask3 = mockComTaskWithProtocolTaskForCategory(3L, "AdHoc planned", wrongCategory, allEnablements);
        mockComTaskExecution(comTask3, Planned, AdHoc, allComTaskExecutions);

        ComTask comTask4 = mockComTaskWithProtocolTaskForCategory(4L, "Manually scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask4, OnHold, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask5 = mockComTaskWithProtocolTaskForCategory(5L, "Manually scheduled running", wrongCategory, allEnablements);
        mockComTaskExecution(comTask5, Planned, ManuallyScheduled, allComTaskExecutions);

        ComTask comTask6 = mockComTaskWithProtocolTaskForCategory(6L, "Scheduled on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask6, OnHold, SharedScheduled, allComTaskExecutions);

        ComTask comTask7 = mockComTaskWithProtocolTaskForCategory(7L, "Scheduled running", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask7, Planned, SharedScheduled, allComTaskExecutions);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(allEnablements);
        when(device.getComTaskExecutions()).thenReturn(allComTaskExecutions);

        String response = target("/devices/ZABF010000080004/devicemessages").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.deviceMessages[0].willBePickedUpByPlannedComTask")).isEqualTo(true);
    }

    private ComTaskExecution mockComTaskExecution(ComTask comTask, Progress progress, RunMode runMode, List<ComTaskExecution> comTaskExecutions) {
        ComTaskExecution mock = mock(runMode.clazz());
        when(mock.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(mock.isOnHold()).thenReturn(progress.onHold());
        when(mock.isAdHoc()).thenReturn(runMode.adHoc());
        when(mock.isScheduledManually()).thenReturn(runMode.scheduledManually());
        comTaskExecutions.add(mock);
        return mock;
    }

    private DeviceMessage<Device> mockCommand(Device device, Long id, DeviceMessageId deviceMessageId, String messageSpecName, String errorMessage, DeviceMessageStatus status, String trackingId, String userName, Instant creationDate, Instant releaseDate, Instant sentDate, DeviceMessageCategory deviceMessageCategory) {
        DeviceMessage mock = mock(DeviceMessage.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getSentDate()).thenReturn(Optional.ofNullable(sentDate));
        when(mock.getCreationDate()).thenReturn(creationDate);
        when(mock.getReleaseDate()).thenReturn(releaseDate);
        when(mock.getProtocolInfo()).thenReturn(errorMessage);
        when(mock.getStatus()).thenReturn(status);
        when(mock.getTrackingId()).thenReturn(trackingId);
        when(mock.getUser()).thenReturn(userName);
        DeviceMessageSpec specification = mock(DeviceMessageSpec.class);
        when(specification.getCategory()).thenReturn(deviceMessageCategory);
        when(specification.getId()).thenReturn(deviceMessageId);
        when(specification.getName()).thenReturn(messageSpecName);
        when(mock.getSpecification()).thenReturn(specification);
        when(mock.getDevice()).thenReturn(device);
        return mock;
    }

    private ComTaskEnablement mockComTaskEnablement(ComTask comTask) {
        ComTaskEnablement mock = mock(ComTaskEnablement.class);
        when(mock.getComTask()).thenReturn(comTask);
        return mock;
    }

    private ComTask mockComTaskWithProtocolTaskForCategory(long id, String name, DeviceMessageCategory deviceMessageCategory, List<ComTaskEnablement> allEnablements) {
        ProtocolTask task2 = mock(ProtocolTask.class); // non matching task
        DeviceMessageCategory category2 = mock(DeviceMessageCategory.class);
        when(category2.getId()).thenReturn(-1); // non matching id
        MessagesTask task1 = mock(MessagesTask.class);
        when(task1.getDeviceMessageCategories()).thenReturn(Arrays.asList(deviceMessageCategory, category2));
        ComTask comTask = mock(ComTask.class);
        when(task1.getComTask()).thenReturn(comTask);
        when(task2.getComTask()).thenReturn(comTask);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(task1, task2));
        when(comTask.getName()).thenReturn(name);
        when(comTask.getId()).thenReturn(id);
        allEnablements.add(mockComTaskEnablement(comTask));
        return comTask;
    }

    enum Progress {
        OnHold(true),
        Planned(false);
        private final boolean onHold;

        Progress(boolean onHold) {
            this.onHold = onHold;
        }

        boolean onHold() {
            return onHold;
        }
    }

    enum RunMode {
        SharedScheduled(false, false, ScheduledComTaskExecution.class),
        ManuallyScheduled(false, true, ManuallyScheduledComTaskExecution.class),
        AdHoc(true, true, ManuallyScheduledComTaskExecution.class);
        private final boolean adHoc;
        private final boolean scheduledManually;
        private final Class<? extends ComTaskExecution> comTaskExecutionClass;

        RunMode(boolean adHoc, boolean scheduledManually, Class<? extends ComTaskExecution> comTaskExecutionClass) {
            this.adHoc = adHoc;
            this.scheduledManually = scheduledManually;
            this.comTaskExecutionClass = comTaskExecutionClass;
        }

        public Class<? extends ComTaskExecution> clazz() {
            return comTaskExecutionClass;
        }

        boolean adHoc() {
            return adHoc;
        }

        boolean scheduledManually() {
            return scheduledManually;
        }
    }



}
