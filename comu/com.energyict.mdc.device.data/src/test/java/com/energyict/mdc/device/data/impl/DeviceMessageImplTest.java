package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageImplTest extends PersistenceIntegrationTest{

    @Before
    public void initBefore() {
        User mockedUser = inMemoryPersistence.getMockedUser();
        when(mockedUser.hasPrivilege(anyString())).thenReturn(true);
    }

    private Device createSimpleDeviceWithName(String name, String mRID){
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, name, mRID);
        device.save();
        return device;
    }

    @Test
    @Transactional
    public void createSimpleDeviceMessageTest() {
        Device device = createSimpleDeviceWithName("createSimpleDeviceMessageTest", "createSimpleDeviceMessageTest");
        DeviceMessageId contactorClose = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage<Device> deviceMessage = device.newDeviceMessage(contactorClose).add();

        assertThat(deviceMessage).isNotNull();
        assertThat(deviceMessage.getDeviceMessageId()).isEqualTo(contactorClose);
    }

}