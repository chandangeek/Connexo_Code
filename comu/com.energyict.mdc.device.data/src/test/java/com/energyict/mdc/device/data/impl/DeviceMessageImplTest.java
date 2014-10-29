package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
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

    @Test
    @Transactional
    public void createSimpleDeviceMessageTest() {
        Instant myCurrentInstant = Instant.ofEpochSecond(123456789L);
        Instant myReleaseInstant = myCurrentInstant.plusSeconds(100L);

        when(clock.instant()).thenReturn(myCurrentInstant);

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

}