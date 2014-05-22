package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link CollectedDeviceCacheCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/09/12
 * Time: 15:12
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedDeviceCacheCommandTest {

    private final long DEVICE_ID = 654;

    @Mock
    private Device device;
    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EngineService engineService;
    @Mock
    private ServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        when(this.serviceProvider.engineService()).thenReturn(this.engineService);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @Test
    public void updateWithoutChangeTest() throws BusinessException, SQLException {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(mock(DeviceIdentifier.class));
        DeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache);
        deviceCacheCommand.logExecutionWith(this.executionLogger);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(this.engineService, times(0)).findDeviceCacheByDeviceId(any(Device.class));
    }

    @Test
    public void updateWithChangeTest() throws BusinessException, SQLException {
        final String newDescription = "laaaalallalallallllaaal";
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(getMockedDeviceIdentifier());
        SimpleDeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        protocolCache.updateChangedState(true);
        protocolCache.updateDescription(newDescription);
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache);
        deviceCacheCommand.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(this.engineService.findDeviceCacheByDeviceId(this.device)).thenReturn(null);
        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(this.engineService).findDeviceCacheByDeviceId(this.device);
        verify(this.engineService).newDeviceCache(this.device, protocolCache);
    }

    @Test
    public void testToJournalMessageDescription() {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceDataService);
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        CollectedDeviceCacheCommand command = new CollectedDeviceCacheCommand(updatedDeviceCache);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).isEqualTo(CollectedDeviceCacheCommand.class.getSimpleName() + " {deviceIdentifier: id 654}");
    }

    private DeviceIdentifier getMockedDeviceIdentifier(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    private class SimpleDeviceProtocolCache implements Serializable, DeviceProtocolCache {

        private boolean changed = false;
        private String description = "NoDescription";

        @Override
        public boolean contentChanged() {
            return changed;
        }

        protected void updateChangedState(final boolean changedState) {
            this.changed = changedState;
        }

        protected void updateDescription(final String newDescription){
            this.description = newDescription;
        }

        protected String getDescription(){
            return this.description;
        }
    }

}
