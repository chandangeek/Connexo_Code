package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 09/07/15
 * Time: 16:01
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageEnablementImplTest extends DeviceTypeProvidingPersistenceTest {

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

    private DeviceConfiguration deviceConfiguration;

    @Test
    @Transactional
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

     @Test
     @Transactional
     public void testDeviceConfigurationCreation() {
         DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = this.deviceType.newConfiguration("DeviceConfiguration");
         deviceConfiguration = deviceConfigurationBuilder.add();

         DeviceConfiguration deviceConfiguration = getReloadedDeviceConfiguration();

         List<DeviceMessageEnablementImpl> deviceMessageEnablements = inMemoryPersistence.getDataModel().mapper(DeviceMessageEnablementImpl.class).find("deviceConfiguration", deviceConfiguration);
         assertThat(deviceMessageEnablements.stream().filter(enablement -> enablement.getDeviceMessageDbValue() == DeviceMessageId.CONTACTOR_CLOSE.dbValue()).count()).isEqualTo(1);
         assertThat(deviceMessageEnablements.stream().filter(enablement -> enablement.getDeviceMessageDbValue() == DeviceMessageId.CONTACTOR_OPEN.dbValue()).count()).isEqualTo(1);
         assertThat(deviceMessageEnablements.stream().filter(enablement -> enablement.getDeviceMessageDbValue() == DeviceMessageId.CONTACTOR_ARM.dbValue()).count()).isEqualTo(1);
     }

    private DeviceConfiguration getReloadedDeviceConfiguration(){
        return inMemoryPersistence.getDeviceConfigurationService()
                .findDeviceConfiguration(this.deviceConfiguration.getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload device configuration " + this.deviceConfiguration.getId()));
    }

}