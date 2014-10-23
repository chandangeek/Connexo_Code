package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.jayway.jsonpath.JsonModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceCommandResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetDeviceCommands() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<?> command1 = mockCommand(device, 1, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", 3, "DeviceMessageCategories.RESET", created, created.plusSeconds(10), null);
        DeviceMessage<?> command2 = mockCommand(device, 2, "reset clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", 4, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List<Long>>get("$.commands[*].releaseDate")).isSortedAccordingTo((c1,c2)->-c1.compareTo(c2));
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.commands[0].name")).isEqualTo("do delete rule");
        assertThat(model.<String>get("$.commands[0].trackingId")).isEqualTo("T14");
        assertThat(model.<String>get("$.commands[0].category")).isEqualTo("DeviceMessageCategories.RESET");
        assertThat(model.<String>get("$.commands[0].status")).isEqualTo("Pending");
        assertThat(model.<Long>get("$.commands[0].releaseDate")).isEqualTo(created.plusSeconds(10).toEpochMilli());
        assertThat(model.<Long>get("$.commands[0].creationDate")).isEqualTo(created.toEpochMilli());
        assertThat(model.<Long>get("$.commands[0].sentDate")).isNull();
        assertThat(model.<String>get("$.commands[0].user")).isEqualTo("Jeff");
        assertThat(model.<String>get("$.commands[0].errorMessage")).isEqualTo("Error message");

        assertThat(model.<Long>get("$.commands[1].sentDate")).isEqualTo(sent.toEpochMilli());
    }

    @Test
    public void testGetDeviceCommandsWithUnscheduledComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2, "reset clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId+1); // non matching category id
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].name")).isEqualTo("reset clock");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isEqualTo(false);
    }

    @Test
    public void testGetDeviceCommandsWithScheduledComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2, "reset clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].name")).isEqualTo("reset clock");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isEqualTo(true);
    }

    private ComTaskExecution mockComTaskExecution(int categoryId) {
        ComTaskExecution mock = mock(ComTaskExecution.class);
        ComTask comTask = mockComTaskWithProtocolTaskForCategory(categoryId);
        when(mock.getComTasks()).thenReturn(Arrays.asList(comTask));
        return mock;
    }

    private ComTaskEnablement mockComTaskEnablement(Integer categoryId) {
        ComTaskEnablement comTaskEnablement1 = mock(ComTaskEnablement.class);
        ComTask comTask = mockComTaskWithProtocolTaskForCategory(categoryId);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask);
        return comTaskEnablement1;
    }

    private ComTask mockComTaskWithProtocolTaskForCategory(Integer categoryId) {
        ProtocolTask task2 = mock(ProtocolTask.class); // non matching task
        DeviceMessageCategory category1 = mock(DeviceMessageCategory.class);
        when(category1.getId()).thenReturn(categoryId);
        DeviceMessageCategory category2 = mock(DeviceMessageCategory.class);
        when(category2.getId()).thenReturn(-1); // non matching id
        MessagesTask task1 = mock(MessagesTask.class);
        when(task1.getDeviceMessageCategories()).thenReturn(Arrays.asList(category1));
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(task1, task2));
        return comTask;
    }

    private DeviceMessage<?> mockCommand(Device device, Integer id, String commandName, String errorMessage, DeviceMessageStatus status, String trackingId, String userName, Integer categoryId, String categoryName, Instant creationDate, Instant releaseDate, Instant sentDate) {
        DeviceMessage mock = mock(DeviceMessage.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getSentDate()).thenReturn(Optional.ofNullable(sentDate));
        when(mock.getCreationDate()).thenReturn(creationDate);
        when(mock.getReleaseDate()).thenReturn(releaseDate);
        when(mock.getProtocolInfo()).thenReturn(errorMessage);
        when(mock.getStatus()).thenReturn(status);
        when(mock.getTrackingId()).thenReturn(trackingId);
        User user = mock(User.class);
        when(user.getName()).thenReturn(userName);
        when(mock.getUser()).thenReturn(user);
        DeviceMessageSpec specification = mock(DeviceMessageSpec.class);
        DeviceMessageCategory category = mock(DeviceMessageCategory.class);
        when(category.getName()).thenReturn(categoryName);
        when(category.getId()).thenReturn(categoryId);
        when(specification.getCategory()).thenReturn(category);
        when(specification.getName()).thenReturn(commandName);
        when(mock.getSpecification()).thenReturn(specification);
        when(mock.getDevice()).thenReturn(device);
        return mock;
    }
}
