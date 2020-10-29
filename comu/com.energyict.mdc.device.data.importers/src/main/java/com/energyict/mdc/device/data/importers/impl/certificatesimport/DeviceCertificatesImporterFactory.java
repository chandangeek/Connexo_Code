package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.DeviceDataZipImporter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_CERTIFICATES_IMPORTER;


@Component(name = "com.energyict.mdc.device.data.importers.impl." + DeviceCertificatesImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class DeviceCertificatesImporterFactory extends AbstractDeviceDataFileImporterFactory {
    public static final String NAME = "DeviceCertificatesImporterFactory";

    private volatile DeviceDataImporterContext context;

    public DeviceCertificatesImporterFactory() {
    }

    @Inject
    public DeviceCertificatesImporterFactory(DeviceDataImporterContext context) {
        setDeviceDataImporterContext(context);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        DeviceCertificatesParser parser = new DeviceCertificatesParser(getContext());
        DeviceCertificatesImportProcessor processor = new DeviceCertificatesImportProcessor(getContext(), properties);
        DeviceCertificatesImportLogger logger = new DeviceCertificatesImportLogger(getContext());
        return DeviceDataZipImporter.withParser(parser).withProcessor(processor).withLogger(logger).build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return getContext().getThesaurus().getFormat(DEVICE_CERTIFICATES_IMPORTER).format();
    }

    @Override
    protected Set<DeviceDataImporterProperty> getProperties() {
        return EnumSet.of(
                DeviceDataImporterProperty.SECURITY_ACCESSOR_MAPPING,
                DeviceDataImporterProperty.SYSTEM_TITLE_PROPERTY_NAME);
    }

    @Override
    protected DeviceDataImporterContext getContext() {
        return this.context;
    }

    @Override
    @Reference
    public void setDeviceDataImporterContext(DeviceDataImporterContext context) {
        this.context = context;
    }
}
