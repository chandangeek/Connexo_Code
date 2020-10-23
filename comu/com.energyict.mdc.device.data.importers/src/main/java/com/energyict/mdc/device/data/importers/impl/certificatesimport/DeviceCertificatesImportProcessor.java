package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterProperty;
import com.energyict.mdc.device.data.importers.impl.FileImportZipEntry;
import com.energyict.mdc.device.data.importers.impl.FileImportZipLogger;
import com.energyict.mdc.device.data.importers.impl.FileImportZipProcessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.exceptions.ZipProcessorException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class DeviceCertificatesImportProcessor implements FileImportZipProcessor {

    private final Thesaurus thesaurus;
    private Optional<JSONObject> mappingJson = null;
    private final Map<String, Object> properties;

    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;

    public DeviceCertificatesImportProcessor(DeviceDataImporterContext deviceDataImporterContext, Map<String, Object> properties) {
        securityManagementService = deviceDataImporterContext.getSecurityManagementService();
        deviceService = deviceDataImporterContext.getDeviceService();
        thesaurus = deviceDataImporterContext.getThesaurus();
        this.properties = properties;
    }

    @Override
    public void process(ZipFile zipFile, FileImportZipEntry importZipEntry, FileImportZipLogger logger) {
        if (skipThisEntry(importZipEntry, logger)){
            logger.info(MessageSeeds.SKIPPING_ENTRY, importZipEntry.getFileName());
            return;
        }

        X509Certificate certificate;
        try {
            // preload the zip entry, so can be processed without accessing the zip several times;
            certificate = getX509Certificate(zipFile, importZipEntry);
        } catch (Exception ex){
            logger.warning(MessageSeeds.FAILED_TO_CREATE_CERTIFICATE, importZipEntry.getFileName());
            return;
        }

        // first try to find host device by CN - the proper way!
        List<Device> devices = findDeviceByCommonName(certificate, importZipEntry, logger);
        if (devices.isEmpty()){
            // fall-back to legacy director-based naming
            devices = deviceService.findDevicesBySerialNumber(importZipEntry.getDirectory());
        }
        if (!devices.isEmpty()) {
            importCertificate(zipFile, importZipEntry, certificate, logger, devices);
        } else {
            logger.warning(MessageSeeds.NO_SERIAL_NUMBER, importZipEntry.getFileName(), importZipEntry.getDirectory());
        }
    }

    private boolean skipThisEntry(FileImportZipEntry importZipEntry, FileImportZipLogger logger) {
        if (!getMappingJson(logger).isPresent()){
            return false;
        }

        Iterator keys = getMappingJson(logger).get().keys();
        while (keys.hasNext()) {
            String fileNamePrefix = (String) keys.next();
            if (importZipEntry.getSecurityAccessorTypeName().startsWith(fileNamePrefix)) {
                try {
                    Object mappingSetting = getMappingJson(logger).get().get(fileNamePrefix);
                    if (mappingSetting instanceof Boolean) {
                        /**
                         * The CA trust-chain shouldn't be re-imported, so if it's a boolean,
                         * should be only "false" ... but people are inventive ...
                         */
                        return true;
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        return false;
    }

    private List<Device> findDeviceByCommonName(X509Certificate certificate, FileImportZipEntry importZipEntry, FileImportZipLogger logger) {
        try {
            Optional<String> cn = extractCommonName(certificate);
            if (cn.isPresent()){
                List<Device> devices = deviceService.findDevicesByPropertySpecValue(getSystemTitlePropertyName(), cn.get());
                if (devices.isEmpty()){
                    logger.warning(MessageSeeds.CANNOT_FIND_DEVICE_WITH_COMMON_NAME, cn.get(), getSystemTitlePropertyName(), importZipEntry.getFileName());
                }
                return devices;
            } else {
                logger.warning(MessageSeeds.CANNOT_EXTRACT_COMMON_NAME, importZipEntry.getFileName(), cn.get());
            }
        } catch (Exception e) {
            logger.warning(MessageSeeds.CANNOT_EXTRACT_COMMON_NAME, importZipEntry.getFileName(), e.getLocalizedMessage());
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private String getSystemTitlePropertyName() {
        return (String)getProperties().get(DeviceDataImporterProperty.SYSTEM_TITLE_PROPERTY_NAME.getPropertyKey());
    }

    private Optional<String> extractCommonName(X509Certificate certificate) throws InvalidNameException {
        String dn = certificate.getSubjectDN().getName();
        LdapName ldapName = null;
        ldapName = new LdapName(dn);
        for(Rdn rdn : ldapName.getRdns()) {
            if(rdn.getType().equalsIgnoreCase("CN")) {
                return Optional.of( rdn.getValue().toString() );
            }
        }

        return Optional.empty();
    }

    private void importCertificate(ZipFile zipFile, FileImportZipEntry importZipEntry, X509Certificate certificate, FileImportZipLogger logger, List<Device> devices) {
        try {
            for (Device device : devices) {
                for (SecurityAccessorType accessorType : findMatchingKeyAccessorTypes(importZipEntry, device, logger)) {
                    logger.info(MessageSeeds.LINKING_CERTIFICATE, accessorType.getName(), device.getName(), importZipEntry.getFileName());
                    setKeyWithCertificate(certificate, importZipEntry, device, accessorType);
                }
            }
        } catch (CertificateException | IOException e) {
            throw new ZipProcessorException(e);
        }
    }

    private void setKeyWithCertificate(X509Certificate certificate, FileImportZipEntry importZipEntry, Device device, SecurityAccessorType securityAccessorType) throws CertificateException, IOException {
        SecurityAccessor accessor = getKeyAccessor(device, securityAccessorType);

        ClientCertificateWrapper wrapper = getWrapper(importZipEntry, securityAccessorType, certificate);
        save(accessor, wrapper);
    }

    private void save(SecurityAccessor accessor, ClientCertificateWrapper wrapper) {
        boolean valueChange = false;
        if (!accessor.getActualPassphraseWrapperReference().isPresent()) {
            accessor.setActualPassphraseWrapperReference(wrapper);
            valueChange = true;
        } else if (!accessor.getTempValue().isPresent()) {
            if (accessor.getActualPassphraseWrapperReference().isPresent()) {
                accessor.setTempValue(wrapper);
                valueChange = true;
            }
        }

        if (valueChange) {
            accessor.save();
        }
    }

    private SecurityAccessor getKeyAccessor(Device device, SecurityAccessorType securityAccessorType) {
        SecurityAccessor accessor;
        if (device.getSecurityAccessor(securityAccessorType).isPresent()) {
            accessor = device.getSecurityAccessor(securityAccessorType).get();
        } else {
            accessor = device.newSecurityAccessor(securityAccessorType);
        }
        return accessor;
    }

    protected X509Certificate getX509Certificate(ZipFile zipFile, FileImportZipEntry importZipEntry) throws CertificateException, IOException {
        CertificateFactory cf;
        X509Certificate certificate;
        cf = CertificateFactory.getInstance("X.509");
        certificate = (X509Certificate) cf.generateCertificate(zipFile.getInputStream(importZipEntry.getZipEntry()));
        return certificate;
    }

    private ClientCertificateWrapper getWrapper(FileImportZipEntry importZipEntry, SecurityAccessorType securityAccessorType, X509Certificate certificate) {
        String certificateAlias = getAlias(importZipEntry.getFileName());

        ClientCertificateWrapper wrapper;
        if (securityManagementService.findClientCertificateWrapper(certificateAlias).isPresent()) {
            wrapper = securityManagementService.findClientCertificateWrapper(certificateAlias).get();
        } else {
            wrapper = securityManagementService.newClientCertificateWrapper(securityAccessorType.getKeyType(), securityAccessorType.getKeyEncryptionMethod()).alias(certificateAlias).add();
        }
        wrapper.setCertificate(certificate, Optional.empty());
        return wrapper;
    }

    private String getAlias(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
    }

    private List<SecurityAccessorType> findMatchingKeyAccessorTypes(FileImportZipEntry importZipEntry, Device device, FileImportZipLogger logger) {
        if (isMappingEnabled(logger)){
            return findMappedSecurityAccessor(importZipEntry, device, logger);
        } else {
            return findPrefixedSecurityAccessor(importZipEntry, device, logger);
        }
    }


    /**
     * Will look for the importZipEntry in the JSON mapping and will find the corresponding security-accessor name.
     *
     * @param importZipEntry
     * @param device
     * @param logger
     * @return
     */
    private List<SecurityAccessorType> findMappedSecurityAccessor(FileImportZipEntry importZipEntry, Device device, FileImportZipLogger logger) {
        List<SecurityAccessorType> securityAccessorTypes = Collections.emptyList();

        Iterator keys = getMappingJson(logger).get().keys();
        while (keys.hasNext()) {
            String fileNamePrefix = (String) keys.next();
            if (importZipEntry.getSecurityAccessorTypeName().startsWith(fileNamePrefix)){
                try {
                    Object mappingSetting = getMappingJson(logger).get().get(fileNamePrefix);

                    // Option 1 -> a boolean (should be only false), to skip it
                    // filename: false
                    if(mappingSetting instanceof Boolean){
                        // should already be skipped, but just to be safe in case somebody refactors
                        return Collections.emptyList();
                    }

                    // Option 2 -> an array of strings, parse them all
                    // filename: { "option1", "option2" .... }
                    if (mappingSetting instanceof JSONArray){
                        JSONArray possibleAccessorNames = getMappingJson(logger).get().getJSONArray(fileNamePrefix);
                        for (int i=0; i< possibleAccessorNames.length(); i++){
                            String securityAccessorName = possibleAccessorNames.getString(i);
                            List<SecurityAccessorType> foundAccessors = findSecurityAccessor(device, securityAccessorName);
                            securityAccessorTypes = Stream.concat(
                                    securityAccessorTypes.stream(),
                                    foundAccessors.stream())
                                    .collect(Collectors.toList());
                        }
                    }

                    // Option 3 -> simple name, just use it
                    if (mappingSetting instanceof String) {
                        String securityAccessorName = getMappingJson(logger).get().getString(fileNamePrefix);
                        securityAccessorTypes = findSecurityAccessor(device, securityAccessorName);
                    }
                } catch (JSONException e) {
                    logger.importFailed(e);
                }
            }
        }
        if (securityAccessorTypes.isEmpty()) {
            logger.warning(MessageSeeds.CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE, importZipEntry.getFileName(), importZipEntry.getSecurityAccessorTypeName());
        }
        return securityAccessorTypes;
    }

    /**
     * Will return the security accessors of a particular device which have the specified name.
     * Typically this should be only one!
     *
     * @param device
     * @param securityAccessorName
     * @return
     */
    private List<SecurityAccessorType> findSecurityAccessor(Device device, String securityAccessorName) {
        return device.getDeviceType()
                .getDeviceSecurityAccessorType().stream()
                .filter( x -> x.getSecurityAccessor().getName().equals(securityAccessorName) )
                .map( dsa -> dsa.getSecurityAccessor())
                .collect(Collectors.toList());
    }

    /**
     * Legacy hard-coded mapping using file-name as a prefix.
     * In some contexts this is not working, because there are restrictions on security accessor name,
     * so the JSON mapping from findMappedSecurityAccessor() will add more flexibility.
     *
     * @param importZipEntry
     * @param device
     * @param logger
     * @return
     */
    @Deprecated
    private List<SecurityAccessorType> findPrefixedSecurityAccessor(FileImportZipEntry importZipEntry, Device device, FileImportZipLogger logger) {
        List<SecurityAccessorType> securityAccessorTypes = device.getDeviceType()
                .getSecurityAccessorTypes().stream()
                .filter(x -> x.getName().startsWith(importZipEntry.getSecurityAccessorTypeName()))
                .collect(Collectors.toList());

        if (securityAccessorTypes.isEmpty()) {
            logger.warning(MessageSeeds.CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE, importZipEntry.getFileName());
        }

        return securityAccessorTypes;
    }

    public Map<String,Object> getProperties() {
        return properties;
    }

    private Optional<JSONObject> getMappingJson(FileImportZipLogger logger) {
        if (Objects.nonNull(this.mappingJson)){
            return this.mappingJson;
        }
        String jsonProperty = (String) getProperties().get(DeviceDataImporterProperty.SECURITY_ACCESSOR_MAPPING.getPropertyKey());
        if (Objects.isNull(jsonProperty) || jsonProperty.isEmpty()){
            return Optional.empty();
        }
        try{
            this.mappingJson = Optional.of( new JSONObject(jsonProperty) );
        }catch (Exception ex){
            logger.warning(MessageSeeds.INVALID_JSON, DeviceDataImporterProperty.SECURITY_ACCESSOR_MAPPING);
            this.mappingJson = Optional.empty();
        }

        return this.mappingJson;
    }

    private boolean isMappingEnabled(FileImportZipLogger logger) {
        return getMappingJson(logger).isPresent();
    }

}
