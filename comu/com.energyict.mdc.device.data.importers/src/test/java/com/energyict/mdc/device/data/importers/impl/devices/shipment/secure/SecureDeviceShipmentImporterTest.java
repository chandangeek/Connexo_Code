package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.util.exception.MessageSeed;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
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
        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore);
        when(fileImportOccurrence.getContents()).thenReturn(this.getClass().getResourceAsStream("example-shipment-file-v1.5.xml"));
        secureDeviceShipmentImporter.process(fileImportOccurrence);
    }

    @Test
    public void importShipmentFileMeters() throws Exception {
        SecureDeviceShipmentImporter secureDeviceShipmentImporter = new SecureDeviceShipmentImporter(thesaurus, trustStore);
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
