package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 09/07/15
 * Time: 16:01
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageEnablementImplTest {

    @Mock
    private DeviceConfigurationImpl original;
    @Mock
    private DeviceConfiguration clone;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void cloneDeviceMessageEnablementTest() {
        DeviceMessageId deviceMessageId = DeviceMessageId.CLOCK_SET_TIME;
        DeviceMessageEnablement mockedDeviceMessageEnablement = mock(DeviceMessageEnablement.class);
        DeviceMessageEnablementBuilder deviceMessageEnablementBuilder = mock(DeviceMessageEnablementBuilder.class);
        when(clone.createDeviceMessageEnablement(deviceMessageId)).thenReturn(deviceMessageEnablementBuilder);
        when(deviceMessageEnablementBuilder.build()).thenReturn(mockedDeviceMessageEnablement);

        DeviceMessageEnablementImpl deviceMessageEnablement = new DeviceMessageEnablementImpl(dataModel, eventService, thesaurus);
        when(dataModel.getInstance(DeviceMessageEnablementImpl.class)).thenReturn(deviceMessageEnablement);
        DeviceMessageEnablement originalDeviceMessageEnablement = DeviceMessageEnablementImpl.from(dataModel, original, deviceMessageId);
        DeviceMessageUserAction userAction1 = DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1;
        DeviceMessageUserAction userAction2 = DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2;
        originalDeviceMessageEnablement.addDeviceMessageUserAction(userAction1);
        originalDeviceMessageEnablement.addDeviceMessageUserAction(userAction2);

        DeviceMessageEnablement clonedDeviceMessageEnablement = ((ServerDeviceMessageEnablement) originalDeviceMessageEnablement).cloneForDeviceConfig(clone);
        verify(deviceMessageEnablementBuilder, times(1)).addUserAction(userAction1);
        verify(deviceMessageEnablementBuilder, times(1)).addUserAction(userAction2);
        verify(clone).createDeviceMessageEnablement(deviceMessageId);
    }

}