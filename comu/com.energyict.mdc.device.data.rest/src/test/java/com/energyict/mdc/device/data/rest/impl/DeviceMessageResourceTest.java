package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.protocolimplv2.sdksample.SDKDeviceProtocolTestWithMandatoryProperty;
import com.jayway.jsonpath.JsonModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.energyict.mdc.device.data.rest.impl.DeviceMessageResourceTest.Necessity.Required;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetDeviceCommands() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<?> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", 3, "DeviceMessageCategories.RESET", created, created.plusSeconds(10), null);
        DeviceMessage<?> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "set clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", 4, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(command1.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List<Long>>get("$.commands[*].releaseDate")).isSortedAccordingTo((c1, c2) -> -c1.compareTo(c2));
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.commands[0].messageSpecification.name")).isEqualTo("do delete rule");
        assertThat(model.<String>get("$.commands[0].messageSpecification.id")).isEqualTo("DEVICE_ACTIONS_DEMAND_RESET");
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
    public void testGetDeviceCommandProperties() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<?> command1 = mockCommand(device, 1L, DeviceMessageId.CLOCK_SET_TIME, "set clock", "Error message", DeviceMessageStatus.PENDING, "T14", "Jeff", 3, "DeviceMessageCategories.RESET", created, created.plusSeconds(10), null);
        DeviceMessageAttribute attribute1 = mockAttribute("ID", BigDecimal.valueOf(123L), new BigDecimalFactory(), Required);
        DeviceMessageAttribute attribute2 = mockAttribute("Delete", true, new BooleanFactory(), Required);
        Date now = new Date();
        DeviceMessageAttribute attribute3 = mockAttribute("Time", now, new DateAndTimeFactory(), Required);
        when(command1.getAttributes()).thenReturn(Arrays.asList(attribute1, attribute2, attribute3));
        when(device.getMessages()).thenReturn(Arrays.asList(command1));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.commands[0].properties[0].key")).isEqualTo("ID");
        assertThat(model.<Integer>get("$.commands[0].properties[0].propertyValueInfo.value")).isEqualTo(123);
        assertThat(model.<String>get("$.commands[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(model.<Boolean>get("$.commands[0].properties[0].required")).isEqualTo(true);
        assertThat(model.<String>get("$.commands[0].properties[1].key")).isEqualTo("Delete");
        assertThat(model.<Boolean>get("$.commands[0].properties[1].propertyValueInfo.value")).isEqualTo(true);
        assertThat(model.<String>get("$.commands[0].properties[1].propertyTypeInfo.simplePropertyType")).isEqualTo("BOOLEAN");
        assertThat(model.<Boolean>get("$.commands[0].properties[1].required")).isEqualTo(true);
        assertThat(model.<String>get("$.commands[0].properties[2].key")).isEqualTo("Time");
        assertThat(model.<Long>get("$.commands[0].properties[2].propertyValueInfo.value")).isEqualTo(now.getTime());
        assertThat(model.<String>get("$.commands[0].properties[2].propertyTypeInfo.simplePropertyType")).isEqualTo("CLOCK");
        assertThat(model.<Boolean>get("$.commands[0].properties[2].required")).isEqualTo(true);
    }

    private DeviceMessageAttribute mockAttribute(String name, Object value, ValueFactory valueFactory, Necessity necessity) {
        DeviceMessageAttribute mock = mock(DeviceMessageAttribute.class);
        when(mock.getName()).thenReturn(name);
        PropertySpec spec = mock(PropertySpec.class);
        when(spec.isRequired()).thenReturn(necessity.bool());
        when(spec.getName()).thenReturn(name);
        when(spec.getValueFactory()).thenReturn(valueFactory);
        when(mock.getValue()).thenReturn(value);
        when(mock.getSpecification()).thenReturn(spec);
        return mock;
    }

    @Test
    public void testGetDeviceCommandsWithUnscheduledComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "reset clock", null, DeviceMessageStatus.PENDING, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), null);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId + 1, false); // non matching category id
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(command2.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].messageSpecification.id")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<String>get("$.commands[0].messageSpecification.name")).isEqualTo("reset clock");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isEqualTo(false);
    }

    @Test
    public void testGetDeviceCommandsWithScheduledComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "reset clock", null, DeviceMessageStatus.PENDING, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), null);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(command2.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].messageSpecification.name")).isEqualTo("reset clock");
        assertThat(model.<String>get("$.commands[0].messageSpecification.id")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isEqualTo(true);
    }

    @Test
    public void testGetSentDeviceCommandsWithScheduledComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "reset clock", null, DeviceMessageStatus.SENT, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), sent);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(command2.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].messageSpecification.id")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<String>get("$.commands[0].messageSpecification.name")).isEqualTo("reset clock");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isNull();
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isNull();
    }

    @Test
    public void testGetDeviceCommandsWithHaltedScheduleComTaskForCommand() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        int categoryId = 101;
        DeviceMessage<?> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "reset clock", null, DeviceMessageStatus.PENDING, "T15", "Jeff", categoryId, "DeviceMessageCategories.RESET", created.minusSeconds(5), created.plusSeconds(5), null);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(command2.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/commands").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.commands[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.commands[0].messageSpecification.id")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<String>get("$.commands[0].messageSpecification.name")).isEqualTo("reset clock");
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByComTask")).isEqualTo(true);
        assertThat(model.<Boolean>get("$.commands[0].willBePickedUpByScheduledComTask")).isEqualTo(false);
    }

    @Test
    public void testGetAvailableCategoriesForUserOnDevice() throws Exception {
        int categoryId = 1011;
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        // there is a total of 34 device messages supported by the protocol
        when(deviceMessageService.allCategories()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class).stream().map(DeviceMessageCategoryImpl::new).collect(Collectors.toList()));

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);

        // User has access to 2 device messages
        EnumSet<DeviceMessageId> userAuthorizedDeviceMessages = EnumSet.of(DeviceMessageId.CONTACTOR_OPEN, DeviceMessageId.CONTACTOR_CLOSE);


        DeviceMessageEnablement deviceMessageEnablement1 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement1.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        DeviceMessageEnablement deviceMessageEnablement2 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement2.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE);
        DeviceMessageEnablement deviceMessageEnablement3 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement3.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_ARM);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        // 3 device messages are enabled on the device config
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(deviceMessageEnablement1, deviceMessageEnablement2, deviceMessageEnablement3));
        when(deviceConfiguration.isAuthorized(anyObject())).thenAnswer(invocationOnMock -> userAuthorizedDeviceMessages.contains(invocationOnMock.getArguments()[0]));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getDeviceProtocol()).thenReturn(new SDKDeviceProtocolTestWithMandatoryProperty());
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        // given the above: we expect 2 device messages in the same category
        String response = target("/devices/ZABF010000080004/messagecategories").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<List>get("$")).hasSize(1); // one category
        assertThat(jsonModel.<String>get("$[0].name")).isEqualTo("Contactor");
        assertThat(jsonModel.<Integer>get("$[0].id")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$[0].deviceMessageSpecs")).hasSize(2); // 2 device messages
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[0].name")).isEqualTo("Contactor close");
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[0].id")).isEqualTo("CONTACTOR_CLOSE");
        assertThat(jsonModel.<Boolean>get("$[0].deviceMessageSpecs[0].willBePickedUpByComTask")).isNotNull();
        assertThat(jsonModel.<Boolean>get("$[0].deviceMessageSpecs[0].willBePickedUpByScheduledComTask")).isNotNull();
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[1].name")).isEqualTo("Contactor open");
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[1].id")).isEqualTo("CONTACTOR_OPEN");
    }

    @Test
    public void testGetAvailableCategoriesWithMessageSpecsForUserOnDevice() throws Exception {
        int categoryId = 1011;
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);

        // there is a total of 34 device messages supported by the protocol
        when(deviceMessageService.allCategories()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class).stream().map(DeviceMessageCategoryImpl::new).collect(Collectors.toList()));

        doAnswer(invocationOnMock->new BasicPropertySpec((String)invocationOnMock.getArguments()[0], (Boolean)invocationOnMock.getArguments()[1], (ValueFactory)invocationOnMock.getArguments()[2])).
                when(propertySpecService).basicPropertySpec(any(String.class), any(Boolean.class), any(ValueFactory.class));
        doAnswer(invocationOnMock->new BasicPropertySpec((String)invocationOnMock.getArguments()[0], (Boolean)invocationOnMock.getArguments()[1], new BigDecimalFactory())).
                when(propertySpecService).bigDecimalPropertySpecWithValues(any(String.class), any(Boolean.class), any(BigDecimal.class), any(BigDecimal.class));
        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);

        // User has access to 2 device messages
        EnumSet<DeviceMessageId> userAuthorizedDeviceMessages = EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT);


        DeviceMessageEnablement deviceMessageEnablement1 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement1.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT);
        DeviceMessageEnablement deviceMessageEnablement2 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement2.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT);
        DeviceMessageEnablement deviceMessageEnablement3 = mock(DeviceMessageEnablement.class);
        when(deviceMessageEnablement3.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_ARM);

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        // 3 device messages are enabled on the device config
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(deviceMessageEnablement1, deviceMessageEnablement2, deviceMessageEnablement3));
        when(deviceConfiguration.isAuthorized(anyObject())).thenAnswer(invocationOnMock -> userAuthorizedDeviceMessages.contains(invocationOnMock.getArguments()[0]));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(pluggableClass);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        // given the above: we expect 2 device messages in the same category
        String response = target("/devices/ZABF010000080004/messagecategories").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<List>get("$")).hasSize(1); // one category
        assertThat(jsonModel.<String>get("$[0].name")).isEqualTo("Contactor");
        assertThat(jsonModel.<Integer>get("$[0].id")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$[0].deviceMessageSpecs")).hasSize(2); // 2 device messages
        assertThat(jsonModel.<List>get("$[0].deviceMessageSpecs[0].propertySpecs")).hasSize(1);
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[0].propertySpecs[0].key")).isEqualTo("ContactorDeviceMessage.digitalOutput");
        assertThat(jsonModel.<String>get("$[0].deviceMessageSpecs[0].propertySpecs[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(jsonModel.<Boolean>get("$[0].deviceMessageSpecs[0].propertySpecs[0].required")).isEqualTo(true);
    }

    @Test
    public void testCreateDeviceMessage() throws Exception {
        DeviceMessage<Device> deviceMessage = mockDeviceMessage(1L);

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        Device.DeviceMessageBuilder deviceMessageBuilder = mock(Device.DeviceMessageBuilder.class);
        when(deviceMessageBuilder.addProperty(anyString(), anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);
        when(device.newDeviceMessage(DeviceMessageId.CONTACTOR_OPEN)).thenReturn(deviceMessageBuilder);
        DeviceMessageInfo deviceMessageInfo = new DeviceMessageInfo();
        deviceMessageInfo.releaseDate=Instant.now();
        deviceMessageInfo.messageSpecification=new DeviceMessageSpecInfo();
        deviceMessageInfo.messageSpecification.id="CONTACTOR_OPEN";
        deviceMessageInfo.properties=new ArrayList<>();
        deviceMessageInfo.properties.add(new PropertyInfo("ID",new PropertyValueInfo<>(123L, null, null),new PropertyTypeInfo(SimplePropertyType.NUMBER, null, null, null), true));
        deviceMessageInfo.properties.add(new PropertyInfo("Time",new PropertyValueInfo<>(1414067539213L, null, null),new PropertyTypeInfo(SimplePropertyType.CLOCK, null, null, null), true));
        Response response = target("/devices/ZABF010000080004/commands").request().post(Entity.json(deviceMessageInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        ArgumentCaptor<DeviceMessageId> deviceMessageIdArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageId.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> objectArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(device, times(1)).newDeviceMessage(deviceMessageIdArgumentCaptor.capture());
//        verify(deviceMessageBuilder, times(2)).addProperty(stringArgumentCaptor.capture(), objectArgumentCaptor.capture());
        verify(deviceMessageBuilder, times(1)).setReleaseDate(instantArgumentCaptor.capture());
        assertThat(deviceMessageIdArgumentCaptor.getValue()).isEqualTo(DeviceMessageId.CONTACTOR_OPEN);
//        assertThat(stringArgumentCaptor.getAllValues().get(0)).isEqualTo("ID");
//        assertThat(objectArgumentCaptor.getAllValues().get(0)).isEqualTo(123L);
//        assertThat(stringArgumentCaptor.getAllValues().get(1)).isEqualTo("Time");
//        assertThat(objectArgumentCaptor.getAllValues().get(1)).isEqualTo(1414067539213L);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(deviceMessageInfo.releaseDate);

    }

    @Test
    public void testDeleteDeviceMessage() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        DeviceMessage msg1 = mock(DeviceMessage.class);
        when(msg1.getId()).thenReturn(1L);
        DeviceMessage msg2 = mock(DeviceMessage.class);
        when(msg2.getId()).thenReturn(2L);
        DeviceMessage msg3 = mock(DeviceMessage.class);
        when(msg3.getId()).thenReturn(3L);
        when(device.getMessages()).thenReturn(Arrays.asList(msg1, msg2, msg3));
        Response response = target("/devices/ZABF010000080004/commands/2").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<DeviceMessage> argumentCaptor = ArgumentCaptor.forClass(DeviceMessage.class);
        verify(device, times(1)).removeDeviceMessage(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getId()).isEqualTo(2L);
    }

    @Test
    public void testUpdateDeviceMessage() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF010000080004")).thenReturn(device);
        DeviceMessage msg1 = mock(DeviceMessage.class);
        when(msg1.getId()).thenReturn(1L);
        DeviceMessage msg2 = mock(DeviceMessage.class);
        when(msg2.getId()).thenReturn(2L);
        DeviceMessage msg3 = mockDeviceMessage(3L);
        when(device.getMessages()).thenReturn(Arrays.asList(msg1, msg2, msg3));

        DeviceMessageInfo deviceMessageInfo = new DeviceMessageInfo();
        deviceMessageInfo.releaseDate=Instant.now();

        Response response = target("/devices/ZABF010000080004/commands/3").request().put(Entity.json(deviceMessageInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<Instant> argumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(msg3, times(1)).setReleaseDate(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(deviceMessageInfo.releaseDate);

        verify(msg3, times(1)).save();
    }

    private DeviceMessage<Device> mockDeviceMessage(long id) {
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getName()).thenReturn("category");
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN.dbValue())).thenReturn(Optional.of(deviceMessageSpec));
        DeviceMessage<Device> deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.CANCELED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.empty());
        when(deviceMessage.getId()).thenReturn(id);
        User user = mock(User.class);
        when(user.getName()).thenReturn("username");
        when(deviceMessage.getUser()).thenReturn(user);
        return deviceMessage;
    }

    private ComTaskExecution mockComTaskExecution(int categoryId, boolean isOnHold) {
        ComTaskExecution mock = mock(ComTaskExecution.class);
        ComTask comTask = mockComTaskWithProtocolTaskForCategory(categoryId);
        when(mock.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(mock.isOnHold()).thenReturn(isOnHold);
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

    private DeviceMessage<?> mockCommand(Device device, Long id, DeviceMessageId deviceMessageId, String messageSpecName, String errorMessage, DeviceMessageStatus status, String trackingId, String userName, Integer categoryId, String categoryName, Instant creationDate, Instant releaseDate, Instant sentDate) {
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
        when(specification.getId()).thenReturn(deviceMessageId);
        when(specification.getName()).thenReturn(messageSpecName);
        when(mock.getSpecification()).thenReturn(specification);
        when(mock.getDevice()).thenReturn(device);
        return mock;
    }

    enum Necessity {
        Optional(false),
        Required(true);
        private final boolean b;

        Necessity(boolean b) {
            this.b = b;
        }

        boolean bool() {
            return b;
        }
    }

    private class DeviceMessageCategoryImpl implements DeviceMessageCategory {
        private final DeviceMessageCategories category;

        private DeviceMessageCategoryImpl(DeviceMessageCategories category) {
            super();
            this.category = category;
        }

        @Override
        public String getName() {
            return thesaurus.getString(this.category.getNameResourceKey(), this.category.defaultTranslation());
        }

        @Override
        public String getDescription() {
            return thesaurus.getString(this.category.getDescriptionResourceKey(), this.category.getDescriptionResourceKey());
        }

        @Override
        public int getId() {
            return this.category.ordinal();
        }

        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return this.category.getMessageSpecifications(this, propertySpecService, thesaurus);
        }

    }

}
