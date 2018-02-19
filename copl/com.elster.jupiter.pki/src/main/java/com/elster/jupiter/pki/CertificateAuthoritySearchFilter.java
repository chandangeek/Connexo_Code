package com.elster.jupiter.pki;

import java.math.BigInteger;

/**
 * Serves mainly as struct to hold certificate identifiers, without the need to obtain the actual certificate. This
 * struct will serve as search filter.
 */
public class CertificateAuthoritySearchFilter {
    private BigInteger serialNumber;
    private String issuerDN;
    private String subjectDN;

    public CertificateAuthoritySearchFilter() {
    }

    public CertificateAuthoritySearchFilter(BigInteger serialNumber, String issuerDN, String subjectDN) {
        this.serialNumber = serialNumber;
        this.issuerDN = issuerDN;
        this.subjectDN = subjectDN;
    }

    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getIssuerDN() {
        return issuerDN;
    }

    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }

    public String getSubjectDN() {
        return subjectDN;
    }

    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    //Have to implement equals/hashCode to properly test async functionality
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CertificateAuthoritySearchFilter filter = (CertificateAuthoritySearchFilter) o;

        return (serialNumber != null ? serialNumber.equals(filter.serialNumber) : filter.serialNumber == null) &&
                (issuerDN != null ? issuerDN.equals(filter.issuerDN) : filter.issuerDN == null) &&
                (subjectDN != null ? subjectDN.equals(filter.subjectDN) : filter.subjectDN == null);
    }

    @Override
    public int hashCode() {
        int result = serialNumber != null ? serialNumber.hashCode() : 0;
        result = 31 * result + (issuerDN != null ? issuerDN.hashCode() : 0);
        result = 31 * result + (subjectDN != null ? subjectDN.hashCode() : 0);
        return result;
    }
}
