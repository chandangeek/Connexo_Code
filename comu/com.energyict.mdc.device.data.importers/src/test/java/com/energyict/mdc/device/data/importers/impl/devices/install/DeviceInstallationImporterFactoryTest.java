package com.energyict.mdc.device.data.importers.impl.devices.install;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.devices.installation.DeviceInstallationImporterFactory;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInstallationImporterFactoryTest {

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

    private FileImporter createDeviceInstallImporter() {
        DeviceInstallationImporterFactory factory = new DeviceInstallationImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        return factory.createImporter(properties);
    }

    @Test
    public void testSuccessCaseInstallActive() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.of(usagePoint));
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        Meter meter = mock(Meter.class);
        when(amrSystem.findMeter("1")).thenReturn(Optional.of(meter));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testBadColumnNumberCase() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.FILE_FORMAT_ERROR.getTranslated(thesaurus, 2, 2, 1));
    }

    @Test
    public void testMissingMandatoryDeviceMridValueCase() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "   ;01/08/2015 00:30;VPB0001;Usage MRID;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 2, "mrid"));
    }

    @Test
    public void testMissingMandatoryInstallationDateValueCase() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;  ;VPB0001;Usage MRID;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 2, "installation date"));
    }

    @Test
    public void testBadDeviceMrid() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.empty());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.NO_DEVICE.getTranslated(thesaurus, 2, "VPB0002"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testSuccessCaseAlreadyHasMasterWithTheSameMrid() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(masterDevice));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.of(usagePoint));
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        Meter meter = mock(Meter.class);
        when(amrSystem.findMeter("1")).thenReturn(Optional.of(meter));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, never()).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testSuccessCaseAlreadyHasMasterWithDifferentMrid() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        Device oldMasterDevice = mock(Device.class);
        when(oldMasterDevice.getmRID()).thenReturn("VPB0000");
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldMasterDevice));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.of(usagePoint));
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        Meter meter = mock(Meter.class);
        when(amrSystem.findMeter("1")).thenReturn(Optional.of(meter));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));
        verify(logger, times(1)).info(TranslationKeys.MASTER_WILL_BE_OVERRIDDEN.getTranslated(thesaurus, 2, "VPB0000", "VPB0001"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testBadMasterDeviceMrid() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.NO_MASTER_DEVICE.getTranslated(thesaurus, 2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testWithoutUsagePoint() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;   ;   ;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(meteringService, never()).findUsagePoint(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndGoodServiceKind() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint("Usage MRID")).thenReturn(usagePoint);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        Meter meter = mock(Meter.class);
        when(amrSystem.findMeter("1")).thenReturn(Optional.of(meter));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));
        verify(logger, times(1)).info(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED.getTranslated(thesaurus, 2, "Usage MRID"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndBadServiceKind() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;some;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint("Usage MRID")).thenReturn(usagePoint);

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, times(1)).info(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED.getTranslated(thesaurus, 2, "Usage MRID"));
        verify(logger, times(1)).warning(MessageSeeds.NO_USAGE_POINT.getTranslated(thesaurus, 2, "Usage MRID",
                Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndNoServiceKind() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;    ;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint("Usage MRID")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint("Usage MRID")).thenReturn(usagePoint);

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, times(1)).info(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED.getTranslated(thesaurus, 2, "Usage MRID"));
        verify(logger, times(1)).warning(MessageSeeds.NO_USAGE_POINT.getTranslated(thesaurus, 2, "Usage MRID",
                Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testSuccessCaseInstallInactive() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;  ;  ;  ;true;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceCanNotBeMovedToThatState() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.empty());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE.getTranslated(thesaurus, 2,
                DefaultState.ACTIVE.getKey(), DefaultState.IN_STOCK.getKey()));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceAlreadyInThatState() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.ACTIVE.getKey());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.DEVICE_ALREADY_IN_THAT_STATE.getTranslated(thesaurus, 2, DefaultState.ACTIVE.getKey()));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testPreTransitionCheckFailed() {
        String csv = "mrid;installation date;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;VPB0001;Usage MRID;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        doThrow(new MultipleMicroCheckViolationsException(thesaurus, null, Collections.<DeviceLifeCycleActionViolation>emptyList()))
                .when(executableAction).execute(Matchers.any(Instant.class), Matchers.anyList());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.startsWith("Error in line 2: Pre-transition check(s) failed: "));
        verify(logger, never()).severe(Matchers.anyString());
    }
}
