package com.elster.jupiter.pki.impl.importers;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.properties.PropertySpec;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.pki." + CertificateImporterFactory.NAME,
        service = FileImporterFactory.class,
        immediate = true)
public class CertificateImporterFactory implements FileImporterFactory {
    public static final String NAME = "CertificateImporterFactory";

    private volatile Thesaurus thesaurus;


    public CertificateImporterFactory() {
    }

    @Inject
    public CertificateImporterFactory(NlsService nlsService) {
        this();
        setNlsService(nlsService);
    }


    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new CertificateImporter(thesaurus);
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.CERTIFICATES_FILE_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return CertificateImporterMessageHandler.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return "SYS";
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        // No properties to validate
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(PkiService.COMPONENTNAME, Layer.DOMAIN);
    }
}