/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.topology.TopologyService;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.ALLOW_REASSIGNING;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DELIMITER;
import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.DEVICE_IDENTIFIER;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.INVALID_TOPOLOGY_IMPORT_RECORD;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.LINK_ALREADY_EXISTS;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.MASTER_DEVICE_NOT_CONFIGURED;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.MASTER_DEVICE_NOT_FOUND;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.MISSING_TITLE_ERROR;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.NO_LINK_EXISTS;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SAME_SERIAL_NUMBER;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_DEVICE_LINKED_TO_ANOTHER_MASTER;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_DEVICE_NOT_CONFIGURED;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_DEVICE_NOT_FOUND;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_DEVICE_SUCCESSFULLY_REASSIGNED;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_SUCCESSFULLY_LINKED;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.SLAVE_SUCCESSFULLY_UNLINKED;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.TOPOLOGY_SAME_DEVICE;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.UNSUPPORTED_DEVICE_IDENTIFIER;
import static com.energyict.mdc.device.data.importers.impl.MessageSeeds.WRONG_LINE_SIZE;
import static com.energyict.mdc.device.data.importers.impl.devices.topology.DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_NAME;
import static com.energyict.mdc.device.data.importers.impl.devices.topology.DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_SERIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTopologyImporterFactoryTest {
    private final Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private DeviceDataImporterContext context;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private TopologyService topologyService;
    @Mock
    Logger logger;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;

    @Before
    public void beforeTest() throws SQLException {
        reset(logger, deviceConfigurationService, deviceService, topologyService);
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setDeviceConfigurationService(deviceConfigurationService);
        context.setPropertySpecService(propertySpecService);
        context.setTopologyService(topologyService);
        when(ormService.getDataModels()).thenReturn(Collections.singletonList(dataModel));
        context.setOrmService(ormService);
        when(context.getThesaurus()).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createDeviceTopologyImporter(String deviceIdentifier, boolean allowReassigning) {
        DeviceTopologyImporterFactory factory = new DeviceTopologyImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DEVICE_IDENTIFIER.getPropertyKey(), deviceIdentifier);
        properties.put(ALLOW_REASSIGNING.getPropertyKey(), allowReassigning);
        return factory.createImporter(properties);
    }

    @Test
    public void testGetProperties() {
        DeviceTopologyImporterFactory factory = new DeviceTopologyImporterFactory(context);
        Set<DeviceDataImporterProperty> properties = factory.getProperties();

        assertThat(properties).hasSize(3);
        assertThat(properties).containsExactly(DELIMITER, DEVICE_IDENTIFIER, ALLOW_REASSIGNING);
    }

    @Test
    public void testSuccessCaseDeviceNameNotReassigningLink() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_LINKED).format(2, slaveDevice.getName(), masterDevice.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseDeviceNameNotReassigningLinkExists() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(masterDevice));

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).info(thesaurus.getFormat(LINK_ALREADY_EXISTS).format(2, slaveDevice.getName(), masterDevice.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseDeviceNameNotReassigningUnlink() {
        String csv = "masterDevice;slaveDevice\n" +
                ";SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        when(masterDevice.getName()).thenReturn("SPE01000003");
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        Optional<Device> deviceOptional = Optional.of(masterDevice);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(deviceOptional);

        importer.process(importOccurrence);

        verify(topologyService).clearPhysicalGateway(slaveDevice);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice.getName(), masterDevice.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseDeviceNameNotReassigningNothingToUnlink() {
        String csv = "masterDevice;slaveDevice\n" +
                ";SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService, never()).clearPhysicalGateway(slaveDevice);
        verify(logger).info(thesaurus.getFormat(NO_LINK_EXISTS).format(2, slaveDevice.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseDeviceNameNotReassigningUnlinkAll() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice1 = mock(Device.class);
        Device slaveDevice2 = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");
        when(slaveDevice1.getName()).thenReturn("SPG01000004");
        when(slaveDevice2.getName()).thenReturn("SPW01000004");
        Optional<Device> deviceOptional = Optional.of(masterDevice);
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(slaveDevice2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        List<Device> deviceList = Arrays.asList(slaveDevice1, slaveDevice2);
        when(topologyService.getSlaveDevices(masterDevice)).thenReturn(deviceList);
        when(topologyService.getPhysicalGateway(slaveDevice1)).thenReturn(deviceOptional);
        when(topologyService.getPhysicalGateway(slaveDevice2)).thenReturn(deviceOptional);

        importer.process(importOccurrence);

        verify(topologyService).clearPhysicalGateway(slaveDevice1);
        verify(topologyService).clearPhysicalGateway(slaveDevice2);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice1.getName(), masterDevice.getName()));
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice2.getName(), masterDevice.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseDeviceNameReassigningLink() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, true);
        Device masterDevice1 = mock(Device.class);
        Device masterDevice2 = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice1));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice1.getName()).thenReturn("SPE01000003");
        when(masterDevice2.getName()).thenReturn("SPE01000004");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(masterDevice1.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(masterDevice2));

        importer.process(importOccurrence);

        verify(topologyService).setPhysicalGateway(slaveDevice, masterDevice1);
        verify(logger).info(thesaurus.getFormat(SLAVE_DEVICE_SUCCESSFULLY_REASSIGNED).format(2, slaveDevice.getName(), masterDevice2.getName(), masterDevice1.getName()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseSerialNumberNotReassigningLink() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> masterDeviceList = Collections.singletonList(masterDevice);
        List<Device> slaveDeviceList = Collections.singletonList(slaveDevice);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList);
        when(masterDevice.getSerialNumber()).thenReturn("SPE01000003");
        when(slaveDevice.getSerialNumber()).thenReturn("SPG01000004");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_LINKED).format(2, slaveDevice.getSerialNumber(), masterDevice.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
        verify(masterDevice, never()).getName();
    }

    @Test
    public void testSuccessCaseSerialNumberNotReassigningLinkExists() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        List<Device> masterDeviceList = Collections.singletonList(masterDevice);
        List<Device> slaveDeviceList = Collections.singletonList(slaveDevice);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList);
        when(masterDevice.getSerialNumber()).thenReturn("SPE01000003");
        when(slaveDevice.getSerialNumber()).thenReturn("SPG01000004");
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(masterDevice));

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).info(thesaurus.getFormat(LINK_ALREADY_EXISTS).format(2, slaveDevice.getSerialNumber(), masterDevice.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseSerialNumberNotReassigningUnlink() {
        String csv = "masterDevice;slaveDevice\n" +
                ";SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device masterDevice = mock(Device.class);
        when(masterDevice.getSerialNumber()).thenReturn("SPE01000003");
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        Optional<Device> deviceOptional = Optional.of(masterDevice);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> slaveDeviceList = Collections.singletonList(slaveDevice);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList);
        when(slaveDevice.getSerialNumber()).thenReturn("SPG01000004");
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(deviceOptional);

        importer.process(importOccurrence);

        verify(topologyService).clearPhysicalGateway(slaveDevice);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice.getSerialNumber(), masterDevice.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseSerialNumberNotReassigningNothingToUnlink() {
        String csv = "masterDevice;slaveDevice\n" +
                ";SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> slaveDeviceList = Collections.singletonList(slaveDevice);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList);
        when(slaveDevice.getSerialNumber()).thenReturn("SPG01000004");
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService, never()).clearPhysicalGateway(slaveDevice);
        verify(logger).info(thesaurus.getFormat(NO_LINK_EXISTS).format(2, slaveDevice.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseSerialNumberNotReassigningUnlinkAll() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice1 = mock(Device.class);
        Device slaveDevice2 = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> masterDeviceList = Collections.singletonList(masterDevice);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList);
        when(masterDevice.getSerialNumber()).thenReturn("SPE01000003");
        when(slaveDevice1.getSerialNumber()).thenReturn("SPG01000004");
        when(slaveDevice2.getSerialNumber()).thenReturn("SPW01000004");
        Optional<Device> deviceOptional = Optional.of(masterDevice);
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(slaveDevice2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        List<Device> deviceList = Arrays.asList(slaveDevice1, slaveDevice2);
        when(topologyService.getSlaveDevices(masterDevice)).thenReturn(deviceList);
        when(topologyService.getPhysicalGateway(slaveDevice1)).thenReturn(deviceOptional);
        when(topologyService.getPhysicalGateway(slaveDevice2)).thenReturn(deviceOptional);

        importer.process(importOccurrence);

        verify(topologyService).clearPhysicalGateway(slaveDevice1);
        verify(topologyService).clearPhysicalGateway(slaveDevice2);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice1.getSerialNumber(), masterDevice.getSerialNumber()));
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_UNLINKED).format(2, slaveDevice2.getSerialNumber(), masterDevice.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testSuccessCaseSerialNumberReassigningLink() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, true);
        Device masterDevice1 = mock(Device.class);
        Device masterDevice2 = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> masterDeviceList1 = Collections.singletonList(masterDevice1);
        List<Device> slaveDeviceList = Collections.singletonList(slaveDevice);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList1);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList);
        when(masterDevice1.getSerialNumber()).thenReturn("SPE01000003");
        when(masterDevice2.getSerialNumber()).thenReturn("SPE01000004");
        when(slaveDevice.getSerialNumber()).thenReturn("SPG01000004");
        when(masterDevice1.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(masterDevice2));

        importer.process(importOccurrence);

        verify(topologyService).setPhysicalGateway(slaveDevice, masterDevice1);
        verify(logger).info(thesaurus.getFormat(SLAVE_DEVICE_SUCCESSFULLY_REASSIGNED)
                .format(2, slaveDevice.getSerialNumber(), masterDevice2.getSerialNumber(), masterDevice1.getSerialNumber()));
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(1));
    }

    @Test
    public void testPartialSuccessCaseSerialNumberNotReassigning() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004\n" +
                "SPE01000003;SPW01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        Device masterDevice1 = mock(Device.class);
        Device masterDevice2 = mock(Device.class);
        Device slaveDevice1 = mock(Device.class);
        Device slaveDevice2 = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        List<Device> masterDeviceList = Collections.singletonList(masterDevice1);
        List<Device> slaveDeviceList1 = Collections.singletonList(slaveDevice1);
        List<Device> slaveDeviceList2 = Collections.singletonList(slaveDevice2);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList);
        when(deviceService.findDevicesBySerialNumber("SPG01000004")).thenReturn(slaveDeviceList1);
        when(deviceService.findDevicesBySerialNumber("SPW01000004")).thenReturn(slaveDeviceList2);
        when(masterDevice1.getSerialNumber()).thenReturn("SPE01000003");
        when(masterDevice2.getSerialNumber()).thenReturn("SPE01000004");
        when(slaveDevice1.getSerialNumber()).thenReturn("SPG01000004");
        when(slaveDevice2.getSerialNumber()).thenReturn("SPW01000004");
        Optional<Device> deviceOptional = Optional.of(masterDevice2);
        when(masterDevice1.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(slaveDevice2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice1)).thenReturn(Optional.empty());
        when(topologyService.getPhysicalGateway(slaveDevice2)).thenReturn(deviceOptional);

        importer.process(importOccurrence);

        verify(topologyService).setPhysicalGateway(slaveDevice1, masterDevice1);
        verify(topologyService, never()).setPhysicalGateway(slaveDevice2, masterDevice2);
        verify(logger).info(thesaurus.getFormat(SLAVE_SUCCESSFULLY_LINKED).format(2, slaveDevice1.getSerialNumber(), masterDevice1.getSerialNumber()));
        verify(logger).warning(thesaurus.getFormat(SLAVE_DEVICE_LINKED_TO_ANOTHER_MASTER)
                .format(3, slaveDevice2.getSerialNumber(), masterDevice2.getSerialNumber(), masterDevice1.getSerialNumber()));
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(1, 1));
    }

    @Test
    public void testFailureCaseDeviceNameNotReassigningLink() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice1 = mock(Device.class);
        Device masterDevice2 = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice1));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice1.getName()).thenReturn("SPE01000003");
        when(masterDevice2.getName()).thenReturn("SPE01000004");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(masterDevice1.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.of(masterDevice2));

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(slaveDevice, masterDevice1);
        verify(logger).warning(thesaurus.getFormat(SLAVE_DEVICE_LINKED_TO_ANOTHER_MASTER).format(2, slaveDevice.getName(), masterDevice2.getName(), masterDevice1.getName()));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseDeviceNameMasterNotConfigured() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.NONE);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).warning(thesaurus.getFormat(MASTER_DEVICE_NOT_CONFIGURED).format(2, masterDevice.getName()));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseDeviceNameSlaveNotConfigured() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        Device slaveDevice = mock(Device.class);
        DeviceConfiguration slaveDeviceConfiguration = mock(DeviceConfiguration.class);
        when(slaveDeviceConfiguration.isDirectlyAddressable()).thenReturn(true);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(deviceService.findDeviceByName("SPG01000004")).thenReturn(Optional.of(slaveDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");
        when(slaveDevice.getName()).thenReturn("SPG01000004");
        when(masterDevice.getConfigurationGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(slaveDevice.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration);
        when(topologyService.getPhysicalGateway(slaveDevice)).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(slaveDevice, masterDevice);
        verify(logger).warning(thesaurus.getFormat(SLAVE_DEVICE_NOT_CONFIGURED).format(2, slaveDevice.getName()));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseDeviceNameSameDevice() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000003";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        Device masterDevice = mock(Device.class);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(masterDevice));
        when(masterDevice.getName()).thenReturn("SPE01000003");

        importer.process(importOccurrence);

        verify(topologyService, never()).setPhysicalGateway(masterDevice, masterDevice);
        verify(logger).warning(thesaurus.getFormat(TOPOLOGY_SAME_DEVICE).format(2, masterDevice.getName()));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseDeviceNameMasterNotFound() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MASTER_DEVICE_NOT_FOUND).format(2, "SPE01000003"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseDeviceNameSlaveNotFound() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);
        when(deviceService.findDeviceByName("SPE01000003")).thenReturn(Optional.of(mock(Device.class)));
        when(deviceService.findDeviceByName("SPE01000004")).thenReturn(Optional.empty());

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(SLAVE_DEVICE_NOT_FOUND).format(2, "SPE01000004"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseSerialNumberMasterNotFound() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(Collections.emptyList());

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(MASTER_DEVICE_NOT_FOUND).format(2, "SPE01000003"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseSerialNumberSlaveNotFound() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, false);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(Collections.singletonList(mock(Device.class)));
        when(deviceService.findDevicesBySerialNumber("SPE01000004")).thenReturn(Collections.emptyList());

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(SLAVE_DEVICE_NOT_FOUND).format(2, "SPE01000004"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseInvalidHeader() {
        String csv = "masterDevice\n" +
                "SPE01000003;SPE01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);

        importer.process(importOccurrence);

        verify(logger).severe(thesaurus.getFormat(MISSING_TITLE_ERROR).format(2, 1));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCORRECT_HEADER).format());
    }

    @Test
    public void testFailureCaseEmptyFile() {
        String csv = "masterDevice;slaveDevice\n";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);

        importer.process(importOccurrence);

        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_NO_REQUESTS_PROCESSED).format());
    }

    @Test
    public void testFailureCaseFileFormatError() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPE01000004;SPE01000005\n" +
                "SPE01000003\n" +
                ";";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_NAME, false);

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(WRONG_LINE_SIZE).format(2));
        verify(logger).warning(thesaurus.getFormat(WRONG_LINE_SIZE).format(3));
        verify(logger).warning(thesaurus.getFormat(INVALID_TOPOLOGY_IMPORT_RECORD).format(4));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(3));
    }

    @Test
    public void testFailureCaseSerialNumberNotUnique() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter(DEVICE_IDENTIFIER_SERIAL, true);
        Device masterDevice1 = mock(Device.class);
        Device masterDevice2 = mock(Device.class);
        List<Device> masterDeviceList1 = Arrays.asList(masterDevice1, masterDevice2);
        when(deviceService.findDevicesBySerialNumber("SPE01000003")).thenReturn(masterDeviceList1);
        when(masterDevice1.getSerialNumber()).thenReturn("SPE01000003");

        importer.process(importOccurrence);

        verify(logger).warning(thesaurus.getFormat(SAME_SERIAL_NUMBER).format(2, masterDevice1.getSerialNumber()));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(1));
    }

    @Test
    public void testFailureCaseWrongIdentifier() {
        String csv = "masterDevice;slaveDevice\n" +
                "SPE01000003;SPG01000004";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceTopologyImporter("Fake", true);

        importer.process(importOccurrence);

        verify(logger).severe(thesaurus.getFormat(UNSUPPORTED_DEVICE_IDENTIFIER).format("Fake"));
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCOMPLETE_ALL_ERRORS).format(0));
    }

}
