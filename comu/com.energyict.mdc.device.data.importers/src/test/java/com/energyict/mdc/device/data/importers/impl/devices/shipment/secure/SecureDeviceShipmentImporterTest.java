package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.DeviceKeyImporter;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
    PkiService pkiService;

    @Before
    public void setUp() throws Exception {
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((com.elster.jupiter.util.exception.MessageSeed) invocation.getArguments()[0]));
        when(thesaurus.getSimpleFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((com.elster.jupiter.util.exception.MessageSeed) invocation.getArguments()[0]));
        when(fileImportOccurrence.getLogger()).thenReturn(Logger.getLogger("tests"));
    }

    @Test
    public void importBeaconShipmentFile() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);

        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfig.getName()).thenReturn("Default");
        when(deviceConfig.isActive()).thenReturn(true);

        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfig));
        when(deviceConfigurationService.findDeviceTypeByName("Beacon-3100/SM765")).thenReturn(Optional.of(deviceType));

        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.empty());
        Device newDevice = mock(Device.class);
        when(newDevice.getDeviceType()).thenReturn(deviceType);
        when(deviceService.newDevice(eq(deviceConfig), anyString(), any(Instant.class))).thenReturn(newDevice);
        when(deviceService.newDevice(eq(deviceConfig), anyString(), anyString(), any(Instant.class))).thenReturn(newDevice);
        KeyAccessor keyAccessor = mock(KeyAccessor.class);
        when(keyAccessor.getActualValue()).thenReturn(Optional.empty());
        when(keyAccessor.getTempValue()).thenReturn(Optional.empty());
        DeviceKeyImporter deviceKeyImporter = (ek, iv, wk, sa, aa) -> {
            System.out.println(":"+sa + "\t" +aa);
            return null;
        };
        List<KeyAccessorType> keyAccessorTypes = Stream.of("NTP_HASH", "MK_DC", "EAP_PSK_DC", "WEB_PORTAL_LOGIN_RO", "WEB_PORTAL_LOGIN_RW",
                "WEB_PORTAL_LOGIN_ADMIN", "Priv_DC_SSH_cl", "Pub_DC_SSH_sv", "DLMS_WAN_DMGMT_MC_GUEK", "DLMS_WAN_DBROAD_MC_GUEK",
                "DLMS_WAN_DMGMT_RW_GUEK", "DLMS_WAN_DBROAD_RW_GUEK", "DLMS_WAN_DMGMT_FU_GUEK", "DLMS_WAN_DBROAD_FU_GUEK",
                "DLMS_WAN_DMGMT_MC_GAK", "DLMS_WAN_DBROAD_MC_GAK", "DLMS_WAN_DMGMT_RW_GAK", "DLMS_WAN_DBROAD_RW_GAK",
                "DLMS_WAN_DMGMT_FU_GAK", "DLMS_WAN_DBROAD_FU_GAK", "DLMS_WAN_DMGMT_MC_LLS", "DLMS_WAN_DBROAD_MC_LLS",
                "DLMS_WAN_DBROAD_MC_LLS", "DLMS_WAN_DMGMT_RW_LLS", "DLMS_WAN_DBROAD_RW_LLS", "DLMS_WAN_DMGMT_FU_LLS",
                "DLMS_WAN_DBROAD_FU_LLS", "DLMS_WAN_DMGMT_MC_HLS", "DLMS_WAN_DBROAD_MC_HLS", "DLMS_WAN_DMGMT_RW_HLS",
                "DLMS_WAN_DBROAD_RW_HLS", "DLMS_WAN_DMGMT_FU_HLS", "DLMS_WAN_DBROAD_FU_HLS").map(name -> {
            KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
            when(keyAccessorType.getName()).thenReturn(name);
            when(newDevice.getKeyAccessor(keyAccessorType)).thenReturn(Optional.of(keyAccessor));
            when(pkiService.getSymmetricKeyImporter(keyAccessorType)).thenReturn(deviceKeyImporter);
            return keyAccessorType;
        }).collect(toList());
        when(deviceType.getKeyAccessorTypes()).thenReturn(keyAccessorTypes);

        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, pkiService);
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("example-shipment-file-v1.5.xml"));
        secureDeviceShipmentImporter.process(fileImportOccurrence);
    }

    @Test
    public void importShipmentFileMeters() throws Exception {
        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore, deviceConfigurationService, deviceService, pkiService);
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("Shipment file example - meters.xml"));
        secureDeviceShipmentImporter.process(fileImportOccurrence);
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
}
