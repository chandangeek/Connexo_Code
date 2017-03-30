/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    private Logger logger;
    @Mock
    private Clock clock;

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceService, topologyService, meteringService, deviceLifeCycleService, finiteStateMachineService);
        this.setupTranslations();
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setTopologyService(topologyService);
        context.setMeteringService(meteringService);
        context.setDeviceLifeCycleService(deviceLifeCycleService);
        context.setFiniteStateMachineService(finiteStateMachineService);
        context.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        context.setClock(clock);
        when(context.getThesaurus()).thenReturn(thesaurus);
        when(deviceService.findDeviceByMrid(anyString())).thenReturn(Optional.empty());
    }

    private void setupTranslations() {
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        when(this.deviceLifeCycleConfigurationService.getDisplayName(any(DefaultState.class)))
                .thenAnswer(invocationOnMock -> {
                    DefaultState state = (DefaultState) invocationOnMock.getArguments()[0];
                    return state.getDefaultFormat();
                });
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
        String csv = "Device name;Transition date;Activate\n" +
                "VPB0001;01/08/2015 00:30;true";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        Device device = mock(Device.class, RETURNS_DEEP_STUBS);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(device.getLifecycleDates().getInstalledDate()).thenReturn(Optional.empty());
        when(device.forValidation().getLastChecked()).thenReturn(Optional.empty());

        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));

        createDeviceActivationDeactivationImporter().process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(anyString());
        verify(logger).warning(contains(
                thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER)
                         .format(2, DefaultState.ACTIVE.getDefaultFormat(), DefaultState.IN_STOCK.getDefaultFormat(), DefaultState.INACTIVE.getDefaultFormat())));
        verify(logger, never()).severe(anyString());
    }

    @Test
    public void testDeactivateTransitionIsNotSupportedByImporter() {
        String csv = "Device name;Transition date;Activate\n" +
                "VPB0001;01/08/2015 00:30;false";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);

        Device device = mock(Device.class);
        CIMLifecycleDates dates = mock(CIMLifecycleDates.class);
        DeviceValidation validation = mock(DeviceValidation.class);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.COMMISSIONING.getKey());
        when(device.getLifecycleDates()).thenReturn(dates);
        when(dates.getInstalledDate()).thenReturn(Optional.empty());
        when(device.forValidation()).thenReturn(validation);
        when(validation.getLastChecked()).thenReturn(Optional.empty());

        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));

        createDeviceActivationDeactivationImporter().process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(anyString());
        verify(logger).warning(contains(
                thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER)
                         .format(2, DefaultState.INACTIVE.getDefaultFormat(), DefaultState.COMMISSIONING.getDefaultFormat(), DefaultState.ACTIVE.getDefaultFormat())));
        verify(logger, never()).severe(anyString());
    }
}
