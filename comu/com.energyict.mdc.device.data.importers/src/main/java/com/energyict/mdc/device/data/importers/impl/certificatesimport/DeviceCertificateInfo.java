package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.exceptions.ZipFieldParserException;
import com.google.common.base.CharMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DeviceCertificateInfo {
    private final Pattern securityAccessorTypeExtractor = Pattern.compile(".*/(.+)-.+?");
    private final Pattern certificateNameExtractor = Pattern.compile(".*/(.+\\.[^.]+)$");

    private final String serialNumber;
    private final String securityAccessorType;
    private final String certificateName;
    private final ZipEntry certificate;
    private final Thesaurus thesaurus;


    public DeviceCertificateInfo(Thesaurus thesaurus, ZipEntry zipEntry) {
        this.thesaurus = thesaurus;
        this.serialNumber = extractSerialNumber(zipEntry);
        this.certificateName = extractCertificateName(zipEntry);
        this.securityAccessorType = extractSecurityAccessorType(zipEntry);
        this.certificate = zipEntry;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getCertificateName() {
        return this.certificateName;
    }

    public String getSecurityAccessorType() {
        return this.securityAccessorType;
    }

    public ZipEntry getCertificate() {
        return this.certificate;
    }

    private String extractSerialNumber(ZipEntry entry) {
        if (CharMatcher.is('/').countIn(entry.getName()) == 1) {
            return entry.getName().split("/")[0];
        } else {
            throw new ZipFieldParserException(thesaurus, MessageSeeds.COULD_NOT_EXTRACT_SERIAL_NUMBER, entry.getName());
        }
    }

    private String extractCertificateName(ZipEntry entry) {
        Matcher matcher = certificateNameExtractor.matcher(entry.getName());
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new ZipFieldParserException(thesaurus, MessageSeeds.COULD_NOT_EXTRACT_CERTIFICATE_NAME, entry.getName());
        }
    }

    private String extractSecurityAccessorType(ZipEntry entry) {
        Matcher matcher = securityAccessorTypeExtractor.matcher(entry.getName());
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new ZipFieldParserException(thesaurus, MessageSeeds.COULD_NOT_EXTRACT_SECURITY_ACCESSOR_TYPE, entry.getName());
        }
    }
}
