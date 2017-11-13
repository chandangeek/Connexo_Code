package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportZipEntry;
import com.energyict.mdc.device.data.importers.impl.FileImportZipParser;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeviceCertificatesParser implements FileImportZipParser {

    private volatile ZipFile zipFile;
    private volatile Thesaurus thesaurus;

    public DeviceCertificatesParser(DeviceDataImporterContext deviceDataImporterContext) {
        this.thesaurus = deviceDataImporterContext.getThesaurus();
    }

    @Override
    public void init(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public Stream<FileImportZipEntry> getZipEntries() {
        return sortZipEntriesBySubDirectoryAndCertificateName();
    }

//    @Override
//    public FileImportZipEntry parse(ZipEntry zipEntry) throws FileImportParserException {
//        DeviceCertificateInfo info = new DeviceCertificateInfo(zipEntry);
//        return new FileImportZipEntry(info.getSerialNumber(), info.getCertificateName(), info.getCertificate());
//    }

    private Stream<FileImportZipEntry> sortZipEntriesBySubDirectoryAndCertificateName() {
        Predicate<ZipEntry> isFile = ze -> !ze.isDirectory();
        Comparator<DeviceCertificateInfo> bySerialNumber = Comparator.comparing(e -> e.getSerialNumber());
        Comparator<DeviceCertificateInfo> byCertificateName = Comparator.comparingInt(e -> e.getSecurityAccessorType().length());

        return zipFile.stream()
                .filter(isFile)
                .map(zipEntry -> new DeviceCertificateInfo(thesaurus, zipEntry))
                .sorted(bySerialNumber.thenComparing(byCertificateName))
                .map(deviceCertificateInfo -> new FileImportZipEntry(deviceCertificateInfo.getSerialNumber(),
                        deviceCertificateInfo.getCertificateName(), deviceCertificateInfo.getCertificate(),
                        deviceCertificateInfo.getSecurityAccessorType()));
    }
}
