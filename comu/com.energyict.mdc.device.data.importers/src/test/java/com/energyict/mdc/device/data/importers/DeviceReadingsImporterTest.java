package com.energyict.mdc.device.data.importers;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportParser;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportProcessor;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceReadingsImporterTest {

    @Mock
    private FileImportOccurrence fileImportOccurence;


    @Test
    public void test() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading value;\n" +
                     "device;17/07/2015 11:43;0.0.0.0.0.0.0.0.0.0.0.0.0.0;100500;";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes());
        when(fileImportOccurence.getContents()).thenReturn(inputStream);

        DeviceDataCsvImporter<DeviceReadingsImportRecord> importer = new DeviceDataCsvImporter<DeviceReadingsImportRecord>(
                ';',
                new DeviceReadingsImportParser("dd/MM/yyyy HH:mm", "GMT+03:00", SupportedNumberFormat.FORMAT1),
                new DeviceReadingsImportProcessor());
        importer.process(fileImportOccurence);
    }
}
