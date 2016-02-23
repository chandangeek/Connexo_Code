package com.energyict.mdc.engine.commands.store;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.*;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.datastorage.*;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.offline.*;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandImplTest {

    @Mock
    DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    ComServerDAO comServerDAO;
    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    IssueService issueService;
    @Mock
    EventPublisher publisher;

    @Before
    public void initMocks(){
        when(serviceProvider.eventPublisher()).thenReturn(publisher);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(serviceProvider.issueService()).thenReturn(issueService);
    }

    @Test
    public void ExecutingCollectedDeviceCacheCommandPublishesEvent(){
        UpdatedDeviceCache deviceCache = mock(UpdatedDeviceCache.class);

        CollectedDeviceCacheCommand deviceCacheCommand = new CollectedDeviceCacheCommand(deviceCache, comTaskExecution, serviceProvider);

        // Business method
        deviceCacheCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedDeviceCacheEvent.class));
    }

    @Test
    public void ExecutingCollectedDeviceTopologyDeviceCommandPublishesEvent(){
        CollectedTopology deviceTopology = mock(CollectedTopology.class);
        when(deviceTopology.getTopologyAction()).thenReturn(TopologyAction.VERIFY);
        MeterDataStoreCommand meterDataStoreCommand = mock(MeterDataStoreCommand.class);
        Optional<OfflineDevice> device = Optional.of(mock(OfflineDevice.class));

        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class),any(DeviceOfflineFlags.class))).thenReturn(device);

        CollectedDeviceTopologyDeviceCommand topologyCommand = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, meterDataStoreCommand,serviceProvider );

        // Business method
        topologyCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedDeviceTopologyEvent.class));
    }

    @Test
    public void ExecutingCollectedFirmwareVersionDeviceCommandPublishesEvent(){
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);

        CollectedFirmwareVersionDeviceCommand firmwareVersionCommand = new CollectedFirmwareVersionDeviceCommand(serviceProvider, firmwareVersion, comTaskExecution );

        // Business method
        firmwareVersionCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedFirmwareVersionEvent.class));
    }

    @Test
    public void ExecutingCollectedLoadProfileDeviceCommandDeviceCommandPublishesEvent(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        LoadProfileIdentifier loadProfileIdentifier = mock(LoadProfileIdentifier.class);
        CollectedLoadProfile loadProfile = mock(CollectedLoadProfile.class);
        when(loadProfile.getLoadProfileIdentifier()).thenReturn(loadProfileIdentifier);
        when(loadProfileIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        MeterDataStoreCommand meterDataStoreCommand = mock(MeterDataStoreCommand.class);
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getObisCode()).thenReturn(mock(ObisCode.class));
        when(offlineLoadProfile.getLastReading()).thenReturn(Optional.of(Instant.now()));

        when(comServerDAO.findOfflineLoadProfile(loadProfileIdentifier)).thenReturn(Optional.of(offlineLoadProfile));

        CollectedLoadProfileDeviceCommand loadProfileCommand = new CollectedLoadProfileDeviceCommand(loadProfile, comTaskExecution, meterDataStoreCommand, serviceProvider);
        loadProfileCommand.logExecutionWith(executionLogger);

        // Business method
        loadProfileCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedLoadProfileEvent.class));
    }

    @Test
    public void ExecutingMeterDataStoreCommandPublishesEvent(){
        when(serviceProvider.eventPublisher()).thenReturn(publisher);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());

        MeterDataStoreCommandImpl deviceCommand = new MeterDataStoreCommandImpl(comTaskExecution, serviceProvider);

        // Business method
        deviceCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(MeterDataStorageEvent.class));
    }

}
