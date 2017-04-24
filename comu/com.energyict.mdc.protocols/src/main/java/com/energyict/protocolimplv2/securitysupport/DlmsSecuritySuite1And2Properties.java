package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySuite1And2Support}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (16:51)
 */
public class DlmsSecuritySuite1And2Properties extends DlmsSecurityProperties {

    private CertificateWrapper serverSignatureCertificate;
    private CertificateWrapper serverAgreementCertificate;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        super.copyActualPropertiesFrom(propertyValues);
        this.serverSignatureCertificate = (CertificateWrapper) getTypedPropertyValue(propertyValues, SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString());
        this.serverAgreementCertificate = (CertificateWrapper) getTypedPropertyValue(propertyValues, SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        super.copyActualPropertiesTo(propertySetValues);
        if (this.serverSignatureCertificate != null) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecName.SERVER_SIGNING_CERTIFICATE.toString(), this.serverSignatureCertificate);
        }
        if (this.serverAgreementCertificate != null) {
            setTypedPropertyValueTo(propertySetValues, SecurityPropertySpecName.SERVER_KEY_AGREEMENT_CERTIFICATE.toString(), this.serverAgreementCertificate);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    public enum ActualFields {
        SERVER_SIGNATURE_CERTIFICATE {
            @Override
            public String javaName() {
                return "serverSignatureCertificate";
            }

            @Override
            public String databaseName() {
                return "SRVR_SIGNATURE_CERTIFICATE";
            }
        },
        SERVER_KEY_AGREEMENT_CERTIFICATE {
            @Override
            public String javaName() {
                return "serverAgreementCertificate";
            }

            @Override
            public String databaseName() {
                return "SRVR_AGREEMNT_CERTIFICATE";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }

    }
}