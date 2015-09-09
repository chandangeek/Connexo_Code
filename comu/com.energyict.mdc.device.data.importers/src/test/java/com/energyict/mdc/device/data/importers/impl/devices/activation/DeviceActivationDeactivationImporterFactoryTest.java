package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceActivationDeactivationImporterFactoryTest {

    @Mock
    private Thesaurus thesaurus;

    private DeviceDataImporterContext context;
    @Mock
    private DeviceService deviceService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    private FiniteStateMachineService finiteStateMachineService;
    @Mock
    private Logger logger;
    @Mock
    private Clock clock;

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceService, topologyService, meteringService, deviceLifeCycleService, finiteStateMachineService);
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        when(thesaurus.getStringBeyondComponent(anyString(), anyString()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setTopologyService(topologyService);
        context.setMeteringService(meteringService);
        context.setDeviceLifeCycleService(deviceLifeCycleService);
        context.setFiniteStateMachineService(finiteStateMachineService);
        context.setClock(clock);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createDeviceActivationDeactivationImporter() {
        DeviceActivationDeactivationImportFactory factory = new DeviceActivationDeactivationImportFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        return factory.createImporter(properties);
    }

    @Test
    public void testActivateTransitionIsNotSupportedByImporter() {
        String csv = "Device MRID;Transition date;Activate\n" +
                "VPB0001;01/08/2015 00:30;true";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getLifecycleDates().getInstalledDate()).thenReturn(Optional.empty());
        when(device.forValidation().getLastChecked()).thenReturn(Optional.empty());

        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));

        createDeviceActivationDeactivationImporter().process(importOccurrence);

        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(contains(thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER)
                .format(2, DefaultState.ACTIVE.getKey(), DefaultState.IN_STOCK.getKey(), DefaultState.INACTIVE.getKey())));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeactivateTransitionIsNotSupportedByImporter() {
        String csv = "Device MRID;Transition date;Activate\n" +
                "VPB0001;01/08/2015 00:30;false";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.COMMISSIONING.getKey());
        when(device.getLifecycleDates().getInstalledDate()).thenReturn(Optional.empty());
        when(device.forValidation().getLastChecked()).thenReturn(Optional.empty());

        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));

        createDeviceActivationDeactivationImporter().process(importOccurrence);

        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(contains(thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER)
                .format(2, DefaultState.INACTIVE.getKey(), DefaultState.COMMISSIONING.getKey(), DefaultState.ACTIVE.getKey())));
        verify(logger, never()).severe(Matchers.anyString());
    }
}
