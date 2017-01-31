/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceMessageStatusTranslationKeys;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.impl.device.messages.ContactorDeviceMessage;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.energyict.mdc.device.data.rest.impl.DeviceMessageResourceTest.Necessity.Required;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageResourceTest extends DeviceDataRestApplicationJerseyTest {

    private DeviceMessageCategoryImpl deviceMessageCategoryDeviceActions;
    private DeviceMessageCategoryImpl deviceMessageCategoryActivityCalendar;
    private DeviceMessageCategoryImpl deviceMessageCategoryClock;

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        Stream.of(TrackingCategory.values()).forEach(this::mockTranslation);
        Stream.of(DeviceMessageStatusTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(ContactorDeviceMessage.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(TranslationKey translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Before
    public void initDependenciesOnPropertySpecService() {
        when(deviceMessageSpecificationService.filteredCategoriesForUserSelection()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class).stream().map(deviceMessageCategory -> new DeviceMessageCategoryImpl(deviceMessageCategory, thesaurus, propertySpecService)).collect(Collectors.toList()));
        when(deviceMessageSpecificationService.filteredCategoriesForComTaskDefinition()).thenReturn(EnumSet.allOf(DeviceMessageCategories.class)
                .stream()
                .map(deviceMessageCategory -> new DeviceMessageCategoryImpl(deviceMessageCategory, thesaurus, propertySpecService))
                .collect(Collectors.toList()));
        deviceMessageCategoryDeviceActions = new DeviceMessageCategoryImpl(DeviceMessageCategories.DEVICE_ACTIONS, thesaurus, propertySpecService);
        deviceMessageCategoryActivityCalendar = new DeviceMessageCategoryImpl(DeviceMessageCategories.ACTIVITY_CALENDAR, thesaurus, propertySpecService);
        deviceMessageCategoryClock = new DeviceMessageCategoryImpl(DeviceMessageCategories.CLOCK, thesaurus, propertySpecService);
    }

    @Test
    public void testGetDeviceCommands() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(10), null, deviceMessageCategoryDeviceActions);
        DeviceMessage<Device> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "set clock", null, DeviceMessageStatus.SENT, "15", "Jeff", created
                .minusSeconds(5), created.plusSeconds(5), sent, deviceMessageCategoryClock);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        when(serviceCallService.getServiceCall(14L)).thenReturn(Optional.empty());
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCallService.getServiceCall(15L)).thenReturn(Optional.of(serviceCall));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(command1.getAttributes()).thenReturn(Collections.emptyList());

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        String response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List<Long>>get("$.deviceMessages[*].releaseDate")).isSortedAccordingTo((c1, c2) -> -c1.compareTo(c2));
        assertThat(model.<Integer>get("$.deviceMessages[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceMessages[0].messageSpecification.name")).isEqualTo("do delete rule");
        assertThat(model.<String>get("$.deviceMessages[0].messageSpecification.id")).isEqualTo("DEVICE_ACTIONS_DEMAND_RESET");
        assertThat(model.<String>get("$.deviceMessages[0].trackingIdAndName.id")).isEqualTo("14");
        assertThat(model.<String>get("$.deviceMessages[0].trackingCategory.id")).isEqualTo("trackingCategory.serviceCall");
        assertThat(model.<String>get("$.deviceMessages[0].trackingCategory.name")).isEqualTo("Service call");
        assertThat(model.<Boolean>get("$.deviceMessages[0].trackingCategory.activeLink")).isEqualTo(false);
        assertThat(model.<String>get("$.deviceMessages[0].category")).isEqualTo("Device actions");
        assertThat(model.<String>get("$.deviceMessages[0].status.displayValue")).isEqualTo("Pending");
        assertThat(model.<String>get("$.deviceMessages[0].status.value")).isEqualTo("PENDING");
        assertThat(model.<Long>get("$.deviceMessages[0].releaseDate")).isEqualTo(created.plusSeconds(10).toEpochMilli());
        assertThat(model.<Long>get("$.deviceMessages[0].creationDate")).isEqualTo(created.toEpochMilli());
        assertThat(model.<Long>get("$.deviceMessages[0].sentDate")).isNull();
        assertThat(model.<String>get("$.deviceMessages[0].user")).isEqualTo("Jeff");
        assertThat(model.<String>get("$.deviceMessages[0].errorMessage")).isEqualTo("Error message");

        assertThat(model.<Long>get("$.deviceMessages[1].sentDate")).isEqualTo(sent.toEpochMilli());
        assertThat(model.<Boolean>get("$.deviceMessages[0].trackingCategory.activeLink")).isEqualTo(false);
    }

    @Test
    public void testInvalidTrackingId() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(10), null, deviceMessageCategoryDeviceActions);
        when(command1.getTrackingId()).thenReturn("XXX"); // INVALID
        when(device.getMessages()).thenReturn(Arrays.asList(command1));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(command1.getAttributes()).thenReturn(Collections.emptyList());

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        Response response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 0)
                .queryParam("limit", 10)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("trackingIdAndName");
    }

    @Test
    public void testGetDeviceCommandsByPage() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(10), null, deviceMessageCategoryDeviceActions);
        DeviceMessage<Device> command2 = mockCommand(device, 2L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(20), null, deviceMessageCategoryDeviceActions);
        DeviceMessage<Device> command3 = mockCommand(device, 3L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .minusSeconds(10), null, deviceMessageCategoryDeviceActions);
        DeviceMessage<Device> command4 = mockCommand(device, 4L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .minusSeconds(20), null, deviceMessageCategoryDeviceActions);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2, command3, command4));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(command1.getAttributes()).thenReturn(Collections.emptyList());

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        String response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 2).queryParam("limit", 2).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(4);
        assertThat(model.<Integer>get("$.deviceMessages[0].id")).isEqualTo(3);
        assertThat(model.<Integer>get("$.deviceMessages[1].id")).isEqualTo(4);
    }

    @Test
    public void testGetDeviceCommandsWithoutReleaseDate() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        Instant sent = LocalDateTime.of(2014, 10, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "do delete rule", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(10), null, deviceMessageCategoryDeviceActions);
        DeviceMessage<Device> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "set clock", null, DeviceMessageStatus.SENT, "15", "Jeff", created
                .minusSeconds(5), null, sent, deviceMessageCategoryClock);
        when(device.getMessages()).thenReturn(Arrays.asList(command1,command2));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(command1.getAttributes()).thenReturn(Collections.emptyList());

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        String response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Integer>get("$.deviceMessages[0].id")).isEqualTo(1);
        assertThat(model.<Integer>get("$.deviceMessages[1].id")).isEqualTo(2);
    }

    @Test
    public void testGetDeviceCommandProperties() throws Exception {
        Instant created = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);

        Device device = mock(Device.class);
        DeviceMessage<Device> command1 = mockCommand(device, 1L, DeviceMessageId.CLOCK_SET_TIME, "set clock", "Error message", DeviceMessageStatus.PENDING, "14", "Jeff", created, created
                .plusSeconds(10), null, deviceMessageCategoryClock);
        DeviceMessageAttribute attribute1 = mockAttribute("ID", BigDecimal.valueOf(123L), new BigDecimalFactory(), Required);
        DeviceMessageAttribute attribute2 = mockAttribute("Delete", true, new BooleanFactory(), Required);
        Date now = new Date();
        DeviceMessageAttribute attribute3 = mockAttribute("Time", now, new DateAndTimeFactory(), Required);
        when(command1.getAttributes()).thenReturn(Arrays.asList(attribute1, attribute2, attribute3));
        when(device.getMessages()).thenReturn(Arrays.<DeviceMessage<Device>>asList(command1));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CLOCK_SET_TIME, DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);
        PropertyInfo propertyInfo = new PropertyInfo("ID", "ID", new PropertyValueInfo<>(123, 123),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        String response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceMessages[0].properties[0].key")).isEqualTo("ID");
        assertThat(model.<Integer>get("$.deviceMessages[0].properties[0].propertyValueInfo.value")).isEqualTo(123);
        assertThat(model.<String>get("$.deviceMessages[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(model.<Boolean>get("$.deviceMessages[0].properties[0].required")).isEqualTo(true);
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
        DeviceMessage<Device> command2 = mockCommand(device, 2L, DeviceMessageId.CLOCK_SET_TIME, "reset clock", null, DeviceMessageStatus.PENDING, "15", "Jeff", created
                .minusSeconds(5), created.plusSeconds(5), null, deviceMessageCategoryClock);
        when(device.getMessages()).thenReturn(Arrays.asList(command2));
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId + 1, false, true); // non matching category id
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));
        when(command2.getAttributes()).thenReturn(Collections.emptyList());

        String response = target("/devices/ZABF010000080004/devicemessages").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);
        JsonModel model = JsonModel.model(response);

        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Integer>get("$.deviceMessages[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceMessages[0].messageSpecification.id")).isEqualTo("CLOCK_SET_TIME");
        assertThat(model.<String>get("$.deviceMessages[0].messageSpecification.name")).isEqualTo("reset clock");
    }

    @Test
    public void testGetAvailableCategoriesForUserOnDevice() throws Exception {
        int categoryId = 1011;
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);
        DeviceMessageCategory contactorCategory = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection().stream().filter(each -> each.getId() == DeviceMessageCategories.CONTACTOR.ordinal()).findFirst().get();

        DeviceMessageSpec deviceMessageSpec1 = contactorCategory.getMessageSpecifications().stream().filter(each -> each.getId().equals(DeviceMessageId.CONTACTOR_OPEN)).findFirst().get();
        DeviceMessageSpec deviceMessageSpec2 = contactorCategory.getMessageSpecifications().stream().filter(each -> each.getId().equals(DeviceMessageId.CONTACTOR_CLOSE)).findFirst().get();

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1));
        when(deviceConfiguration
                .getEnabledAndAuthorizedDeviceMessageSpecsIn(contactorCategory))
            .thenReturn(Arrays.asList(deviceMessageSpec2, deviceMessageSpec1));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, true, true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1));

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(pluggableClass.getDeviceProtocol()).thenReturn(new MessageTestDeviceProtocol());
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);

        // given the above: we expect 2 device messages in the same category
        String response = target("/devices/ZABF010000080004/messagecategories").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.categories")).hasSize(1); // one category
        assertThat(jsonModel.<String>get("$.categories[0].name")).isEqualTo("Contactor");
        assertThat(jsonModel.<Integer>get("$.categories[0].id")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.categories[0].deviceMessageSpecs")).hasSize(2); // 2 device messages
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[0].name")).isEqualTo("Contactor close");
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[0].id")).isEqualTo("CONTACTOR_CLOSE");
        assertThat(jsonModel.<Boolean>get("$.categories[0].deviceMessageSpecs[0].willBePickedUpByComTask")).isNotNull();
        assertThat(jsonModel.<Boolean>get("$.categories[0].deviceMessageSpecs[0].willBePickedUpByPlannedComTask")).isNotNull();
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[1].name")).isEqualTo("Contactor open");
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[1].id")).isEqualTo("CONTACTOR_OPEN");
    }

    @Test
    public void testGetAvailableCategoriesWithMessageSpecsForUserOnDevice() throws Exception {
        int categoryId = 1011;
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        DataModel dataModel = mock(DataModel.class);
        OrmService ormService = mock(OrmService.class);
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService = new PropertySpecServiceImpl(mock(TimeService.class), ormService, mock(BeanService.class));
        this.propertySpecService = new com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl(jupiterPropertySpecService, mock(DataVaultService.class), ormService);
        this.initDependenciesOnPropertySpecService();

        ComTaskEnablement comTaskEnablement1 = mockComTaskEnablement(categoryId);

        DeviceMessageCategory contactorCategory = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection().stream().filter(each -> each.getId() == DeviceMessageCategories.CONTACTOR.ordinal()).findFirst().get();
        DeviceMessageSpec deviceMessageSpec1 = contactorCategory.getMessageSpecifications().stream().filter(each -> each.getId().equals(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT)).findFirst().get();
        DeviceMessageSpec deviceMessageSpec2 = contactorCategory.getMessageSpecifications().stream().filter(each -> each.getId().equals(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT)).findFirst().get();

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement1));
        when(deviceConfiguration.getEnabledAndAuthorizedDeviceMessageSpecsIn(contactorCategory)).thenReturn(Arrays.asList(deviceMessageSpec1, deviceMessageSpec2));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution1 = mockComTaskExecution(categoryId, true, true);
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution1));

        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(EnumSet.of(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, DeviceMessageId.CONTACTOR_ARM, DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN));
        when(pluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceType()).thenReturn(deviceType);
        PropertyValueConverter propertyValueConverter = mock(PropertyValueConverter.class);
        when(propertyValueInfoService.getConverter(any())).thenReturn(propertyValueConverter);
        PropertyInfo propertyInfo = new PropertyInfo("ContactorDeviceMessage.digitalOutput", "ContactorDeviceMessage.digitalOutput", new PropertyValueInfo<>(123, 123),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        // given the above: we expect 2 device messages in the same category
        String response = target("/devices/ZABF010000080004/messagecategories").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1); // one category
        assertThat(jsonModel.<List>get("$.categories")).hasSize(1); // one category
        assertThat(jsonModel.<String>get("$.categories[0].name")).isEqualTo("Contactor");
        assertThat(jsonModel.<Integer>get("$.categories[0].id")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.categories[0].deviceMessageSpecs")).hasSize(2); // 2 device messages
        assertThat(jsonModel.<List>get("$.categories[0].deviceMessageSpecs[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[0].properties[0].key")).isEqualTo("ContactorDeviceMessage.digitalOutput");
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageSpecs[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(jsonModel.<Boolean>get("$.categories[0].deviceMessageSpecs[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testCreateDeviceMessage() throws Exception {

        Device device = mockDevice();
        DeviceMessage<Device> deviceMessage = mockDeviceMessage(1L, device);
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
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
        deviceMessageInfo.properties.add(new PropertyInfo("ID", "ID", new PropertyValueInfo<>(123L, null, null, true), new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.NUMBER, null, null, null), true));
        deviceMessageInfo.properties.add(new PropertyInfo("Time","Time",new PropertyValueInfo<>(1414067539213L, null, null, true),new PropertyTypeInfo(SimplePropertyType.CLOCK, null, null, null), true));
        Response response = target("/devices/ZABF010000080004/devicemessages").request().post(Entity.json(deviceMessageInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        ArgumentCaptor<DeviceMessageId> deviceMessageIdArgumentCaptor = ArgumentCaptor.forClass(DeviceMessageId.class);
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(device, times(1)).newDeviceMessage(deviceMessageIdArgumentCaptor.capture());
        verify(deviceMessageBuilder, times(1)).setReleaseDate(instantArgumentCaptor.capture());
        assertThat(deviceMessageIdArgumentCaptor.getValue()).isEqualTo(DeviceMessageId.CONTACTOR_OPEN);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(deviceMessageInfo.releaseDate);

    }

    @Test
    public void testDeleteDeviceMessage() throws Exception {
        Device device = mockDevice();
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("ZABF010000080004", 1L)).thenReturn(Optional.of(device));
        DeviceMessageSpec deviceMessageSpecification = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecification.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getName()).thenReturn("category");
        when(deviceMessageSpecification.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessage msg1 = mock(DeviceMessage.class);
        when(msg1.getId()).thenReturn(1L);
        when(msg1.getSpecification()).thenReturn(deviceMessageSpecification);
        DeviceMessage msg2 = mockCommand(device, 2L, DeviceMessageId.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_TYPE, "spec", "error", DeviceMessageStatus.REVOKED, "13333", "admin", null, null, null, deviceMessageCategoryActivityCalendar);
        when(deviceMessageService.findDeviceMessageById(2)).thenReturn(Optional.of(msg2));
        when(deviceMessageService.findAndLockDeviceMessageByIdAndVersion(2, 1L)).thenReturn(Optional.of(msg2));

        DeviceMessage msg3 = mock(DeviceMessage.class);
        when(msg3.getId()).thenReturn(3L);
        when(msg3.getSpecification()).thenReturn(deviceMessageSpecification);
        when(msg3.getSpecification()).thenReturn(deviceMessageSpecification);
        when(device.getMessages()).thenReturn(Arrays.asList(msg1, msg2, msg3));
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());

        DeviceMessageInfo deviceMessageInfo = new DeviceMessageInfo();
        deviceMessageInfo.status=new DeviceMessageInfo.StatusInfo();
        deviceMessageInfo.status.value = DeviceMessageStatusTranslationKeys.REVOKED.getDeviceMessageStatus().name();
        deviceMessageInfo.version = 1L;
        deviceMessageInfo.parent = new VersionInfo<>("ZABF010000080004", 1L);
        deviceMessageInfo.messageSpecification = new DeviceMessageSpecInfo();
        deviceMessageInfo.messageSpecification.name = "some";

        Response response = target("/devices/ZABF010000080004/devicemessages/2").request().put(Entity.json(deviceMessageInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(msg2, times(1)).revoke();
    }

    @Test
    public void testUpdateDeviceMessage() throws Exception {
        Device device = mockDevice();
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("ZABF010000080004", 1L)).thenReturn(Optional.of(device));
        DeviceMessage msg1 = mock(DeviceMessage.class);
        when(msg1.getId()).thenReturn(1L);
        DeviceMessage msg2 = mock(DeviceMessage.class);
        when(msg2.getId()).thenReturn(2L);
        DeviceMessage msg3 = mockDeviceMessage(3L, device);
        when(device.getMessages()).thenReturn(Arrays.asList(msg1, msg2, msg3));

        Instant expectedReleaseDate = LocalDateTime.of(2014, 10, 1, 11, 22, 33).toInstant(ZoneOffset.UTC);
        DeviceMessageInfo deviceMessageInfo = new DeviceMessageInfo();
        deviceMessageInfo.version = 1L;
        deviceMessageInfo.parent = new VersionInfo<>("ZABF010000080004", 1L);
        deviceMessageInfo.messageSpecification = new DeviceMessageSpecInfo();
        deviceMessageInfo.messageSpecification.name = "some";
        deviceMessageInfo.releaseDate = expectedReleaseDate;

        Response response = target("/devices/ZABF010000080004/devicemessages/3").request().put(Entity.json(deviceMessageInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<Instant> argumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(msg3, times(1)).setReleaseDate(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(expectedReleaseDate);

        verify(msg3, times(1)).save();
    }

    @Test
    public void testUpdateDeviceMessageBadVersion() throws Exception {
        Device device = mockDevice();
        when(deviceService.findDeviceByName("ZABF010000080004")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("ZABF010000080004", 1L)).thenReturn(Optional.of(device));

        DeviceMessage msg3 = mockDeviceMessage(3L, device);
        when(deviceMessageService.findAndLockDeviceMessageByIdAndVersion(3L, 2L)).thenReturn(Optional.empty());

        DeviceMessageInfo deviceMessageInfo = new DeviceMessageInfo();
        deviceMessageInfo.version = 2L;
        deviceMessageInfo.parent = new VersionInfo<>("ZABF010000080004", 1L);
        deviceMessageInfo.messageSpecification = new DeviceMessageSpecInfo();
        deviceMessageInfo.messageSpecification.name = "some";

        Response response = target("/devices/ZABF010000080004/devicemessages/3").request().put(Entity.json(deviceMessageInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    private Device mockDevice() {
        Device mock = mock(Device.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        return mock;
    }

    private DeviceMessage<Device> mockDeviceMessage(long id, Device device) {
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getName()).thenReturn("category");
        when(deviceMessageSpec.getCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN.dbValue())).thenReturn(Optional.of(deviceMessageSpec));
        DeviceMessage<Device> deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getSpecification()).thenReturn(deviceMessageSpec);
        when(deviceMessage.getStatus()).thenReturn(DeviceMessageStatus.REVOKED);
        when(deviceMessage.getSentDate()).thenReturn(Optional.empty());
        when(deviceMessage.getId()).thenReturn(id);
        when(deviceMessage.getDevice()).thenReturn(device);
        when(deviceMessage.getUser()).thenReturn("username");
        when(deviceMessage.getVersion()).thenReturn(1L);
        when(deviceMessageService.findDeviceMessageById(id)).thenReturn(Optional.of(deviceMessage));
        when(deviceMessageService.findAndLockDeviceMessageByIdAndVersion(id, deviceMessage.getVersion())).thenReturn(Optional.of(deviceMessage));
        return deviceMessage;
    }

    private ComTaskExecution mockComTaskExecution(int categoryId, boolean isOnHold, boolean isAdHoc) {
        ComTaskExecution mock = mock(ComTaskExecution.class);
        ComTask comTask = mockComTaskWithoutProtocolTaskForCategory();
        when(mock.getComTask()).thenReturn(comTask);
        when(mock.isOnHold()).thenReturn(isOnHold);
        when(mock.isAdHoc()).thenReturn(isAdHoc);
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
        when(task1.getDeviceMessageCategories()).thenReturn(Arrays.asList(category1, category2));
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(task1, task2));
        when(comTask.getName()).thenReturn("com task");
        when(comTask.getId()).thenReturn(54321L);
        return comTask;
    }

    private ComTask mockComTaskWithoutProtocolTaskForCategory() {
        ProtocolTask task2 = mock(ProtocolTask.class); // non matching task
        DeviceMessageCategory category1 = mock(DeviceMessageCategory.class);
        when(category1.getId()).thenReturn(-1); // non matching id
        MessagesTask task1 = mock(MessagesTask.class);
        when(task1.getDeviceMessageCategories()).thenReturn(Arrays.asList(category1));
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.asList(task1, task2));
        when(comTask.getName()).thenReturn("com task");
        when(comTask.getId()).thenReturn(54321L);
        return comTask;
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
        when(mock.getTrackingCategory()).thenReturn(TrackingCategory.serviceCall);
        when(serviceCallService.getServiceCall(Long.parseLong(trackingId))).thenReturn(Optional.empty());
        when(mock.getUser()).thenReturn(userName);
        DeviceMessageSpec specification = mock(DeviceMessageSpec.class);
        when(specification.getCategory()).thenReturn(deviceMessageCategory);
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

    public static class MessageTestDeviceProtocol implements DeviceProtocol {

        private EnumSet<DeviceMessageId> deviceMessageIds =
                EnumSet.of(DeviceMessageId.CONTACTOR_CLOSE, DeviceMessageId.CONTACTOR_OPEN);

        @Override
        public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
            return Optional.empty();
        }

        @Override
        public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public void init(OfflineDevice offlineDevice, ComChannel comChannel) {

        }

        @Override
        public void terminate() {

        }

        @Override
        public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
            return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
        }

        @Override
        public String getProtocolDescription() {
            return "Test protocol for messages";
        }

        @Override
        public DeviceFunction getDeviceFunction() {
            return null;
        }

        @Override
        public ManufacturerInformation getManufacturerInformation() {
            return null;
        }

        @Override
        public List<ConnectionType> getSupportedConnectionTypes() {
            return Collections.emptyList();
        }

        @Override
        public void logOn() {

        }

        @Override
        public void daisyChainedLogOn() {

        }

        @Override
        public void logOff() {

        }

        @Override
        public void daisyChainedLogOff() {

        }

        @Override
        public String getSerialNumber() {
            return null;
        }

        @Override
        public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {

        }

        @Override
        public DeviceProtocolCache getDeviceCache() {
            return null;
        }

        @Override
        public void setTime(Date timeToSet) {

        }

        @Override
        public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
            return null;
        }

        @Override
        public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
            return null;
        }

        @Override
        public Date getTime() {
            return null;
        }

        @Override
        public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
            return null;
        }

        @Override
        public Set<DeviceMessageId> getSupportedMessages() {
            return deviceMessageIds;
        }

        @Override
        public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
            return null;
        }

        @Override
        public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
            return null;
        }

        @Override
        public String format(PropertySpec propertySpec, Object messageAttribute) {
            return null;
        }

        @Override
        public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
            return Collections.emptyList();
        }

        @Override
        public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

        }

        @Override
        public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
            return null;
        }

        @Override
        public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

        }

        @Override
        public CollectedTopology getDeviceTopology() {
            return null;
        }

        @Override
        public String getVersion() {
            return "Pré-Alpha";
        }

        @Override
        public void copyProperties(TypedProperties properties) {

        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public CollectedFirmwareVersion getFirmwareVersions() {
            return null;
        }

        @Override
        public CollectedBreakerStatus getBreakerStatus() {
            return null;
        }

        @Override
        public CollectedCalendar getCollectedCalendar() {
            return null;
        }
    }

}