package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesImportLogger;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.logging.Logger;
import java.util.zip.ZipFile;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceDataZipImporterTest {

    private String fileName;
    private DeviceCertificatesParser parser;

    @Mock
    private FileImportZipProcessor processor;

    @Mock
    private Logger logger;

    @Mock
    private DeviceDataImporterContext context;

    @Before
    public void beforeTest() {
        reset(processor, logger);
        Thesaurus thesaurus = mock(Thesaurus.class,RETURNS_MOCKS);
        when(thesaurus.getSimpleFormat(MessageSeeds.COULD_NOT_EXTRACT_SERIAL_NUMBER))
                .thenReturn(new SimpleNlsMessageFormat(MessageSeeds.COULD_NOT_EXTRACT_SERIAL_NUMBER));
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    public FileImportZipProcessor mockProcessor() {
        FileImportZipProcessor processor = mock(FileImportZipProcessor.class);
        return processor;
    }

    private FileImportOccurrence mockFileImportOccurrence() {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);

        doReturn(Thread.currentThread()
                .getContextClassLoader()
                .getResource("com/energyict/mdc/device/data/importers/impl/" + fileName)
                .getPath())
                .when(importOccurrence).getPath();

        return importOccurrence;
    }

    private DeviceDataZipImporter mockImporter(FileImportZipParser parser, FileImportZipProcessor processor) {
        return DeviceDataZipImporter.withParser(parser).withProcessor(processor).withLogger(new DeviceCertificatesImportLogger(context)).build();
    }


    @Test
    public void testMarkSuccessWhenInputIsZipFile() throws Exception {
        setFileName("certificates.zip");
        FileImportOccurrence importOccurrence = mockFileImportOccurrence();

        // execute parser logic
        parser = new DeviceCertificatesParser(context);
        DeviceCertificatesParser spyParser = spy(parser);

        DeviceDataZipImporter importer = mockImporter(spyParser, processor);

        importer.process(importOccurrence);

        verify(importOccurrence).getPath();
        verify(spyParser).init(any(ZipFile.class));
        verify(spyParser).getZipEntries();
        verify(importOccurrence).markSuccess(anyString());
    }

    @Test
    public void testExceptionIsThrownWhenInputIsZipFileWithMultiLevelDirectories() {
        setFileName("certificatesWithMultiLevelDirs.zip");
        FileImportOccurrence importOccurrence = mockFileImportOccurrence();

        parser = new DeviceCertificatesParser(context);
        DeviceCertificatesParser spyParser = spy(parser);

        DeviceDataZipImporter importer = mockImporter(spyParser, processor);

        importer.process(importOccurrence);

        verify(importOccurrence).getPath();
        verify(importOccurrence).markFailure(anyString());
        verify(spyParser).init(Matchers.any(ZipFile.class));
        verify(logger).severe(Matchers.startsWith("The device serial number could not be extracted"));
    }

    @Test
    public void testExceptionIsThrownWhenInputIsNotZipFile() {
        setFileName("certificatesWithInvalidTextFormat.zip");
        FileImportOccurrence importOccurrence = mockFileImportOccurrence();

        parser = new DeviceCertificatesParser(context);
        DeviceCertificatesParser spyParser = spy(parser);

        DeviceDataZipImporter importer = mockImporter(spyParser, processor);

        importer.process(importOccurrence);

        verify(importOccurrence).getPath();
        verify(importOccurrence).markFailure(anyString());
        verify(spyParser, never()).init(Matchers.any(ZipFile.class));
        verify(logger).severe("error in opening zip file");
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
