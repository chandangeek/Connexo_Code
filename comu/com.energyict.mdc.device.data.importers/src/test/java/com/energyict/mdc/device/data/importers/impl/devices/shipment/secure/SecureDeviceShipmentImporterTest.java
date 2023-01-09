package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricAlgorithm;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.ImporterExtension;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/20/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecureDeviceShipmentImporterTest {
    @Mock
    Thesaurus thesaurus;
    @Mock
    FileImportOccurrence fileImportOccurrence;
    @Mock
    TrustStore trustStore;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    DeviceService deviceService;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    ImporterExtension importerExtension;

    private TestHandler testHandler;

    @Before
    public void setUp() throws Exception {
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((com.elster.jupiter.util.exception.MessageSeed) invocation.getArguments()[0]));
        when(thesaurus.getSimpleFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((com.elster.jupiter.util.exception.MessageSeed) invocation.getArguments()[0]));
        Logger logger = Logger.getLogger("tests");
        logger.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
        testHandler = new TestHandler();
        logger.addHandler(testHandler);
        when(fileImportOccurrence.getLogger()).thenReturn(logger);
        SymmetricAlgorithm symmetricAlgorithm = mock(SymmetricAlgorithm.class);
        when(symmetricAlgorithm.getCipherName()).thenReturn("AES");
        when(securityManagementService.getSymmetricAlgorithm("http://www.w3.org/2001/04/xmlenc#aes256-cbc")).thenReturn(Optional.of(symmetricAlgorithm));
    }

    @Test
    public void importBeaconShipmentFile() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);

        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfig.getName()).thenReturn("Default");
        when(deviceConfig.isActive()).thenReturn(true);
        when(deviceConfig.isDefault()).thenReturn(true);

        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfig));
        when(deviceConfigurationService.findDeviceTypeByName("Beacon-3100/SM765")).thenReturn(Optional.of(deviceType));

        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.empty());
        Device newDevice = mock(Device.class);
        when(newDevice.getDeviceType()).thenReturn(deviceType);
        when(deviceService.newDevice(eq(deviceConfig) ,anyString(), anyString(), any(Instant.class))).thenReturn(newDevice);
        when(deviceService.newDevice(eq(deviceConfig), anyString(), anyString(), any(Instant.class))).thenReturn(newDevice);
        SecurityAccessor keyAccessor = mock(SecurityAccessor.class);
        when(keyAccessor.getActualValue()).thenReturn(Optional.empty());
        when(keyAccessor.getTempValue()).thenReturn(Optional.empty());
        DeviceSecretImporter deviceSecretImporter = mock(DeviceSecretImporter.class);
        List<SecurityAccessorType> keyAccessorTypes = Stream.of("NTP_HASH", "MK_DC", "EAP_PSK_DC", "WEB_PORTAL_LOGIN_RO", "WEB_PORTAL_LOGIN_RW",
                "WEB_PORTAL_LOGIN_ADMIN", "Priv_DC_SSH_cl", "Pub_DC_SSH_sv", "DLMS_WAN_DMGMT_MC_GUEK", "DLMS_WAN_DBROAD_MC_GUEK",
                "DLMS_WAN_DMGMT_RW_GUEK", "DLMS_WAN_DBROAD_RW_GUEK", "DLMS_WAN_DMGMT_FU_GUEK", "DLMS_WAN_DBROAD_FU_GUEK",
                "DLMS_WAN_DMGMT_MC_GAK", "DLMS_WAN_DBROAD_MC_GAK", "DLMS_WAN_DMGMT_RW_GAK", "DLMS_WAN_DBROAD_RW_GAK",
                "DLMS_WAN_DMGMT_FU_GAK", "DLMS_WAN_DBROAD_FU_GAK", "DLMS_WAN_DMGMT_MC_LLS", "DLMS_WAN_DBROAD_MC_LLS",
                "DLMS_WAN_DBROAD_MC_LLS", "DLMS_WAN_DMGMT_RW_LLS", "DLMS_WAN_DBROAD_RW_LLS", "DLMS_WAN_DMGMT_FU_LLS",
                "DLMS_WAN_DBROAD_FU_LLS", "DLMS_WAN_DMGMT_MC_HLS", "DLMS_WAN_DBROAD_MC_HLS", "DLMS_WAN_DMGMT_RW_HLS",
                "DLMS_WAN_DBROAD_RW_HLS", "DLMS_WAN_DMGMT_FU_HLS", "DLMS_WAN_DBROAD_FU_HLS").map(name -> {
            SecurityAccessorType keyAccessorType = mock(SecurityAccessorType.class);
            when(keyAccessorType.getName()).thenReturn(name);
            when(newDevice.getSecurityAccessor(keyAccessorType)).thenReturn(Optional.of(keyAccessor));
            when(securityManagementService.getDeviceSecretImporter(keyAccessorType)).thenReturn(deviceSecretImporter);
            return keyAccessorType;
        }).collect(toList());
        when(deviceType.getSecurityAccessorTypes()).thenReturn(keyAccessorTypes);

        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService,Optional.of(importerExtension));
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("example-shipment-file-v1.5.xml"));

        secureDeviceShipmentImporter.process(fileImportOccurrence);

        verify(deviceSecretImporter, times(160)).importSecret(any(byte[].class), any(byte[].class), any(byte[].class), anyString(), anyString());
        List<String> logMessages = testHandler.getLogMessages();

        assertThat(logMessages).contains(MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY.getDefaultFormat());
        assertThat(logMessages).contains("Now importing device '00376280'");
        assertThat(logMessages).contains("Device '00376280' imported successfully");
        assertThat(logMessages).contains("Now importing device '00376281'");
        assertThat(logMessages).contains("Device '00376281' imported successfully");
        assertThat(logMessages).contains("Now importing device '00376282'");
        assertThat(logMessages).contains("Device '00376282' imported successfully");
        assertThat(logMessages).contains("Now importing device '00376283'");
        assertThat(logMessages).contains("Device '00376283' imported successfully");
        assertThat(logMessages).contains("Now importing device '00376284'");
        assertThat(logMessages).contains("Device '00376284' imported successfully");
    }

    @Test
    public void importBeaconShipmentFileWithUnknownDeviceType() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);

        when(deviceConfigurationService.findDeviceTypeByName("Beacon-3100/SM765")).thenReturn(Optional.empty());

        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService,Optional.of(importerExtension));
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("example-shipment-file-v1.5.xml"));

        try {
            secureDeviceShipmentImporter.process(fileImportOccurrence);
            fail("Importer should have failed for unknown device type");
        } catch (Exception e) {
            assertThat(e.getLocalizedMessage()).isEqualTo("Can't process file: the device type 'Beacon-3100/SM765' required by the importer couldn't be found");
        }

        List<String> logMessages = testHandler.getLogMessages();
        assertThat(logMessages).contains(MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY.getDefaultFormat());
        assertThat(logMessages).contains("Can't process file: the device type 'Beacon-3100/SM765' required by the importer couldn't be found");
    }

    @Test
    public void importBeaconShipmentFileWithoutDefaultDeviceConfig() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);

        when(deviceConfigurationService.findDeviceTypeByName("Beacon-3100/SM765")).thenReturn(Optional.empty());

        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService,Optional.of(importerExtension));
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("example-shipment-file-v1.5.xml"));

        try {
            secureDeviceShipmentImporter.process(fileImportOccurrence);
            fail("Importer should have failed for unknown device type");
        } catch (Exception e) {
            assertThat(e.getLocalizedMessage()).isEqualTo("Can't process file: the device type 'Beacon-3100/SM765' required by the importer couldn't be found");
        }

        List<String> logMessages = testHandler.getLogMessages();
        assertThat(logMessages).contains(MessageSeeds.SIGNATURE_OF_THE_SHIPMENT_FILE_VERIFIED_SUCCESSFULLY.getDefaultFormat());
        assertThat(logMessages).contains("Can't process file: the device type 'Beacon-3100/SM765' required by the importer couldn't be found");
    }

    @Test
    public void importShipmentFileMeters() throws Exception {
        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, securityManagementService,Optional.of(importerExtension));
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("Shipment file example - meters.xml"));

        try {
            secureDeviceShipmentImporter.process(fileImportOccurrence);
            fail("Expected XmlValidationFailedException");
        } catch (XmlValidationFailedException e) {
            // ok
        }
    }

    class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final String defaultFormat;

        SimpleNlsMessageFormat(TranslationKey translationKey) {
            this.defaultFormat = translationKey.getDefaultFormat();
        }

        SimpleNlsMessageFormat(com.elster.jupiter.util.exception.MessageSeed messageSeed) {
            this.defaultFormat = messageSeed.getDefaultFormat();
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(this.defaultFormat, args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(this.defaultFormat, args);
        }

    }

    private class TestHandler extends Handler {

        private List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

        public List<String> getLogMessages() {
            return records.stream().map(rec->rec.getMessage()).collect(toList());
        }
    }

}
