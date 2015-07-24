package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.*;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageImplTest extends PersistenceIntegrationTest {

    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    private User testUser;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;

    @Before
    public void initBefore() {

        deviceProtocolPluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newDeviceProtocolPluggableClass("Pluggable", MessageTestDeviceProtocol.class.getName());
        deviceProtocolPluggableClass.save();

        Group group = inMemoryPersistence.getUserService().createGroup("MyDefaultGroup", "just for testing");
        group.grant("MDC", Privileges.EXECUTE_DEVICE_MESSAGE_1);
        group.grant("MDC", Privileges.EXECUTE_DEVICE_MESSAGE_2);
        group.grant("MDC", Privileges.EXECUTE_DEVICE_MESSAGE_3);
        group.grant("MDC", Privileges.EXECUTE_DEVICE_MESSAGE_4);
        group.save();
        testUser = inMemoryPersistence.getUserService().createUser("TestUser", "This user is just to satisfy the foreign key ...");
        testUser.join(group);
        testUser.save();
        inMemoryPersistence.getThreadPrincipalService().set(testUser);
        freezeClock(1970, Calendar.JANUARY, 1); // Experiencing timing issues in tests that set clock back in time and the respective devices need their device life cycle
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("MyTestDeviceType", deviceProtocolPluggableClass);
        deviceType.save();
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("ConfigForMessaging");
        deviceConfiguration = deviceConfigurationBuilder.add();
        deviceMessageIds.stream().forEach(deviceConfiguration::createDeviceMessageEnablement);
        deviceConfiguration.activate();
        resetClock();
    }

    private Device createSimpleDeviceWithName(String name, String mRID) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Instant initializeClockWithCurrentBeforeReleaseInstant() {
        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        Instant myReleaseInstant = myCurrentInstant.plusSeconds(100L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(myCurrentInstant);
        return myReleaseInstant;
    }

    private Instant initializeClockWithCurrentAfterReleaseInstant() {
        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        Instant myReleaseInstant = myCurrentInstant.minusSeconds(100L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(myCurrentInstant);
        return myReleaseInstant;
    }

    @Test
    @Transactional
    public void createSimpleDeviceMessageTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("createSimpleDeviceMessageTest", "createSimpleDeviceMessageTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();

        Device reloadedDevice = getReloadedDevice(device);

        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        assertThat(messages).hasSize(1);
        DeviceMessage<Device> deviceMessage1 = messages.get(0);
        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorClose);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.WAITING);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_RELEASE_DATE_IS_REQUIRED + "}")
    public void createWithoutReleaseDateTest() {
        Device device = createSimpleDeviceWithName("createWithoutReleaseDateTest", "createWithoutReleaseDateTest");

        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(myCurrentInstant);

        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(null).add();
    }

    @Test
    @Transactional
    public void updateWithReleaseDateInWaitingTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("updateWithReleaseDateInWaitingTest", "updateWithReleaseDateInWaitingTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> deviceMessage = messages.get(0);
        Instant newReleaseDate = myReleaseInstant.plusSeconds(1000L);
        deviceMessage.setReleaseDate(newReleaseDate);
        deviceMessage.save();

        Device finalReloadedDevice = getReloadedDevice(device);
        DeviceMessage<Device> deviceMessage1 = finalReloadedDevice.getMessages().get(0);

        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorClose);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.WAITING);
        assertThat(deviceMessage1.getReleaseDate()).isEqualTo(newReleaseDate);
    }

    @Test
    @Transactional
    public void revokeWithStatusWaitingTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusWaitingTest", "revokeWithStatusWaitingTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> deviceMessage = messages.get(0);
        deviceMessage.revoke();
        deviceMessage.save();

        Device finalReloadedDevice = getReloadedDevice(device);
        DeviceMessage<Device> deviceMessage1 = finalReloadedDevice.getMessages().get(0);

        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorClose);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.REVOKED);
    }

    @Test
    @Transactional
    public void updateWithReleaseDateInPendingTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("updateWithReleaseDateInPendingTest", "updateWithReleaseDateInPendingTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> deviceMessage = messages.get(0);
        Instant newReleaseDate = myReleaseInstant.plusSeconds(2L);
        deviceMessage.setReleaseDate(newReleaseDate);
        deviceMessage.save();

        Device finalReloadedDevice = getReloadedDevice(device);
        DeviceMessage<Device> deviceMessage1 = finalReloadedDevice.getMessages().get(0);

        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorClose);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.PENDING);
        assertThat(deviceMessage1.getReleaseDate()).isEqualTo(newReleaseDate);
    }

    @Test
    @Transactional
    public void revokeWithStatusPendingTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusWaitingTest", "revokeWithStatusWaitingTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> deviceMessage = messages.get(0);
        deviceMessage.revoke();
        deviceMessage.save();

        Device finalReloadedDevice = getReloadedDevice(device);
        DeviceMessage<Device> deviceMessage1 = finalReloadedDevice.getMessages().get(0);

        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorClose);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.REVOKED);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}")
    public void updateReleaseDateWithStatusConfirmedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();
        Instant updatedReleaseDate = myReleaseInstant.plusSeconds(132L);

        Device device = createSimpleDeviceWithName("updateReleaseDateWithStatusConfirmedTest", "updateReleaseDateWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.CONFIRMED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.setReleaseDate(updatedReleaseDate);
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}")
    public void revokeWithStatusConfirmedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusConfirmedTest", "revokeWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.CONFIRMED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.revoke();
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}")
    public void updateReleaseDateWithStatusRevokedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();
        Instant updatedReleaseDate = myReleaseInstant.plusSeconds(132L);

        Device device = createSimpleDeviceWithName("updateReleaseDateWithStatusCanceledTest", "updateReleaseDateWithStatusCanceledTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.REVOKED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.setReleaseDate(updatedReleaseDate);
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}")
    public void revokeWithStatusRevokedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusConfirmedTest", "revokeWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.REVOKED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.revoke();
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}")
    public void updateReleaseDateWithStatusFailedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();
        Instant updatedReleaseDate = myReleaseInstant.plusSeconds(132L);

        Device device = createSimpleDeviceWithName("updateReleaseDateWithStatusFailedTest", "updateReleaseDateWithStatusFailedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.FAILED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.setReleaseDate(updatedReleaseDate);
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}")
    public void revokeWithStatusFailedTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusConfirmedTest", "revokeWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.FAILED);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.revoke();
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}")
    public void updateReleaseDateWithStatusIndoubtTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();
        Instant updatedReleaseDate = myReleaseInstant.plusSeconds(132L);

        Device device = createSimpleDeviceWithName("updateReleaseDateWithStatusIndoubtTest", "updateReleaseDateWithStatusIndoubtTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.INDOUBT);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.setReleaseDate(updatedReleaseDate);
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}")
    public void revokeWithStatusIndoubtTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusConfirmedTest", "revokeWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.INDOUBT);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.revoke();
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}")
    public void updateReleaseDateWithStatusSentTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();
        Instant updatedReleaseDate = myReleaseInstant.plusSeconds(132L);

        Device device = createSimpleDeviceWithName("updateReleaseDateWithStatusSentTest", "updateReleaseDateWithStatusSentTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.SENT);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.setReleaseDate(updatedReleaseDate);
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}")
    public void revokeWithStatusSentTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("revokeWithStatusConfirmedTest", "revokeWithStatusConfirmedTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
        ((ServerDeviceMessage) deviceMessage).moveTo(DeviceMessageStatus.SENT);
        deviceMessage.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> reloadedDeviceMessage = messages.get(0);
        reloadedDeviceMessage.revoke();
        reloadedDeviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_USER_NOT_ALLOWED + "}")
    public void updateReleaseDateWithAUserWhichHasNotGotTheCorrectPrivilegeTest() {
        Instant initialReleaseDate = initializeClockWithCurrentAfterReleaseInstant();

        Device device = createSimpleDeviceWithName("updateReleaseDateWithAUserWhichHasNotGotTheCorrectPrivilegeTest", "updateWithReleaseDateInWaitingTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(initialReleaseDate).add();

        createAndSetPrincipleForUserWithLimitedPrivileges();

        Device reloadedDevice = getReloadedDevice(device);
        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        DeviceMessage<Device> deviceMessage = messages.get(0);
        deviceMessage.setReleaseDate(initialReleaseDate.plusSeconds(1230L));
        deviceMessage.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED + "}")
    public void createWithIncorrectDeviceMessageIdTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("createWithIncorrectDeviceMessageIdTest", "createWithIncorrectDeviceMessageIdTest");
        DeviceMessageId activateFirewall = DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL;
        device.newDeviceMessage(activateFirewall).setReleaseDate(myReleaseInstant).add();
    }

    @Test
    @Transactional
    public void simpleTestWithADeviceMessageAttribute() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("simpleTestWithADeviceMessageAttribute", "simpleTestWithADeviceMessageAttribute");
        DeviceMessageId contactorOpenWithOutput = DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT;
        BigDecimal value = BigDecimal.valueOf(1);

        device.newDeviceMessage(contactorOpenWithOutput)
                .setReleaseDate(myReleaseInstant)
                .addProperty(DeviceMessageConstants.digitalOutputAttributeName, value)
                .add();

        Device reloadedDevice = getReloadedDevice(device);

        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        assertThat(messages).hasSize(1);
        DeviceMessage<Device> deviceMessage1 = messages.get(0);
        assertThat(deviceMessage1.getDeviceMessageId()).isEqualTo(contactorOpenWithOutput);
        assertThat(deviceMessage1.getDevice().getId()).isEqualTo(device.getId());
        assertThat(deviceMessage1.getStatus()).isEqualTo(DeviceMessageStatus.WAITING);
        assertThat(deviceMessage1.getAttributes()).hasSize(1);
        List<DeviceMessageAttribute> attributes = deviceMessage1.getAttributes();
        assertThat(attributes.get(0).getName()).isEqualTo(DeviceMessageConstants.digitalOutputAttributeName);
        assertThat(attributes.get(0).getValue()).isEqualTo(value);
    }

    @Test
    @Transactional
    public void deviceMessageWithMultipleAttributesTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("simpleTestWithADeviceMessageAttribute", "simpleTestWithADeviceMessageAttribute");
        DeviceMessageId contactorClose = DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS;
        String displayMessageAttributeName = "DisplayMessageAttributeName";
        BigDecimal displayMessageTimeDurationAttributeName = BigDecimal.valueOf(123);
        Date displayMessageActivationDate = Date.from(myReleaseInstant);

        device.newDeviceMessage(contactorClose)
                .setReleaseDate(myReleaseInstant)
                .addProperty(DeviceMessageConstants.DisplayMessageAttributeName, displayMessageAttributeName)
                .addProperty(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, displayMessageTimeDurationAttributeName)
                .addProperty(DeviceMessageConstants.DisplayMessageActivationDate, displayMessageActivationDate)
                .add();

        Device reloadedDevice = getReloadedDevice(device);

        List<DeviceMessage<Device>> messages = reloadedDevice.getMessages();
        assertThat(messages).hasSize(1);
        DeviceMessage<Device> deviceMessage1 = messages.get(0);
        assertThat(deviceMessage1.getAttributes()).hasSize(3);
        List<DeviceMessageAttribute> attributes = deviceMessage1.getAttributes();

        Optional<DeviceMessageAttribute> deviceMessageAttributeOptional1 =
                attributes.stream()
                        .filter(attribute -> attribute.getName().equals(DeviceMessageConstants.DisplayMessageAttributeName))
                        .findFirst();
        assertThat(deviceMessageAttributeOptional1.isPresent()).isTrue();
        assertThat(deviceMessageAttributeOptional1.get().getValue()).isEqualTo(displayMessageAttributeName);

        Optional<DeviceMessageAttribute> deviceMessageAttributeOptional2 =
                attributes.stream()
                        .filter(attribute -> attribute.getName().equals(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName))
                        .findFirst();
        assertThat(deviceMessageAttributeOptional2.isPresent()).isTrue();
        assertThat(deviceMessageAttributeOptional2.get().getValue()).isEqualTo(displayMessageTimeDurationAttributeName);

        Optional<DeviceMessageAttribute> deviceMessageAttributeOptional3 =
                attributes.stream()
                        .filter(attribute -> attribute.getName().equals(DeviceMessageConstants.DisplayMessageActivationDate))
                        .findFirst();
        assertThat(deviceMessageAttributeOptional3.isPresent()).isTrue();
        assertThat(deviceMessageAttributeOptional3.get().getValue()).isEqualTo(displayMessageActivationDate);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ATTRIBUTE_IS_REQUIRED + "}")
    public void emptyDeviceMessageAttributeTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("emptyDeviceMessageAttributeTest", "emptyDeviceMessageAttributeTest");
        DeviceMessageId contactorOpenWithOutput = DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT;

        device.newDeviceMessage(contactorOpenWithOutput)
                .setReleaseDate(myReleaseInstant)
                .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ATTRIBUTE_NOT_IN_SPEC + "}")
    public void deviceMessageAttributeNotDefinedInSpecTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("deviceMessageAttributeNotDefinedInSpecTest", "deviceMessageAttributeNotDefinedInSpecTest");
        DeviceMessageId contactorOpenWithOutput = DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT;
        BigDecimal value = BigDecimal.valueOf(1);

        device.newDeviceMessage(contactorOpenWithOutput)
                .setReleaseDate(myReleaseInstant)
                .addProperty("NameOfAttributeWhichIsNotDefinedInSpec", "Blablabla")
                .addProperty(DeviceMessageConstants.digitalOutputAttributeName, value)
                .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "The value \"This should have been a BigDecimal\" is not compatible with the attribute specification ContactorDeviceMessage.digitalOutput.", property = "properties." + DeviceMessageConstants.digitalOutputAttributeName)
    public void invalidDeviceMessageAttributeTest() {
        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("invalidDeviceMessageAttributeTest", "invalidDeviceMessageAttributeTest");
        DeviceMessageId contactorOpenWithOutput = DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT;
        String value = "This should have been a BigDecimal";

        device.newDeviceMessage(contactorOpenWithOutput)
                .setReleaseDate(myReleaseInstant)
                .addProperty(DeviceMessageConstants.digitalOutputAttributeName, value)
                .add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG + "}")
    public void createWithMessageWhichIsNotAllowedByTheDeviceConfigurationTest() {
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("Config2");
        DeviceConfiguration config2 = deviceConfigurationBuilder.add();
        config2.getDeviceMessageEnablements().stream().forEach(deviceMessageEnablement -> config2.removeDeviceMessageEnablement(deviceMessageEnablement.getDeviceMessageId()));
        config2.activate();

        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = inMemoryPersistence.getDeviceService().newDevice(config2, "Name", "mrid");
        device.save();
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_USER_NOT_ALLOWED + "}")
    public void createWithUserDoesNotHaveTheCorrectPrivilegeTest() {
        createAndSetPrincipleForUserWithLimitedPrivileges();

        Instant myReleaseInstant = initializeClockWithCurrentBeforeReleaseInstant();

        Device device = createSimpleDeviceWithName("userDoesNotHaveTheCorrectPrivilegeTest", "userDoesNotHaveTheCorrectPrivilegeTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
    }

    private void createAndSetPrincipleForUserWithLimitedPrivileges() {
        Group group = inMemoryPersistence.getUserService().createGroup("MyPrimitiveGroup", "Useless group");
        group.grant("MDC", Privileges.EXECUTE_DEVICE_MESSAGE_4);
        group.save();
        User primitiveUser = inMemoryPersistence.getUserService().createUser("PrimitiveUser", "User with incorrect privilege");
        primitiveUser.join(group);
        primitiveUser.save();
        inMemoryPersistence.getThreadPrincipalService().set(primitiveUser);
    }

    public static class MessageTestDeviceProtocol implements DeviceProtocol {

        private EnumSet<DeviceMessageId> deviceMessageIds =
                EnumSet.of(DeviceMessageId.CONTACTOR_CLOSE,
                        DeviceMessageId.CONTACTOR_OPEN,
                        DeviceMessageId.CONTACTOR_ARM,
                        DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT,
                        DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
                        DeviceMessageId.DISPLAY_SET_MESSAGE_WITH_OPTIONS);

        public MessageTestDeviceProtocol() {
        }

        @Override
        public List<PropertySpec> getSecurityPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public String getSecurityRelationTypeName() {
            return null;
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
        public PropertySpec getSecurityPropertySpec(String name) {
            return null;
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
        public PropertySpec getPropertySpec(String name) {
            return null;
        }

        @Override
        public CollectedFirmwareVersion getFirmwareVersions() {
            return null;
        }
    }
}