package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.energyict.mdc.device.data.impl.DeviceMessageServiceImplTest.Progress.OnHold;
import static com.energyict.mdc.device.data.impl.DeviceMessageServiceImplTest.Progress.Planned;
import static com.energyict.mdc.device.data.impl.DeviceMessageServiceImplTest.RunMode.AdHoc;
import static com.energyict.mdc.device.data.impl.DeviceMessageServiceImplTest.RunMode.ManuallyScheduled;
import static com.energyict.mdc.device.data.impl.DeviceMessageServiceImplTest.RunMode.SharedScheduled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/31/14.
 */
public class DeviceMessageServiceImplTest extends PersistenceIntegrationTest {
    private Device device;
    private DeviceConfiguration deviceConfiguration;
    private DeviceMessageCategory wrongCategory = new DeviceMessageCategoryImpl(DeviceMessageCategories.ACTIVITY_CALENDAR);
    private DeviceMessageCategory deviceMessageCategory = new DeviceMessageCategoryImpl(DeviceMessageCategories.DEVICE_ACTIONS);

    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    DeviceService deviceService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    Clock clock;

    DeviceMessageService deviceMessageService = new DeviceMessageServiceImpl(new DeviceDataModelServiceImpl(), threadPrincipalService);

    DeviceMessage<Device> command1;

    @Before
    public void setUp() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        device = mock(Device.class);
        when(deviceService.findDeviceByMrid("ZABF010000080004")).thenReturn(Optional.of(device));

        when(deviceMessageSpecificationService.filteredCategoriesForUserSelection()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class)
                .stream()
                .map(deviceMessageCategory -> new DeviceMessageCategoryImpl(deviceMessageCategory))
                .collect(Collectors.toList()));
        when(deviceMessageSpecificationService.filteredCategoriesForComTaskDefinition()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class)
                .stream()
                .map(deviceMessageCategory -> new DeviceMessageCategoryImpl(deviceMessageCategory))
                .collect(Collectors.toList()));
        command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", created, created.plusSeconds(10), null, deviceMessageCategory);
        when(device.getMessages()).thenReturn(Arrays.asList(command1));
        EnumSet<DeviceMessageId> userAuthorizedDeviceMessages = EnumSet.of(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_ARM);

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
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

    }

    @Test
    @Transactional
    public void testGetPreferredComTaskIfAnOnHoldAdHocExists() throws Exception {

        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", deviceMessageCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", deviceMessageCategory, allEnablements);

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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("AdHoc on hold");

    }

    @Test
    @Transactional
    public void testGetPreferredComTaskIfDanglingComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", deviceMessageCategory, allEnablements);

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


        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Merely enabled");

    }

    @Test
    @Transactional
    public void testGetPreferredComTaskIfPlannedAdHocComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("AdHoc planned");
    }

    @Test
    @Transactional
    public void testGetPreferredComTaskIfOnHoldManuallyScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);

        mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Manually scheduled on hold");
    }

    @Test
    @Transactional
    public void testGetPreferredComTaskIfRunningManuallyScheduledComTaskExists() throws Exception {
        List<ComTaskEnablement> allEnablements = new ArrayList<>();
        List<ComTaskExecution> allComTaskExecutions = new ArrayList<>();

        ComTask comTask1 = mockComTaskWithProtocolTaskForCategory(1L, "AdHoc on hold", wrongCategory, allEnablements);
        mockComTaskExecution(comTask1, OnHold, AdHoc, allComTaskExecutions);
        mockComTaskWithProtocolTaskForCategory(2L, "Merely enabled", wrongCategory, allEnablements);

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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Manually scheduled running");

    }

    @Test
    @Transactional
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


        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Scheduled on hold");

    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Scheduled running");
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isFalse();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isFalse();
        assertThat(deviceMessageService.getPreferredComTask(device, command1)).isNull();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isFalse();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isFalse();
        assertThat(deviceMessageService.getPreferredComTask(device, command1).getName()).isEqualTo("Merely enabled");
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isFalse();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isFalse();
    }

    @Test
    @Transactional
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

        assertThat(deviceMessageService.willDeviceMessageBePickedUpByComTask(device, command1)).isTrue();
        assertThat(deviceMessageService.willDeviceMessageBePickedUpByPlannedComTask(device, command1)).isTrue();
    }

    private ComTaskExecution mockComTaskExecution(ComTask comTask, Progress progress, RunMode runMode, List<ComTaskExecution> comTaskExecutions) {
        ComTaskExecution mock = mock(runMode.clazz());
        when(mock.getComTask()).thenReturn(comTask);
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
        SharedScheduled(false, false, ComTaskExecution.class),
        ManuallyScheduled(false, true, ComTaskExecution.class),
        AdHoc(true, true, ComTaskExecution.class);
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

    private class DeviceMessageCategoryImpl implements DeviceMessageCategory {
        private final DeviceMessageCategories category;
        private final int deviceMessageCategoryId;

        private DeviceMessageCategoryImpl(DeviceMessageCategories category) {
            super();
            this.category = category;
            deviceMessageCategoryId = this.category.ordinal();
        }

        @Override
        public String getName() {
            return thesaurus.getString(this.category.getNameResourceKey(), this.category.getDefaultFormat());
        }

        @Override
        public String getDescription() {
            return thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
        }

        @Override
        public int getId() {
            return deviceMessageCategoryId;
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return this.category.getMessageSpecifications(this, propertySpecService, thesaurus);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeviceMessageCategoryImpl)) {
                return false;
            }

            DeviceMessageCategoryImpl that = (DeviceMessageCategoryImpl) o;

            return deviceMessageCategoryId == that.deviceMessageCategoryId;
        }

        @Override
        public int hashCode() {
            int result = category.hashCode();
            result = 31 * result + deviceMessageCategoryId;
            return result;
        }
    }


}
