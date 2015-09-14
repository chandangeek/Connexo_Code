package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.elster.jupiter.events.EventService;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link CollectedDeviceCacheCommand} component.
 * <p>
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
    private DeviceService deviceService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EngineService engineService;

    @Test
    public void updateWithoutChangeTest() throws BusinessException, SQLException {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(mock(DeviceIdentifier.class));
        DeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());
        deviceCacheCommand.logExecutionWith(this.executionLogger);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(this.engineService, times(0)).findDeviceCacheByDevice(any(Device.class));
    }

    @Test
    public void updateWithChangeTest() throws BusinessException, SQLException {
        final String newDescription = "laaaalallalallallllaaal";
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(getMockedDeviceIdentifier());
        SimpleDeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        protocolCache.updateChangedState(true);
        protocolCache.updateDescription(newDescription);
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());
        deviceCacheCommand.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        DeviceCache existingCache = mock(DeviceCache.class);
        when(this.engineService.findDeviceCacheByDevice(this.device)).thenReturn(Optional.of(existingCache));
        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(existingCache).setCacheObject(protocolCache);
        verify(existingCache).update();
    }

    @Test
    public void testToJournalMessageDescription() {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID, deviceService);
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        CollectedDeviceCacheCommand command = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{deviceIdentifier: id 654}");
    }

    private DeviceIdentifier getMockedDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    private class SimpleDeviceProtocolCache implements Serializable, DeviceProtocolCache {

        private boolean dirty = false;
        private String description = "NoDescription";

        @Override
        public boolean isDirty() {
            return dirty;
        }

        @Override
        public void markClean() {
            this.dirty = false;
        }

        @Override
        public void markDirty() {
            this.dirty = true;
        }

        protected void updateChangedState(final boolean changedState) {
            this.dirty = changedState;
        }

        protected void updateDescription(final String newDescription) {
            this.description = newDescription;
        }

        protected String getDescription() {
            return this.description;
        }
    }

    private class EngineServiceOnly implements DeviceCommand.ServiceProvider {
        @Override
        public EventService eventService() {
            return null;
        }

        @Override
        public IssueService issueService() {
            return null;
        }

        @Override
        public Clock clock() {
            return null;
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return null;
        }

        @Override
        public EngineService engineService() {
            return engineService;
        }

        @Override
        public NlsService nlsService() {
            return null;
        }
    }

}
