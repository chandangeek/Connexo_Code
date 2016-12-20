package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceMessagesResourceTest extends BaseLoadProfileTest {

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(DeviceMessageExecutionLevelTranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(DeviceMessageExecutionLevelTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Test
    public void testDeviceTypeDoesNotSupportMessages() {
        mockDeviceConfiguration();
        String response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<Object>>get("$.categories")).hasSize(0);
    }

    @Test
    public void testGetDeviceMessages() {
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration();
        DeviceType deviceType = deviceConfiguration.getDeviceType();
        DeviceProtocolPluggableClass pluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol protocol = mock(DeviceProtocol.class);

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getMessageId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getMessageId()).thenReturn(DeviceMessageId.CLOCK_SET_DST.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec3 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec3.getMessageId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE.dbValue());
        deviceMessageIds.add(deviceMessageSpec3);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec4 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec4.getMessageId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS.dbValue());
        deviceMessageIds.add(deviceMessageSpec4);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec5 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec5.getMessageId()).thenReturn(DeviceMessageId.SECURITY_CHANGE_CLIENT_PASSWORDS.dbValue());
        deviceMessageIds.add(deviceMessageSpec5);

        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(pluggableClass));
        when(pluggableClass.getDeviceProtocol()).thenReturn(protocol);
        when(protocol.getSupportedMessages()).thenReturn(deviceMessageIds);

        DeviceMessageCategory clockCategory = mock(DeviceMessageCategory.class);
        DeviceMessageSpec clockSetTimeMessage = mock(DeviceMessageSpec.class);
        DeviceMessageSpec clockSetDSTMessage = mock(DeviceMessageSpec.class);
        DeviceMessageCategory displayCategory = mock(DeviceMessageCategory.class);
        DeviceMessageSpec displayMessage = mock(DeviceMessageSpec.class);

        when(deviceMessageSpecificationService.filteredCategoriesForComTaskDefinition()).thenReturn(Arrays.asList(clockCategory, displayCategory));
        when(clockCategory.getName()).thenReturn("Clock");
        when(displayCategory.getName()).thenReturn("Display");
        doReturn(Arrays.asList(clockSetTimeMessage, clockSetDSTMessage)).when(clockCategory).getMessageSpecifications();
        when(clockSetTimeMessage.getId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME);
        when(clockSetTimeMessage.getCategory()).thenReturn(clockCategory);
        when(clockSetTimeMessage.getName()).thenReturn("Clock set time");
        when(clockSetDSTMessage.getId()).thenReturn(DeviceMessageId.CLOCK_SET_DST);
        when(clockSetDSTMessage.getCategory()).thenReturn(clockCategory);
        when(clockSetDSTMessage.getName()).thenReturn("Clock set DST");
        doReturn(Arrays.asList(displayMessage)).when(displayCategory).getMessageSpecifications();
        when(displayMessage.getId()).thenReturn(DeviceMessageId.DISPLAY_SET_MESSAGE);
        when(displayMessage.getCategory()).thenReturn(displayCategory);
        when(displayMessage.getName()).thenReturn("Display set message");

        DeviceMessageEnablement enamblement = mock(DeviceMessageEnablement.class);
        Set<DeviceMessageUserAction> privileges = new HashSet<>();
        privileges.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        privileges.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
        privileges.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(enamblement));
        when(enamblement.getDeviceMessageId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME);
        when(enamblement.getUserActions()).thenReturn(privileges);

        String response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.categories[0].name")).isEqualTo("Clock");
        assertThat(jsonModel.<List<Object>>get("$.categories[0].deviceMessageEnablements")).hasSize(2);

        assertThat(jsonModel.<Integer>get("$.categories[0].deviceMessageEnablements[0].id")).isEqualTo((int) DeviceMessageId.CLOCK_SET_DST.dbValue());
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[0].name")).isEqualTo("Clock set DST");
        assertThat(jsonModel.<Boolean>get("$.categories[0].deviceMessageEnablements[0].active")).isFalse();

        assertThat(jsonModel.<Integer>get("$.categories[0].deviceMessageEnablements[1].id")).isEqualTo((int) DeviceMessageId.CLOCK_SET_TIME.dbValue());
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].name")).isEqualTo("Clock set time");
        assertThat(jsonModel.<Boolean>get("$.categories[0].deviceMessageEnablements[1].active")).isTrue();
        assertThat(jsonModel.<List<Object>>get("$.categories[0].deviceMessageEnablements[1].privileges")).hasSize(3);
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[0].privilege")).isEqualTo(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_1);
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[0].name")).isEqualTo("Level 1");
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[1].privilege")).isEqualTo(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_2);
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[1].name")).isEqualTo("Level 2");
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[2].privilege")).isEqualTo(Privileges.Constants.EXECUTE_DEVICE_MESSAGE_3);
        assertThat(jsonModel.<String>get("$.categories[0].deviceMessageEnablements[1].privileges[2].name")).isEqualTo("Level 3");

        assertThat(jsonModel.<String>get("$.categories[1].name")).isEqualTo("Display");
        assertThat(jsonModel.<List<Object>>get("$.categories[1].deviceMessageEnablements")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.categories[1].deviceMessageEnablements[0].id")).isEqualTo((int) DeviceMessageId.DISPLAY_SET_MESSAGE.dbValue());
        assertThat(jsonModel.<String>get("$.categories[1].deviceMessageEnablements[0].name")).isEqualTo("Display set message");
        assertThat(jsonModel.<Boolean>get("$.categories[1].deviceMessageEnablements[0].active")).isFalse();
    }

    @Test
    public void testActivateDeviceMessagesIncorrectMessage() {
        mockDeviceConfiguration();
        when(deviceMessageSpecificationService.findMessageSpecById(1L)).thenReturn(Optional.empty());

        DeviceMessageEnablementInfo requestBody = new DeviceMessageEnablementInfo();
        requestBody.messageIds = Arrays.asList(1L);
        requestBody.privileges = Arrays.asList(
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1),
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
        requestBody.deviceConfiguration = new DeviceConfigurationInfo();
        requestBody.deviceConfiguration.id = 1;
        requestBody.deviceConfiguration.version = OK_VERSION;
        requestBody.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);
        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testActivateDeviceMessages() {
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration();
        DeviceMessageSpec message = mock(DeviceMessageSpec.class);
        DeviceMessageEnablementBuilder enablementBuilder = mock(DeviceMessageEnablementBuilder.class);
        when(deviceMessageSpecificationService.findMessageSpecById(1L)).thenReturn(Optional.of(message));
        when(message.getId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME);
        when(deviceConfiguration.createDeviceMessageEnablement(DeviceMessageId.CLOCK_SET_TIME)).thenReturn(enablementBuilder);

        DeviceMessageEnablementInfo requestBody = new DeviceMessageEnablementInfo();
        requestBody.messageIds = Arrays.asList(1L);
        requestBody.privileges = Arrays.asList(
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1),
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
        requestBody.deviceConfiguration = new DeviceConfigurationInfo();
        requestBody.deviceConfiguration.id = 1;
        requestBody.deviceConfiguration.version = OK_VERSION;
        requestBody.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);
        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(enablementBuilder).addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        verify(enablementBuilder).addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
    }

    @Test
    public void testDeactivateDeviceMessagesIncorrectMessage() {
        DeviceType deviceType = mockDeviceType("Some", 1L);
        when(deviceConfigurationService.findAndLockDeviceType(1L, OK_VERSION)).thenReturn(Optional.of(deviceType));

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceMessageSpecificationService.findMessageSpecById(1L)).thenReturn(Optional.empty());
        when(deviceMessageSpecificationService.findMessageSpecById(13L)).thenReturn(Optional.empty());

        DeviceMessageEnablementInfo requestBody = new DeviceMessageEnablementInfo();
        requestBody.messageIds = Arrays.asList(1L);
        requestBody.privileges = Arrays.asList(
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1),
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
        requestBody.deviceConfiguration.id = 1;
        requestBody.deviceConfiguration.version = OK_VERSION;
        requestBody.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);
        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().build(HttpMethod.DELETE, Entity.json(requestBody)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testDeactivateDeviceMessagesBadVersion() {
        DeviceType deviceType = mockDeviceType("Some", 1L);
        when(deviceConfigurationService.findAndLockDeviceType(1L, OK_VERSION)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceType(1L)).thenReturn(Optional.empty());

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1);
        when(deviceMessageSpecificationService.findMessageSpecById(1L)).thenReturn(Optional.empty());
        when(deviceMessageSpecificationService.findMessageSpecById(13L)).thenReturn(Optional.empty());

        DeviceMessageEnablementInfo requestBody = new DeviceMessageEnablementInfo();
        requestBody.messageIds = Arrays.asList(1L);
        requestBody.privileges = Arrays.asList(
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1),
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
        requestBody.deviceConfiguration.id = 1;
        requestBody.deviceConfiguration.version = OK_VERSION;
        requestBody.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);
        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().build(HttpMethod.DELETE, Entity.json(requestBody)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeactivateDeviceMessages() {
        DeviceType deviceType = mockDeviceType("Some", 1L);
        when(deviceConfigurationService.findAndLockDeviceType(1L, OK_VERSION)).thenReturn(Optional.of(deviceType));

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        DeviceMessageSpec message1 = mock(DeviceMessageSpec.class);
        DeviceMessageSpec message2 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(1)).thenReturn(Optional.of(message1));
        when(deviceMessageSpecificationService.findMessageSpecById(13)).thenReturn(Optional.of(message2));
        when(message1.getId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME);
        when(message2.getId()).thenReturn(DeviceMessageId.CLOCK_SET_DST);

        DeviceMessageEnablementInfo info = new DeviceMessageEnablementInfo();
        info.messageIds = Arrays.asList(1L, 13L);
        info.deviceConfiguration.id = 1;
        info.deviceConfiguration.version = OK_VERSION;
        info.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);

        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceConfiguration).removeDeviceMessageEnablement(DeviceMessageId.CLOCK_SET_TIME);
        verify(deviceConfiguration).removeDeviceMessageEnablement(DeviceMessageId.CLOCK_SET_DST);
    }

    @Test
    public void testChangeDeviceMessagesPrivileges() {
        DeviceType deviceType = mockDeviceType("Some", 1L);
        when(deviceConfigurationService.findAndLockDeviceType(1L, OK_VERSION)).thenReturn(Optional.of(deviceType));
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration();
        DeviceMessageEnablement enablement = mock(DeviceMessageEnablement.class);
        Set<DeviceMessageUserAction> privileges = new HashSet<>();
        privileges.add(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        when(deviceConfiguration.getDeviceMessageEnablements()).thenReturn(Arrays.asList(enablement));
        when(enablement.getDeviceMessageId()).thenReturn(DeviceMessageId.CLOCK_SET_TIME);
        when(enablement.getUserActions()).thenReturn(privileges);

        DeviceMessageEnablementInfo requestBody = new DeviceMessageEnablementInfo();
        requestBody.messageIds = Arrays.asList(15001L, 1L);
        requestBody.privileges = Arrays.asList(
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1),
                DeviceMessagePrivilegeInfo.from(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3));
        requestBody.deviceConfiguration.id = 1;
        requestBody.deviceConfiguration.version = OK_VERSION;
        requestBody.deviceConfiguration.parent = new VersionInfo<>(1L, OK_VERSION);
        Response response = target("/devicetypes/1/deviceconfigurations/1/devicemessageenablements").request().put(Entity.entity(requestBody, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(enablement).removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        verify(enablement).removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
        verify(enablement).removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
        verify(enablement).removeDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4);

        verify(enablement).addDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
        verify(enablement).addDeviceMessageUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
    }

    private DeviceConfiguration mockDeviceConfiguration() {
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>(1);
        deviceConfigurations.add(deviceConfiguration);
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceType(1, OK_VERSION)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        return deviceConfiguration;
    }
}
