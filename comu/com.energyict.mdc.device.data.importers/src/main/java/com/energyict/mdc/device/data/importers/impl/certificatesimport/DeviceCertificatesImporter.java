package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeviceCertificatesImporter implements FileImporter {
    private final Thesaurus thesaurus;

    private volatile DeviceService deviceService;
    private volatile SecurityManagementService securityManagementService;

    DeviceCertificatesImporter(Thesaurus thesaurus, DeviceService deviceService, SecurityManagementService securityManagementService) {
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
        this.securityManagementService = securityManagementService;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try {
            ZipFile zipFile = new ZipFile(fileImportOccurrence.getPath());
            Predicate<ZipEntry> isFile = ze -> !ze.isDirectory();
            Comparator<DeviceCertificateInfo> bySerialNumber = Comparator.comparing(e -> e.getSerialNumber());
            Comparator<DeviceCertificateInfo> byCertificateName = Comparator.comparingInt(e -> e.getCertificateName().length());

            List<DeviceCertificateInfo> certificateInfoList = zipFile.stream()
                    .filter(isFile)
                    .map(zipEntry -> new DeviceCertificateInfo(zipEntry))
                    .sorted(bySerialNumber.thenComparing(byCertificateName))
                    .collect(Collectors.toList());


            certificateInfoList.forEach(deviceCertificateInfo -> {
                List<Device> devices = deviceService.findDevicesBySerialNumber(deviceCertificateInfo.getSerialNumber());

                if (!devices.isEmpty()) {
                    devices.forEach((Device device) -> {
                        List<SecurityAccessorType> accessorTypes = device.getDeviceType().getSecurityAccessorTypes();
                        List<SecurityAccessorType> accessorTypesByAlias = accessorTypes.stream().filter(x -> x.getName().startsWith(deviceCertificateInfo.getCertificateIdentifier())).collect(Collectors.toList());

                        if (accessorTypesByAlias.isEmpty()) {
                            System.out.println("no matching security accessors");
//                                Instead of "Can't process line X" use "Can't process certificate <<certificate name>>"
                        } else {
                            accessorTypesByAlias.forEach(securityAccessorType -> {

                                SecurityAccessor accessor;
                                if (device.getKeyAccessor(securityAccessorType).isPresent()) {
                                    accessor = device.getKeyAccessor(securityAccessorType).get();
                                } else {
                                    accessor = device.newKeyAccessor(securityAccessorType);
                                }

                                ClientCertificateWrapper wrapper = securityManagementService.newClientCertificateWrapper(securityAccessorType.getKeyType(),
                                        securityAccessorType.getKeyEncryptionMethod()).add();

                                CertificateFactory cf = null;
                                X509Certificate certificate = null;
                                try {
                                    cf = CertificateFactory.getInstance("X.509");
                                    certificate = (X509Certificate) cf.generateCertificate(zipFile.getInputStream(deviceCertificateInfo.getCertificate()));
                                } catch (CertificateException | IOException e) {
                                    e.printStackTrace();
                                }
                                wrapper.setCertificate(certificate);

                                if (!accessor.getActualValue().isPresent()) {
                                    accessor.setActualValue(wrapper);
                                } else if (!accessor.getTempValue().isPresent()) {
                                    if (accessor.getActualValue().isPresent()) {
                                        accessor.setTempValue(wrapper);
                                    }
                                }

                                accessor.save();
                            });
                        }
                    });
                } else {
                    System.out.println("Can't process certificate <<certificate name>>. The security key that starts with X is not available.");
                }
            });

            zipFile.close();

        } catch (Exception e) {
//            new ExceptionLogFormatter(thesaurus, fileImportOccurrence.getLogger()).log(e);
            throw new RuntimeException(thesaurus.getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_FAIL).format());
        }
    }

}
