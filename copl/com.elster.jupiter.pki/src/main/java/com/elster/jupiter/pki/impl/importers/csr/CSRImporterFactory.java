/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private volatile Clock clock;

    public CSRImporterFactory() {
    }

    @Inject
    public CSRImporterFactory(NlsService nlsService,
                              PropertySpecService propertySpecService,
                              SecurityManagementService securityManagementService,
                              FtpClientService ftpClientService,
                              Clock clock) {
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setSecurityManagementService(securityManagementService);
        setFtpClientService(ftpClientService);
        setClock(clock);
    }

    @Override
    public CSRImporter createImporter(Map<String, Object> properties) {
        return new CSRImporter(properties, thesaurus, securityManagementService, caService, ftpClientService, clock);
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
        properties.forEach(prop -> {
            if (prop.getName().equals(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey())) {
                TimeDuration timeout = (TimeDuration) prop.getValue();
                if (timeout != null && timeout.isEmpty()) {
                    throw new LocalizedFieldValidationException(MessageSeeds.POSITIVE_VALUE_IS_REQUIRED, prop.getName()).fromSubField("properties");
                }
            } else if (prop.getName().equals(CSRImporterTranslatedProperty.EXPORT_SFTP_PORT.getPropertyKey())) {
                Long port = (Long) prop.getValue();
                if (port != null && port <= 0) {
                    throw new LocalizedFieldValidationException(MessageSeeds.POSITIVE_VALUE_IS_REQUIRED, prop.getName()).fromSubField("properties");
                }
            } else if (prop.getName().equals(CSRImporterTranslatedProperty.CSR_MAPPING.getPropertyKey())) {
                validateJsonProperty(prop);
            } else if (prop.getName().equals(CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING.getPropertyKey())) {
                validateJsonProperty(prop);
                validateExistingAliases(prop);
            }
        });
    }

    private void validateExistingAliases(FileImporterProperty prop) {
        if (Objects.isNull(prop)){
            return;
        }

        String propertyValue = (String)prop.getValue();
        if (propertyValue.isEmpty()){
            return;
        }

        JSONObject jsonMapping = null;
        try {
            jsonMapping = new JSONObject((String)prop.getValue());
        } catch (JSONException e) {
            // redundant, should already by checked in validation
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, prop.getName()).fromSubField("properties");
        }
        Iterator keys = jsonMapping.keys();

        while (keys.hasNext()) {
            String connexoAlias = (String) keys.next();

            Condition condition = Where.where("alias").isEqualTo(connexoAlias);
            Finder<CertificateWrapper> certificateWrapperFinder = securityManagementService.findCertificateWrappers(condition).paged(0,2);
            List<CertificateWrapper> foundAliases = certificateWrapperFinder.find();
            if (foundAliases.size()==1){
                CertificateWrapper certificateWrapper = foundAliases.get(0);
                getLogger().info("Found certificate alias: " + certificateWrapper.getAlias() + " = " + certificateWrapper.getSubject());
            }
            if (foundAliases.size()>1){
                throw new LocalizedFieldValidationException(MessageSeeds.ALIAS_NOT_UNIQUE_EXCEPTION, connexoAlias).fromSubField("properties");
            }
            if (foundAliases.size()==0){
                throw new LocalizedFieldValidationException(MessageSeeds.ALIAS_NOT_FOUND_EXCEPTION, connexoAlias).fromSubField("properties");
            }
        }
    }

    private void validateJsonProperty(FileImporterProperty prop) {
        String jsonString = (String) prop.getValue();
        if (jsonString.isEmpty()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(jsonString);
            getLogger().info(prop.getName() + ": JSON decoded successfully: " + jsonString);
        } catch (Exception ex) {
            getLogger().severe(prop.getName() + ": Cannot parse file mapping JSON: " + jsonString + "\r\n" + ex.getLocalizedMessage());
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, prop.getName()).fromSubField("properties");
        }
    }

    private Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        // TODO: think of support for dependent properties
        List<SecurityAccessor> securityAccessors = securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS);
        List<SecurityAccessor> securityAccessorsForSignatureCheck = securityAccessors.stream()
                .filter(forSignatureCheck())
                .collect(Collectors.toList());
        List<SecurityAccessor> securityAccessorsForSigning = securityAccessorsForSignatureCheck.stream()
                .filter(forSigning())
                .collect(Collectors.toList());
        return Arrays.asList(
                // TODO: is it possible to show this as list of values?
                propertySpecService.timeDurationSpec()
                        .named(CSRImporterTranslatedProperty.TIMEOUT.getPropertyKey(), CSRImporterTranslatedProperty.TIMEOUT)
                        .describedAs(CSRImporterTranslatedProperty.TIMEOUT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
//                        .markExhaustive()
//                        .addValues(TimeDuration.seconds(30), TimeDuration.minutes(1), TimeDuration.minutes(2), TimeDuration.minutes(5))
                        .setDefaultValue(TimeDuration.seconds(30))
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.CHECK_FILE_SIGNATURE.getPropertyKey(), CSRImporterTranslatedProperty.CHECK_FILE_SIGNATURE)
                        .describedAs(CSRImporterTranslatedProperty.CHECK_FILE_SIGNATURE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.referenceSpec(SecurityAccessor.class)
                        .named(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR.getPropertyKey(), CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR)
                        .describedAs(CSRImporterTranslatedProperty.IMPORT_SECURITY_ACCESSOR_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .markExhaustive()
                        .addValues(securityAccessorsForSignatureCheck)
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.SAVE_CERTIFICATE.getPropertyKey(), CSRImporterTranslatedProperty.SAVE_CERTIFICATE)
                        .describedAs(CSRImporterTranslatedProperty.SAVE_CERTIFICATE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(false)
                        .finish(),
                propertySpecService.referenceSpec(SecurityAccessor.class)
                        .named(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SECURITY_ACCESSOR_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markExhaustive()
                        .addValues(securityAccessorsForSigning)
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING.getPropertyKey(),
                        CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING)
                        .describedAs(CSRImporterTranslatedProperty.CLIENT_TRUSTSTORE_MAPPING_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_FOLDER.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_FOLDER)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_FOLDER_DESCRIPTION)
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
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_FLAT_DIR.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_FLAT_DIR)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_FLAT_DIR_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.booleanSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_CERTIFICATES_SFTP_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_SFTP_HOSTNAME.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SFTP_HOSTNAME)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SFTP_HOSTNAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.longSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_SFTP_PORT.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SFTP_PORT)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SFTP_PORT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_SFTP_USER.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SFTP_USER)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SFTP_USER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec()
                        .named(CSRImporterTranslatedProperty.EXPORT_SFTP_PASSWORD.getPropertyKey(), CSRImporterTranslatedProperty.EXPORT_SFTP_PASSWORD)
                        .describedAs(CSRImporterTranslatedProperty.EXPORT_SFTP_PASSWORD_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey(), CSRImporterTranslatedProperty.CA_NAME)
                        .describedAs(CSRImporterTranslatedProperty.CA_NAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey(), CSRImporterTranslatedProperty.CA_END_ENTITY_NAME)
                        .describedAs(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey(), CSRImporterTranslatedProperty.CA_PROFILE_NAME)
                        .describedAs(CSRImporterTranslatedProperty.CA_PROFILE_NAME_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.SUBJECT_DN_FIELDS.getPropertyKey(), CSRImporterTranslatedProperty.SUBJECT_DN_FIELDS)
                        .describedAs(CSRImporterTranslatedProperty.SUBJECT_DN_FIELDS_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService.stringSpec().named(CSRImporterTranslatedProperty.CSR_MAPPING.getPropertyKey(), CSRImporterTranslatedProperty.CSR_MAPPING)
                        .describedAs(CSRImporterTranslatedProperty.CSR_MAPPING_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private Predicate<SecurityAccessor> forSignatureCheck() {
        return securityAccessor -> {
            Optional actual = securityAccessor.getActualPassphraseWrapperReference();
            if (actual.isPresent()) {
                Object object = actual.get();
                if (object instanceof CertificateWrapper) {
                    Optional<X509Certificate> certificateOptional = ((CertificateWrapper) object).getCertificate();
                    if (certificateOptional.isPresent()) {
                        PublicKey publicKey = certificateOptional.get().getPublicKey();
                        if (CSRImporter.isSupportedType(publicKey)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        };
    }

    private Predicate<SecurityAccessor> forSigning() {
        return securityAccessor -> {
            try {
                Optional actual = securityAccessor.getActualPassphraseWrapperReference();
                if (actual.isPresent()) {
                    Object object = actual.get();
                    if (object instanceof CertificateWrapper && ((CertificateWrapper) object).hasPrivateKey() && object instanceof ClientCertificateWrapper) {
                        Optional<PrivateKey> privateKeyOptional = ((ClientCertificateWrapper) object).getPrivateKeyWrapper().getPrivateKey();
                        if (privateKeyOptional.isPresent()) {
                            PrivateKey privateKey = privateKeyOptional.get();
                            if (CSRImporter.isSupportedType(privateKey)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (InvalidKeyException e) {
                // return false
            }
            return false;
        };
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

    @Reference
    public void setClock(Clock clock){
        this.clock = clock;
    }

    @Reference
    public void setSoftwareSecurityDataModel(SoftwareSecurityDataModel ignored) {
        // just wait for security data model to start
    }
}
