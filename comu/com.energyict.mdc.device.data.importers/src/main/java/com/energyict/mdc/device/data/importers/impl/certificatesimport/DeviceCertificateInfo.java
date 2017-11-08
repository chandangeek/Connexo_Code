package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DeviceCertificateInfo {
    private String serialNumber;
    private String certificateIdentifier;
    private String certificateName;
    private ZipEntry certificate;

    private final Pattern nameExtraction = Pattern.compile(".*/(.+)\\-.+?");

    public DeviceCertificateInfo(ZipEntry zipEntry) {
        this.serialNumber = zipEntry.getName().split("/")[0];
        this.certificateName = zipEntry.getName().split("/")[1];
        this.certificateIdentifier = getCertificateIdentifier(zipEntry);
        this.certificate = zipEntry;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getCertificateIdentifier() {
        return this.certificateIdentifier;
    }

    public String getCertificateName() {
        return this.certificateName;
    }

    public ZipEntry getCertificate() {
        return this.certificate;
    }


    private String getCertificateIdentifier(ZipEntry entry) {
        Matcher matcher = nameExtraction.matcher(entry.getName());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
