package com.energyict.mdc.device.data.importers;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.energyict.mdc.device.data.importers.impl.DeviceDataCsvImporter;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportParser;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportProcessor;
import com.energyict.mdc.device.data.importers.impl.readingsimport.DeviceReadingsImportRecord;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.when;

public class DeviceReadingsImporterTest {

    @Mock
    private FileImportOccurrence fileImportOccurence;

    @Mock
    private DeviceDataImporterContext context;

    public void test() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading value;\n" +
                "device;17/07/2015 11:43;0.0.0.0.0.0.0.0.0.0.0.0.0.0;100500;";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes());
        when(fileImportOccurence.getContents()).thenReturn(inputStream);

        DeviceDataCsvImporter<DeviceReadingsImportRecord> importer = DeviceDataCsvImporter.withParser(new DeviceReadingsImportParser("dd/MM/yyyy HH:mm", "GMT+03:00", SupportedNumberFormat.FORMAT1))
                .withProcessor(new DeviceReadingsImportProcessor())
                .withDelimiter(';')
                .build(context);

        importer.process(fileImportOccurence);
    }
}
