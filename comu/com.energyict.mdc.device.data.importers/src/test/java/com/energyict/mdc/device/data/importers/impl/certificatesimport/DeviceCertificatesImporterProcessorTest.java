package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportZipEntry;
import com.energyict.mdc.device.data.importers.impl.FileImportZipLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.zip.ZipFile;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCertificatesImporterProcessorTest {

    @Mock
    DeviceDataImporterContext context;

    @Mock
    DeviceService deviceService;

    @Mock
    FileImportZipLogger logger;

    @InjectMocks
    DeviceCertificatesImportProcessor processor;

    @Before
    public void beforeTest() {
        reset(deviceService,  logger);
        when(context.getDeviceService()).thenReturn(deviceService);
        when(deviceService.findDevicesBySerialNumber(anyString())).thenReturn(Collections.emptyList());

        processor = new DeviceCertificatesImportProcessor(context);
    }

    @Test
    public void testDeviceNotFound() {
        ZipFile zipFile = getZipFile("certificates.zip");
        FileImportZipEntry importZipEntry = getValidZipEntry();

        processor.process(zipFile, importZipEntry, logger);
        verify(logger).warning(MessageSeeds.NO_SERIAL_NUMBER, importZipEntry.getDirectory());
    }

    private ZipFile getZipFile(String fileName) {
        try {
            return new ZipFile(Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("com/energyict/mdc/device/data/importers/impl/" + fileName)
                    .getFile());
        } catch (Exception e) {
            return null;
        }
    }

    private FileImportZipEntry getValidZipEntry() {
        return new FileImportZipEntry("0105425037010016213415730002",
                "fileName", null, "tls-cert");
    }
}