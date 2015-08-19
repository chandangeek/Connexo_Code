package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.exception.MessageSeed;
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
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 1));
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
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.FILE_FORMAT_ERROR.getTranslated(thesaurus, 2, 4, 2));
    }

    @Test
    public void testMissingMandatoryDeviceTypeValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001; ;Default;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 2, "device type"));
    }

    @Test
    public void testMissingMandatoryDeviceConfigurationValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;    ;01/08/2015 00:30;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 2, "device configuration"));
    }

    @Test
    public void testMissingMandatoryShipmentDateValueCase() {
        String csv = "mrid;device type;device configuration;shipment date; serial number; year of certification;batch\n" +
                "VPB0001;Iskra 382;Default;   ;0001;2015;batch";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImporter importer = createDeviceShipmentImporter();

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getTranslated(thesaurus));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(MessageSeeds.LINE_MISSING_VALUE_ERROR.getTranslated(thesaurus, 2, "shipment date"));
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
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.NO_DEVICE_TYPE.getTranslated(thesaurus, 2, "Iskra 382"));
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
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.NO_DEVICE_CONFIGURATION.getTranslated(thesaurus, 2, "Default"));
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
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE
                .getTranslated(thesaurus, 2, "VPB0001", "Error!"));
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
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(MessageSeeds.DEVICE_ALREADY_EXISTS.getTranslated(thesaurus, 2, "VPB0001"));
        verify(logger, never()).severe(Matchers.anyString());
    }
}
