package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.jayway.jsonpath.JsonModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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
        DeviceMessage<?> command1 = mockCommand(1, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", "DeviceMessageCategories.RESET", created, created.plusSeconds(10), null);
        DeviceMessage<?> command2 = mockCommand(2, "reset clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

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

    private DeviceMessage<?> mockCommand(Integer id, String commandName, String errorMessage, DeviceMessageStatus status, String trackingId, String userName, String categoryName, Instant creationDate, Instant releaseDate, Instant sentDate) {
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
        when(specification.getCategory()).thenReturn(category);
        when(specification.getName()).thenReturn(commandName);
        when(mock.getSpecification()).thenReturn(specification);
        return mock;
    }
}
