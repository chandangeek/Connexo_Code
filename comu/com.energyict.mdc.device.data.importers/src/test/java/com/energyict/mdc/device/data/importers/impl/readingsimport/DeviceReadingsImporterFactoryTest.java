package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DeviceReadingsImporterFactoryTest {

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
    private DeviceImportService deviceImportService;
    @Mock
    private Logger logger;

    public void test() {
        String csv = "Device MRID;Reading date;Reading type MRID;Reading value;\n" +
                "device;17/07/2015 11:43;0.0.0.0.0.0.0.0.0.0.0.0.0.0;100500;";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes());
//        when(fileImportOccurence.getContents()).thenReturn(inputStream);

//        DeviceDataCsvImporter<DeviceReadingsImportRecord> importer = DeviceDataCsvImporter.withParser(new DeviceReadingsImportParser("dd/MM/yyyy HH:mm", "GMT+03:00", SupportedNumberFormat.FORMAT1))
//                .withProcessor(new DeviceReadingsImportProcessor())
//                .withDelimiter(';')
//                .build(context);

//        importer.process(fileImportOccurence);
    }
}
