package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesImportLogger;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesParser;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        doReturn(getClass()
                .getResource("/com/energyict/mdc/device/data/importers/impl/" + fileName)
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
        verify(spyParser).init(any(ZipFile.class));
        verify(spyParser).getZipEntries();
        verify(importOccurrence).markSuccess(anyString());
        verify(spyParser).init(Matchers.any(ZipFile.class));
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
        verify(logger).log(Matchers.eq(Level.SEVERE),Matchers.eq("error in opening zip file"), any(Exception.class));
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
