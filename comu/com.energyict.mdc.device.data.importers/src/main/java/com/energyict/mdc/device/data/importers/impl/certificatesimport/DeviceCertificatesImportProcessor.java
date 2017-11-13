package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.importers.impl.*;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.exceptions.ZipProcessorException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class DeviceCertificatesImportProcessor implements FileImportZipProcessor {

    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;
//    private volatile Thesaurus thesaurus;

//    private final Pattern securityAccessorTypeNameExtractor = Pattern.compile(".*/(.+)-.+?");

    public DeviceCertificatesImportProcessor(DeviceDataImporterContext deviceDataImporterContext) {
        securityManagementService = deviceDataImporterContext.getSecurityManagementService();
        deviceService = deviceDataImporterContext.getDeviceService();
//        thesaurus = deviceDataImporterContext.getThesaurus();
    }

    @Override
    public void process(ZipFile zipFile, FileImportZipEntry importZipEntry, FileImportZipLogger logger) {
        List<Device> devices = deviceService.findDevicesBySerialNumber(importZipEntry.getDirectory());
        try {
            for (Device device : devices) {
                for (SecurityAccessorType accessorType : findMatchingKeyAccessorTypes(importZipEntry, device, logger)) {
                    setKeyWithCertificate(zipFile, importZipEntry, device, accessorType);
                }
            }
        } catch (CertificateException | IOException e) {
            throw new ZipProcessorException(e);
        }
    }

    private void setKeyWithCertificate(ZipFile zipFile, FileImportZipEntry importZipEntry, Device device, SecurityAccessorType securityAccessorType) throws CertificateException, IOException {
        SecurityAccessor accessor = getKeyAccessor(device, securityAccessorType);
        ClientCertificateWrapper wrapper = getWrapper(importZipEntry, securityAccessorType, getX509Certificate(zipFile, importZipEntry));
        save(accessor, wrapper);
    }

    private void save(SecurityAccessor accessor, ClientCertificateWrapper wrapper) {
        boolean valueChange = false;
        if (!accessor.getActualValue().isPresent()) {
            accessor.setActualValue(wrapper);
            valueChange = true;
        } else if (!accessor.getTempValue().isPresent()) {
            if (accessor.getActualValue().isPresent()) {
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
        if (device.getKeyAccessor(securityAccessorType).isPresent()) {
            accessor = device.getKeyAccessor(securityAccessorType).get();
        } else {
            accessor = device.newKeyAccessor(securityAccessorType);
        }
        return accessor;
    }

    private X509Certificate getX509Certificate(ZipFile zipFile, FileImportZipEntry importZipEntry) throws CertificateException, IOException {
        CertificateFactory cf;
        X509Certificate certificate;
        cf = CertificateFactory.getInstance("X.509");
        certificate = (X509Certificate) cf.generateCertificate(zipFile.getInputStream(importZipEntry.getZipEntry()));
        return certificate;
    }

    private ClientCertificateWrapper getWrapper(FileImportZipEntry importZipEntry, SecurityAccessorType securityAccessorType, X509Certificate certificate) {
        String certificateAlias = getAlias(importZipEntry.getFileName());
        System.out.println("ca: " + certificateAlias);

        ClientCertificateWrapper wrapper = securityManagementService
                .findClientCertificateWrapper(getAlias(importZipEntry.getFileName()))
                .orElse(securityManagementService.newClientCertificateWrapper(securityAccessorType.getKeyType(), securityAccessorType.getKeyEncryptionMethod()).alias(certificateAlias).add());
        wrapper.setCertificate(certificate);
        return wrapper;
    }

    private String getAlias(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
    }

    private List<SecurityAccessorType> findMatchingKeyAccessorTypes(FileImportZipEntry importZipEntry, Device device, FileImportZipLogger logger) {
        List<SecurityAccessorType> securityAccessorTypes = device.getDeviceType()
                .getSecurityAccessorTypes().stream()
                .filter(x -> x.getName().startsWith(importZipEntry.getSecurityAccessorTypeName()))
                .collect(Collectors.toList());

        if (securityAccessorTypes.isEmpty()) {
            logger.warning(MessageSeeds.CERTIFICATE_NO_SUCH_KEY_ACCESSOR_TYPE, importZipEntry.getFileName());
        }

        return securityAccessorTypes;
    }
}
