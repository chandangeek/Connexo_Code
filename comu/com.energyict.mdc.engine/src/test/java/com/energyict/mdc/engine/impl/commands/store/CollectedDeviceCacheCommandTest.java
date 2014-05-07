package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdw.shadow.DeviceCacheShadow;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link CollectedDeviceCacheCommand} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/09/12
 * Time: 15:12
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedDeviceCacheCommandTest {

    private final int DEVICE_ID = 654;

    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;

    @Test
    public void updateWithoutChangeTest() throws BusinessException, SQLException {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(mock(DeviceIdentifier.class));
        DeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        updatedDeviceCache.setDeviceCache(protocolCache);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, issueService, clock);
        deviceCacheCommand.logExecutionWith(this.executionLogger);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, times(0)).createOrUpdateDeviceCache(anyInt(), any(DeviceCacheShadow.class));
    }

    @Test
    public void updateWithChangeTest() throws BusinessException, SQLException {
        final String newDescription = "laaaalallalallallllaaal";
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(getMockedDeviceIdentifier());
        SimpleDeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        protocolCache.updateChangedState(true);
        protocolCache.updateDescription(newDescription);
        updatedDeviceCache.setDeviceCache(protocolCache);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, issueService, clock);
        deviceCacheCommand.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createOrUpdateDeviceCache(anyInt(), any(DeviceCacheShadow.class));
    }

    @Test
    public void testToJournalMessageDescription() {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        CollectedDeviceCacheCommand command = new CollectedDeviceCacheCommand(updatedDeviceCache, issueService, clock);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CollectedDeviceCacheCommand.class.getSimpleName() + " {deviceIdentifier: id 654}");
    }

    private DeviceIdentifier getMockedDeviceIdentifier(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        BaseDevice device = mock(BaseDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(deviceIdentifier.findDevice()).thenReturn(device);
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
