package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageImplTest extends PersistenceIntegrationTest{

    private User testUser;

    @Before
    public void initBefore() {
        testUser = inMemoryPersistence.getUserService().createUser("TestUser", "This user is just to satisfy the foreign key ...");
        testUser.save();
        inMemoryPersistence.getThreadPrincipalService().set(testUser);
    }

    private Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    private Instant initializeClockWithCurrentAndReleaseInstant() {
        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        Instant myReleaseInstant = myCurrentInstant.plusSeconds(100L);
        when(clock.instant()).thenReturn(myCurrentInstant);
        return myReleaseInstant;
    }

    @Test
    @Transactional
    public void createSimpleDeviceMessageTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAndReleaseInstant();

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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.USER_IS_REQUIRED + "}")
    public void createWithoutUserTest() {
        Instant releaseInstant = initializeClockWithCurrentAndReleaseInstant();

        Device device = createSimpleDeviceWithName("createWithoutUserTest", "createWithoutUserTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;

        inMemoryPersistence.getThreadPrincipalService().set(null);
        device.newDeviceMessage(contactorClose).setReleaseDate(releaseInstant).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CREATE_DATE_IS_REQUIRED + "}")
    public void createWithoutCreateDateTest() {
        Device device = createSimpleDeviceWithName("createWithoutCreateDateTest", "createWithoutCreateDateTest");

        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        Instant myReleaseInstant = myCurrentInstant.plusSeconds(100L);
        when(clock.instant()).thenReturn(null);

        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.RELEASE_DATE_IS_REQUIRED + "}")
    public void createWithoutReleaseDateTest() {
        Device device = createSimpleDeviceWithName("createWithoutReleaseDateTest", "createWithoutReleaseDateTest");

        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        when(clock.instant()).thenReturn(myCurrentInstant);

        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        device.newDeviceMessage(contactorClose).setReleaseDate(null).add();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED + "}")
    public void createWithIncorrectDeviceMessageIdTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAndReleaseInstant();

        Device device = createSimpleDeviceWithName("createWithIncorrectDeviceMessageIdTest", "createWithIncorrectDeviceMessageIdTest");
        DeviceMessageId contactorClose = DeviceMessageId.FIRMWARE_UPGRADE_URL;
        device.newDeviceMessage(contactorClose).setReleaseDate(myReleaseInstant).add();
    }

    @Test
    @Transactional
    public void simpleTestWithADeviceMessageAttribute() {
        Instant myReleaseInstant = initializeClockWithCurrentAndReleaseInstant();

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
        List<DeviceMessageAttribute<?>> attributes = deviceMessage1.getAttributes();
        assertThat(attributes.get(0).getName()).isEqualTo(DeviceMessageConstants.digitalOutputAttributeName);
        assertThat(attributes.get(0).getValue()).isEqualTo(value);
    }

    @Test
    @Transactional
    public void deviceMessageWithMultipleAttributesTest() {
        Instant myReleaseInstant = initializeClockWithCurrentAndReleaseInstant();

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
        List<DeviceMessageAttribute<?>> attributes = deviceMessage1.getAttributes();

        Optional<DeviceMessageAttribute<?>> deviceMessageAttributeOptional1 =
                attributes.stream()
                        .filter(attribute -> attribute.getName().equals(DeviceMessageConstants.DisplayMessageAttributeName))
                        .findFirst();
        assertThat(deviceMessageAttributeOptional1.isPresent()).isTrue();
        assertThat(deviceMessageAttributeOptional1.get().getValue()).isEqualTo(displayMessageAttributeName);

        Optional<DeviceMessageAttribute<?>> deviceMessageAttributeOptional2 =
                attributes.stream()
                        .filter(attribute -> attribute.getName().equals(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName))
                        .findFirst();
        assertThat(deviceMessageAttributeOptional2.isPresent()).isTrue();
        assertThat(deviceMessageAttributeOptional2.get().getValue()).isEqualTo(displayMessageTimeDurationAttributeName);


        //todo validate the other attributes

    }
}