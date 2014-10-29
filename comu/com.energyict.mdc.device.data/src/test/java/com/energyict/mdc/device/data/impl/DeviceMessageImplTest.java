package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;

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
}