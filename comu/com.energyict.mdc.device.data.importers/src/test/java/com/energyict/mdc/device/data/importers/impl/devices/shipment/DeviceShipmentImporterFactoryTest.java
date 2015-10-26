package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.SimpleNlsMessageFormat;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceShipmentImporterFactoryTest {

    @Mock
    private Thesaurus thesaurus;
    private DeviceDataImporterContext context;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private BatchService batchService;
    @Mock
    private Logger logger;

    @Before
    public void beforeTest() {
        reset(logger, thesaurus, deviceConfigurationService, deviceService, batchService);
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((TranslationKey) invocationOnMock.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocationOnMock -> new SimpleNlsMessageFormat((MessageSeed) invocationOnMock.getArguments()[0]));
        context = spy(new DeviceDataImporterContext());
        context.setDeviceService(deviceService);
        context.setDeviceConfigurationService(deviceConfigurationService);
        context.setPropertySpecService(propertySpecService);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private FileImporter createDeviceShipmentImporter() {
        DeviceShipmentImporterFactory factory = new DeviceShipmentImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DELIMITER.getPropertyKey(), ";");
        properties.put(DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(TIME_ZONE.getPropertyKey(), "GMT+00:00");
        return factory.createImporter(properties);
    }

    @Test
    public void testSuccessCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                     "VPB0001;Iskra 382;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceTypeByName("Iskra 382")).thenReturn(Optional.of(deviceType));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Default");
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        Device device = mock(Device.class);
        when(deviceService.newDevice(Matchers.eq(deviceConfiguration), Matchers.eq("VPB0001"), Matchers.eq("VPB0001"), Matchers.eq("batch"))).thenReturn(device);
        CIMLifecycleDates lifecycleDates = mock(CIMLifecycleDates.class);
        when(device.getLifecycleDates()).thenReturn(lifecycleDates);
        Batch batch = mock(Batch.class);
        when(batchService.findOrCreateBatch(Matchers.eq("batch"))).thenReturn(batch);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
        verify(device, times(1)).setSerialNumber("0001");
        verify(device, times(1)).setYearOfCertification(2015);
        verify(lifecycleDates, times(1)).setReceivedDate(Instant.ofEpochMilli(1438389000000L));
    }

    @Test
    public void testBadColumnNumberCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.FILE_FORMAT_ERROR).format(2, 4, 2));
    }

    @Test
    public void testMissingMandatoryDeviceTypeValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001; ;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.LINE_MISSING_VALUE_ERROR).format(2, "device type"));
    }

    @Test
    public void testMissingMandatoryDeviceConfigurationValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;    ;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.LINE_MISSING_VALUE_ERROR).format(2, "device configuration"));
    }

    @Test
    public void testMissingMandatoryShipmentDateValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;   ;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(thesaurus.getFormat(MessageSeeds.LINE_MISSING_VALUE_ERROR).format(2, "shipment date"));
    }

    @Test
    public void testBadDeviceTypeName() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceTypeByName("Iskra 382")).thenReturn(Optional.empty());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE_TYPE).format(2, "Iskra 382"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testBadDeviceConfigurationName() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceTypeByName("Iskra 382")).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Collections.<DeviceConfiguration>emptyList());

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.NO_DEVICE_CONFIGURATION).format(2, "Default"));
        verify(logger, never()).severe(Matchers.anyString());
    }
    @Test
    public void testSomeExceptionDuringDeviceCreation() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.empty());
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceConfigurationService.findDeviceTypeByName("Iskra 382")).thenReturn(Optional.of(deviceType));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Default");
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        doThrow(new RuntimeException("Error!")).when(deviceService)
                .newDevice(Matchers.eq(deviceConfiguration), Matchers.eq("VPB0001"), Matchers.eq("VPB0001"), Matchers.eq("batch"));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE).format(2, "VPB0001", "Error!"));
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testDeviceWithMridAlreadyExists() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("VPB0001")).thenReturn(Optional.of(device));

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(thesaurus.getFormat(MessageSeeds.DEVICE_ALREADY_EXISTS).format(2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
    }
}
