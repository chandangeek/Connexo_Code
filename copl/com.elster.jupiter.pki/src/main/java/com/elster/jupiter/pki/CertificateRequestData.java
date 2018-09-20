package com.elster.jupiter.pki;

import javax.validation.constraints.NotNull;

public class CertificateRequestData {

    private final String caName;
    private final String endEntityName;
    private final String certificateProfileName;


    public CertificateRequestData(@NotNull  String caName, @NotNull String endEntityName, @NotNull String certificateProfileName) {
        this.caName = caName;
        this.endEntityName = endEntityName;
        this.certificateProfileName = certificateProfileName;
    }


    public String getCaName() {
        return caName;
    }

    public String getEndEntityName() {
        return endEntityName;
    }

    public String getCertificateProfileName() {
        return certificateProfileName;
    }
}
