package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationBuilder.LocationMemberBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.LocationTemplateImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.parsers.DateParser;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.topology.TopologyService;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DATE_FORMAT;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.TIME_ZONE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInstallationImporterFactoryTest {

    private DeviceDataImporterContext context;
    private LocationTemplate locationTemplate;

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private DeviceService deviceService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private LocationBuilder locationBuilder;
    @Mock
    private LocationMemberBuilder locationMemberBuilder;
    @Mock
    private DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    @Mock
    private FiniteStateMachineService finiteStateMachineService;
    @Mock
    private Logger logger;
    @Mock
    private Clock clock;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private MeterRole defaultMeterRole;

    private Meter meter;

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
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findEndDeviceByMRID(anyString())).thenReturn(Optional.empty());

        final String templateMembers = "#ccod,#cnam,#adma,#loc,#subloc,#styp,#snam,#snum,#etyp,#enam,#enum,#addtl,#zip,#locale";
        when(dataModel.getInstance(LocationTemplateImpl.class)).thenReturn(new LocationTemplateImpl(dataModel));
        meter = mock(Meter.class, Mockito.RETURNS_DEEP_STUBS);
        when(meter.getAmrSystem().newMeter(eq(meter.getAmrId()), anyString()).newLocationBuilder()).thenReturn(locationBuilder);
        locationTemplate = LocationTemplateImpl.from(dataModel, templateMembers, templateMembers);
        locationTemplate.parseTemplate(templateMembers, templateMembers);
        when(context.getMeteringService().getLocationTemplate()).thenReturn(locationTemplate);
        when(meteringService.findEndDeviceByName("VPB0002")).thenReturn(Optional.of(meter));
        when(locationBuilder.getMemberBuilder("locale")).thenReturn(Optional.empty());
        when(locationBuilder.member()).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setCountryName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setCountryCode(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setAdministrativeArea(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setLocality(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setSubLocality(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetType(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetNumber(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentType(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentNumber(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setAddressDetail(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setZipCode(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setLocale(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.isDaultLocation(anyBoolean())).thenReturn(locationMemberBuilder);
        when(context.getMetrologyConfigurationService()).thenReturn(metrologyConfigurationService);
        when(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT)).thenReturn(defaultMeterRole);
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
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testSuccessCaseInstallActiveForDevicesIdentifiedByMrid() {
        String csv = "mrid;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master mrid;usage point;service category;install inactive;start validation\n" +
                "6a2632a4-6b73-4a13-bbcc-09c8bdd02308;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;7a2632a4-6b73-4a13-bbcc-09c8bdd02308;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByMrid("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(device));
        when(meteringService.findEndDeviceByMRID("6a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(meter));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByMrid("7a2632a4-6b73-4a13-bbcc-09c8bdd02308")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testSuccessCaseInstallActiveWithoutLocation() {
        String csv = "mrid;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master mrid;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;;;;;;;;;;;;;;;VPB0001;Usage Point;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByMrid("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getmRID()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByMrid("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePointByName("Usage Point")).thenReturn(Optional.of(usagePoint));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testBadColumnNumberCase() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
    }

    @Test
    public void testMissingMandatoryDeviceNameValueCase() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "   ;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
    }

    @Test
    public void testMissingMandatoryInstallationDateValueCase() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;  ;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
    }

    @Test
    public void testBadDeviceName() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;countryCode;countryName;17;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.empty());
        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE).format(2, "VPB0002"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testSuccessCaseAlreadyHasTheSameMaster() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(masterDevice));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, never()).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testSuccessCaseHasAnotherMaster() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        Device oldMasterDevice = mock(Device.class);
        when(oldMasterDevice.getName()).thenReturn("VPB0000");
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(oldMasterDevice));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN).format(1, 1));
        verify(logger, times(1)).info(thesaurus.getFormat(TranslationKeys.MASTER_WILL_BE_OVERRIDDEN).format(2, "VPB0000", "VPB0001"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testBadMasterDeviceIdentifier() {
        String csv = "name;installation date;countryCode;countryName;elevation;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.empty());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_MASTER_DEVICE).format(2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testWithoutUsagePoint() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001; ; ;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(meteringService, never()).findUsagePointByName(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndGoodServiceKind() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint(eq("UP0001"), any(Instant.class))).thenReturn(FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN).format(1, 1));
        verify(logger, times(1)).info(thesaurus.getFormat(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED).format(2, "UP0001"));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndBadServiceKind() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;some;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint(eq("UP0001"), any(Instant.class))).thenReturn(FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class));

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, times(1)).info(thesaurus.getFormat(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED).format(2, "UP0001"));
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_USAGE_POINT)
                .format(2, "UP0001", Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testBadUsagePointAndNoServiceKind() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;    ;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.empty());
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(serviceCategory.newUsagePoint(eq("UP0001"), any(Instant.class))).thenReturn(FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class));

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, times(1)).info(thesaurus.getFormat(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED).format(2, "UP0001"));
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_USAGE_POINT)
                .format(2, "UP0001", Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testSuccessCaseInstallInactive() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;  ;  ;  ;true;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
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

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testSuccessCaseInstallActiveWithMultiplier() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation;multiplier\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;;;electricity;false;01/08/2015 00:30;5.6";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class, withSettings().extraInterfaces(AuthorizedTransitionAction.class));
        when(((AuthorizedTransitionAction) authorizedAction).getActions()).thenReturn(EnumSet.of(MicroAction.SET_MULTIPLIER));
        when(executableAction.getAction()).thenReturn(authorizedAction);
        PropertySpec multiplierPropertySpec = mock(PropertySpec.class);
        when(multiplierPropertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key());
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.SET_MULTIPLIER)).thenReturn(Collections.singletonList(multiplierPropertySpec));

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testFailureCaseInstallActiveWithIncorrectMultiplier() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation;multiplier\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;;;electricity;false;01/08/2015 00:30;abc";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class, withSettings().extraInterfaces(AuthorizedTransitionAction.class));
        when(((AuthorizedTransitionAction) authorizedAction).getActions()).thenReturn(EnumSet.of(MicroAction.SET_MULTIPLIER));
        when(executableAction.getAction()).thenReturn(authorizedAction);
        PropertySpec multiplierPropertySpec = mock(PropertySpec.class);
        when(multiplierPropertySpec.getName()).thenReturn(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key());
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.SET_MULTIPLIER)).thenReturn(Collections.singletonList(multiplierPropertySpec));

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.LINE_FORMAT_ERROR).format(2, "multiplier", "123456789.012"));
    }

    @Test
    public void testSuccessCaseInstallActiveWithMultiplierWhenNotApplicableForAction() {
        String csv = "name;installation date;latitude;longitude;countryCode;elevation;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation;multiplier\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;;;electricity;false;01/08/2015 00:30;5.6";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class, withSettings().extraInterfaces(AuthorizedTransitionAction.class));
        when(((AuthorizedTransitionAction) authorizedAction).getActions()).thenReturn(EnumSet.of(MicroAction.SET_MULTIPLIER));
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(deviceLifeCycleService.getPropertySpecsFor(MicroAction.SET_MULTIPLIER)).thenReturn(Collections.emptyList());

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN).format(1, 1));
        verify(logger, times(1)).info(thesaurus.getFormat(MessageSeeds.USELESS_MULTIPLIER_CONFIGURED).format(2, 5.6));
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceCanNotBeMovedToThatState() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.empty());

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE).format(2, DefaultState.ACTIVE.getDefaultFormat(), DefaultState.IN_STOCK.getDefaultFormat()));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceAlreadyInThatState() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.ACTIVE.getKey());

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.DEVICE_ALREADY_IN_THAT_STATE).format(2, DefaultState.ACTIVE.getDefaultFormat()));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testPreTransitionCheckFailed() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
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
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.startsWith("Can't process line 2: Pre-transition check(s) failed: "));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testTransitionIsNotSupportedByImporter() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7427346;21.2384365;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;bla-bla;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.INACTIVE.getKey());

        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger).warning(thesaurus.getFormat(MessageSeeds.DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER)
                .format(2, DefaultState.ACTIVE.getDefaultFormat(), DefaultState.INACTIVE.getDefaultFormat(), DefaultState.IN_STOCK.getDefaultFormat() + ", " + DefaultState.COMMISSIONING.getDefaultFormat()));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceCanNotBeLinkedToUsagePointAlreadyInUse() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getName()).thenReturn("UP0001");
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT+00:00");
        Instant installationTime = dateParser.parse("01/08/2015 00:30").toInstant();
        UsagePointAlreadyLinkedToAnotherDeviceException ex = mock(UsagePointAlreadyLinkedToAnotherDeviceException.class);
        MeterActivation ma = mock(MeterActivation.class);
        when(ma.getStart()).thenReturn(installationTime);
        Meter alreadyLinkedMeter = mock(Meter.class);
        when(alreadyLinkedMeter.getName()).thenReturn("VPB0003");
        when(ma.getMeter()).thenReturn(Optional.of(alreadyLinkedMeter));
        when(ex.getMeterActivation()).thenReturn(ma);
        when(device.activate(installationTime, usagePoint, defaultMeterRole)).thenThrow(ex);

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE)
                .format(2, "UP0001", "VPB0003", DeviceInstallationImportProcessor.getFormattedInstant(installationTime)));
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }

    @Test
    public void testDeviceCanNotBeLinkedToUsagePointMissingReadingTypeRequirements() {
        String csv = "name;installation date;latitude;longitude;elevation;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;master name;usage point;service category;install inactive;start validation\n" +
                "VPB0002;01/08/2015 00:30;45.7540873;21.22388;17;countryCode;countryName;administrativeArea;locality;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;VPB0001;UP0001;electricity;false;01/08/2015 00:30";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceInstallImporter();

        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getName()).thenReturn("VPB0002");
        when(deviceService.findDeviceByName("VPB0002")).thenReturn(Optional.of(device));
        State deviceState = mock(State.class);
        when(device.getState()).thenReturn(deviceState);
        when(deviceState.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("VPB0001");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceService.findDeviceByName("VPB0001")).thenReturn(Optional.of(masterDevice));
        CustomStateTransitionEventType transitionEventType = mock(CustomStateTransitionEventType.class);
        when(finiteStateMachineService.findCustomStateTransitionEventType(Matchers.anyString())).thenReturn(Optional.of(transitionEventType));
        ExecutableAction executableAction = mock(ExecutableAction.class);
        when(deviceLifeCycleService.getExecutableActions(device, transitionEventType)).thenReturn(Optional.of(executableAction));
        AuthorizedAction authorizedAction = mock(AuthorizedAction.class);
        when(executableAction.getAction()).thenReturn(authorizedAction);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.empty());
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getName()).thenReturn("UP0001");
        when(meteringService.findUsagePointByName("UP0001")).thenReturn(Optional.of(usagePoint));

        DateParser dateParser = new DateParser("dd/MM/yyyy HH:mm", "GMT+00:00");
        Instant installationTime = dateParser.parse("01/08/2015 00:30").toInstant();
        UnsatisfiedReadingTypeRequirementsOfUsagePointException ex = mock(UnsatisfiedReadingTypeRequirementsOfUsagePointException.class);
        when(ex.getUnsatisfiedRequirements()).thenReturn(Collections.emptyMap());
        when(device.activate(installationTime, usagePoint, defaultMeterRole)).thenThrow(ex);

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT).format(2, "VPB0002", "UP0001", ""));
        verify(logger, never()).severe(Matchers.anyString());
        verify(topologyService, times(1)).setPhysicalGateway(device, masterDevice);
    }
}
