/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void updateWithoutChangeTest() throws SQLException {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(mock(DeviceIdentifier.class));
        DeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());
        deviceCacheCommand.logExecutionWith(this.executionLogger);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(this.engineService, times(0)).findDeviceCacheByDeviceIdentifier(any(DeviceIdentifier.class));
    }

    @Test
    public void updateWithChangeTest() throws SQLException {
        final String newDescription = "laaaalallalallallllaaal";
        DeviceIdentifier deviceIdentifier = getMockedDeviceIdentifier();
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        SimpleDeviceProtocolCache protocolCache = new SimpleDeviceProtocolCache();
        protocolCache.updateChangedState(true);
        protocolCache.updateDescription(newDescription);
        updatedDeviceCache.setCollectedDeviceCache(protocolCache);
        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());
        deviceCacheCommand.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        DeviceCache existingCache = mock(DeviceCache.class);
        when(this.engineService.findDeviceCacheByDeviceIdentifier(deviceIdentifier)).thenReturn(Optional.of(existingCache));

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createOrUpdateDeviceCache(eq(deviceIdentifier), any(DeviceProtocolCacheXmlWrapper.class));
    }

    @Test
    public void testToJournalMessageDescription() {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(deviceIdentifier);
        CollectedDeviceCacheCommand command = new CollectedDeviceCacheCommand(updatedDeviceCache, null, new EngineServiceOnly());

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("Collected device cache {deviceIdentifier: id 654}");
    }

    private DeviceIdentifier getMockedDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        return deviceIdentifier;
    }

    private class SimpleDeviceProtocolCache implements Serializable, DeviceProtocolCache {

        private boolean dirty = false;
        private String description = "NoDescription";

        @Override
        public boolean contentChanged() {
            return dirty;
        }

        @Override
        public void setContentChanged(boolean changed) {
            this.dirty = changed;
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
            return Clock.systemDefaultZone();
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

        @Override
        public EventPublisher eventPublisher() {
            return null;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return null;
        }

        @Override
        public TransactionService transactionService() {
            return null;
        }
    }

}
