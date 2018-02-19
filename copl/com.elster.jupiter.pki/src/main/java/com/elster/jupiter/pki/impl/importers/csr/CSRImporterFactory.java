/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.pki.impl.importers.csr.CSRImporterFactory",
        service = FileImporterFactory.class,
        immediate = true)
public class CSRImporterFactory implements FileImporterFactory {
    public static final String NAME = "CSRImporterFactory";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile SecurityManagementService securityManagementService;
    private volatile CaService caService;
    private volatile FtpClientService ftpClientService;

    public CSRImporterFactory() {
    }

    @Inject
    public CSRImporterFactory(NlsService nlsService,
                              PropertySpecService propertySpecService,
                              SecurityManagementService securityManagementService) {
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setSecurityManagementService(securityManagementService);
    }

    @Override
    public FileImporter createImporter(Map<String, Object> properties) {
        return new CSRImporter(thesaurus, properties, securityManagementService, caService);
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(TranslationKeys.CSR_IMPORTER).format();
    }

    @Override
    public String getDestinationName() {
        return CSRImporterMessageHandlerFactory.DESTINATION_NAME;
    }

    @Override
    public String getApplicationName() {
        return "SYS";
    }

    @Override
    public void validateProperties(List<FileImporterProperty> properties) {
        // TODO: validate properties
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        // TODO: add a check for certificate usage as import/export certificate somewhere
        return Arrays.asList(
                propertySpecService.referenceSpec(SecurityAccessor.class)
                        .named(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey(), CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR)
                        .describedAs(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .markExhaustive()
                        .addValues(securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS))
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_CERTIFICATES)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(false)
                        .finish(),
                propertySpecService.referenceSpec(SecurityAccessor.class)
                        .named(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markExhaustive()
                        .addValues(securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS))
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_HOSTNAME.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_HOSTNAME)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_HOSTNAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.longSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_PORT.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_PORT)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_PORT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_USER.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_USER)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_USER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_PASSWORD.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_PASSWORD)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_PASSWORD_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_FILE_NAME.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_FILE_NAME)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_FILE_NAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_FILE_EXTENSION_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_FILE_LOCATION_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, new SecurityManagementServiceImpl().getLayer());
    }

    @Reference
    public final void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public final void setCaService(CaService caService) {
        this.caService = caService;
    }

    @Reference
    public void setFtpClientService(FtpClientService ftpClientService) {
        this.ftpClientService = ftpClientService;
    }
}
