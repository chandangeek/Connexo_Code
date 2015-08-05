package com.energyict.mdc.device.data.importers.impl.devices.activation;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            for (TranslationKey translation : TranslationKeys.values()) {
                if (translation.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return translation.getDefaultFormat();
                }
            }
            return invocationOnMock.getArguments()[1];
        });
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

        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER.getTranslated(thesaurus, 2, DefaultState.ACTIVE.getKey(), DefaultState.IN_STOCK.getKey(), DefaultState.INACTIVE.getKey()));
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

        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER.getTranslated(thesaurus, 2, DefaultState.INACTIVE.getKey(), DefaultState.COMMISSIONING.getKey(), DefaultState.ACTIVE.getKey()));
        verify(logger, never()).severe(Matchers.anyString());
    }
}
