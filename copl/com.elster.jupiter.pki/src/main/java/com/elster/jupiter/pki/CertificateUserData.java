package com.elster.jupiter.pki;

import javax.validation.constraints.NotNull;

public class CertificateUserData {

    private final String caName;
    private final String endEntityProfileName;
    private final String certificateProfileName;


    public CertificateUserData(@NotNull  String caName,@NotNull String endEntityProfileName,@NotNull String certificateProfileName) {
        this.caName = caName;
        this.endEntityProfileName = endEntityProfileName;
        this.certificateProfileName = certificateProfileName;
    }


    public String getCaName() {
        return caName;
    }

    public String getEndEntityProfileName() {
        return endEntityProfileName;
    }

    public String getCertificateProfileName() {
        return certificateProfileName;
    }
}
