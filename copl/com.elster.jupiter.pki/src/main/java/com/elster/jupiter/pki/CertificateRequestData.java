package com.elster.jupiter.pki;

import com.elster.jupiter.pki.impl.importers.csr.CSRImporterTranslatedProperty;

import java.util.Map;

public class CertificateRequestData {

    private final String caName;
    private final String endEntityName;
    private final String certificateProfileName;


    public CertificateRequestData(String caName, String endEntityName,String certificateProfileName) {
        if (isEmpty(caName) || isEmpty(endEntityName) || isEmpty(certificateProfileName)) {
            throw new RuntimeException("Invalid certificate request data ca name:" + caName + " end entity name:" + endEntityName + " profile name:" + certificateProfileName);
        }
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

    public static CertificateRequestData from(Map<String, Object> properties){
        return new CertificateRequestData((String)properties.get(CSRImporterTranslatedProperty.CA_NAME.getPropertyKey()),(String)properties.get(CSRImporterTranslatedProperty.CA_END_ENTITY_NAME.getPropertyKey()),(String) properties.get(CSRImporterTranslatedProperty.CA_PROFILE_NAME.getPropertyKey()));
    }

    private boolean isEmpty(String caName) {
        return caName == null || caName.isEmpty();
    }

}
