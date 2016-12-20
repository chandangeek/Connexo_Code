package com.energyict.mdc.engine.commands.store;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceCacheCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceTopologyDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedFirmwareVersionDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedMessageListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CollectedRegisterListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.StoreConfigurationUserFile;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceIpAddress;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceMessage;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceProtocolProperty;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedDeviceCacheEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedDeviceTopologyEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedFirmwareVersionEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLoadProfileEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLogBookEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedMessageListEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedRegisterListEvent;
import com.energyict.mdc.engine.impl.events.datastorage.MeterDataStorageEvent;
import com.energyict.mdc.engine.impl.events.datastorage.NoopCollectedDataEvent;
import com.energyict.mdc.engine.impl.events.datastorage.StoreConfigurationEvent;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceIpAddressEvent;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceMessageEvent;
import com.energyict.mdc.engine.impl.events.datastorage.UpdateDeviceProtocolPropertyEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageAcknowledgement;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        verify(publisher).publish(isA(CollectedDeviceCacheEvent.class));
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
        verify(publisher).publish(isA(CollectedDeviceTopologyEvent.class));
    }

    @Test
    public void ExecutingCollectedFirmwareVersionDeviceCommandPublishesEvent(){
        CollectedFirmwareVersion firmwareVersion = mock(CollectedFirmwareVersion.class);

        CollectedFirmwareVersionDeviceCommand firmwareVersionCommand = new CollectedFirmwareVersionDeviceCommand(serviceProvider, firmwareVersion, comTaskExecution );

        // Business method
        firmwareVersionCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedFirmwareVersionEvent.class));
        verify(publisher).publish(isA(CollectedFirmwareVersionEvent.class));
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
        verify(publisher).publish(isA(CollectedLoadProfileEvent.class));
    }

    @Test
    public void ExecutingCollectedLogBookDeviceCommandPublishesEvent(){
        LogBook logbook = mock(LogBook.class);
        LogBookIdentifier logBookIdentifier = mock(LogBookIdentifier.class);
        OfflineLogBook offlineLogBook = mock(OfflineLogBook.class);

        DeviceLogBook deviceLogBook = mock(DeviceLogBook.class);
        when(deviceLogBook.getLogBookIdentifier()).thenReturn(logBookIdentifier);
        when(logBookIdentifier.getLogBook()).thenReturn(logbook);

        MeterDataStoreCommand meterDataStoreCommand = mock(MeterDataStoreCommand.class);
        when(meterDataStoreCommand.getServiceProvider()).thenReturn(serviceProvider);
        when(comServerDAO.findOfflineLogBook(logBookIdentifier)).thenReturn(Optional.of(offlineLogBook));

        CollectedLogBookDeviceCommand logbookCommand = new CollectedLogBookDeviceCommand(deviceLogBook, comTaskExecution, meterDataStoreCommand);
        logbookCommand.logExecutionWith(executionLogger);

        // Business method
        logbookCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedLogBookEvent.class));
        verify(publisher).publish(isA(CollectedLogBookEvent.class));
    }

    @Test
    public void ExecutingCollectedMessageListDeviceCommandPublishesEvent(){
        DeviceProtocolMessageList messageList = mock(DeviceProtocolMessageList.class);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);

        MeterDataStoreCommand meterDataStoreCommand = mock(MeterDataStoreCommand.class);

        CollectedMessageListDeviceCommand messageListCommand = new CollectedMessageListDeviceCommand(
                messageList,
                Collections.singletonList(offlineDeviceMessage),
                comTaskExecution,
                meterDataStoreCommand,
                serviceProvider);
        messageListCommand.logExecutionWith(executionLogger);

        // Business method
        messageListCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedMessageListEvent.class));
        verify(publisher).publish(isA(CollectedMessageListEvent.class));
    }

    @Test
    public void ExecutingCollectedNoLogBooksDeviceCommandDeviceCommandPublishesEvent(){
        NoLogBooksForDevice noLogBooksForDevice = mock(NoLogBooksForDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        LogBookIdentifier logBookIdentifier = mock(LogBookIdentifier.class);

        when(noLogBooksForDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(noLogBooksForDevice.getLogBookIdentifier()).thenReturn(logBookIdentifier);

        CreateNoLogBooksForDeviceEvent command = new CreateNoLogBooksForDeviceEvent(noLogBooksForDevice,comTaskExecution,serviceProvider);
        command.logExecutionWith(executionLogger);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedNoLogBooksForDeviceEvent.class));
        verify(publisher).publish(isA(CollectedNoLogBooksForDeviceEvent.class));
    }

    @Test
    public void ExecutingCollectedRegisterListDeviceCommandDeviceCommandPublishesEvent(){
        CollectedRegisterList registerList = mock(CollectedRegisterList.class);
        MeterDataStoreCommand meterDataStoreCommand = mock(MeterDataStoreCommand.class);

        CollectedRegisterListDeviceCommand registerListCommand = new CollectedRegisterListDeviceCommand(
                registerList,
                comTaskExecution,
                meterDataStoreCommand,
                serviceProvider);
        registerListCommand.logExecutionWith(executionLogger);

        // Business method
        registerListCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(CollectedRegisterListEvent.class));
        verify(publisher).publish(isA(CollectedRegisterListEvent.class));
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
        verify(publisher).publish(isA(MeterDataStorageEvent.class));
    }

    @Test
    public void ExecutingNoopDeviceCommandPublishesEvent(){
        NoopDeviceCommand noopCommand = new NoopDeviceCommand();

        // Business method
        noopCommand.execute(comServerDAO);

        // Asserts
        verify(publisher, times(0)).publish(any(NoopCollectedDataEvent.class));
    }

    @Test
    public void ExecutingStoreConfigurationUserFilePublishesEvent(){
        DeviceIdentifier identifier = mock(DeviceIdentifier.class);
        when(identifier.toString()).thenReturn("My device identifier");

        DeviceUserFileConfigurationInformation userFileConfigurationInformation = mock(DeviceUserFileConfigurationInformation.class);
        when(userFileConfigurationInformation.getDeviceIdentifier()).thenReturn(identifier);

        StoreConfigurationUserFile storeConfigurationUserFile = new StoreConfigurationUserFile( userFileConfigurationInformation,comTaskExecution, serviceProvider);

        // Business method
        storeConfigurationUserFile.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(StoreConfigurationEvent.class));
                verify(publisher).publish(isA(StoreConfigurationEvent.class));
    }

    @Test
    public void ExecutingUpdateDeviceIpAddressDeviceCommandPublishesEvent(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        DeviceIpAddress deviceIpAddress = mock(DeviceIpAddress.class);
        when(deviceIpAddress.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        UpdateDeviceIpAddress updateDeviceIpAddress = new UpdateDeviceIpAddress(deviceIpAddress, comTaskExecution, serviceProvider);
        updateDeviceIpAddress.logExecutionWith(executionLogger);

        // Business method
        updateDeviceIpAddress.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(UpdateDeviceIpAddressEvent.class));
        verify(publisher).publish(isA(UpdateDeviceIpAddressEvent.class));
    }

    @Test
    public void ExecutingUpdateDeviceMessageDeviceCommandPublishesEvent(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        when(messageIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        OfflineDeviceMessage offlineDeviceMessage = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(offlineDeviceMessage.getIdentifier()).thenReturn(messageIdentifier);
        DeviceProtocolMessageAcknowledgement messageAcknowledgement = mock(DeviceProtocolMessageAcknowledgement.class);
        when(messageAcknowledgement.getMessageIdentifier()).thenReturn(messageIdentifier);
        when(messageAcknowledgement.getDeviceMessageStatus()).thenReturn(mock(DeviceMessageStatus.class));

        when(comServerDAO.findOfflineDeviceMessage(messageIdentifier)).thenReturn(Optional.of(offlineDeviceMessage));

        UpdateDeviceMessage updateDeviceMessage = new UpdateDeviceMessage(messageAcknowledgement, comTaskExecution, serviceProvider);
        updateDeviceMessage.logExecutionWith(executionLogger);

        // Business method
        updateDeviceMessage.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(UpdateDeviceMessageEvent.class));
        verify(publisher).publish(isA(UpdateDeviceMessageEvent.class));
    }

    @Test
    public void ExecutingUpdateDeviceProtocolPropertyDeviceCommandPublishesEvent(){
        DeviceProtocolProperty deviceProtocolProperty = mock(DeviceProtocolProperty.class);
        when(deviceProtocolProperty.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));
        when(deviceProtocolProperty.getPropertySpec()).thenReturn(mock(PropertySpec.class));
        when(deviceProtocolProperty.getPropertyValue()).thenReturn("aaaa");

        Optional<OfflineDevice> device = Optional.of(mock(OfflineDevice.class));

        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any())).thenReturn(device);

        UpdateDeviceProtocolProperty updateDeviceProtocolProperty = new UpdateDeviceProtocolProperty(deviceProtocolProperty, comTaskExecution, serviceProvider);
        updateDeviceProtocolProperty.logExecutionWith(executionLogger);

        // Business method
        updateDeviceProtocolProperty.execute(comServerDAO);

        // Asserts
        verify(publisher, times(1)).publish(any(UpdateDeviceProtocolPropertyEvent.class));
        verify(publisher).publish(isA(UpdateDeviceProtocolPropertyEvent.class));
    }


}
