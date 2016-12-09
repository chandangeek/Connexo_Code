package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.upl.security.Certificate;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link DlmsSecuritySuite1And2Support}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (16:51)
 */
public class DlmsSecuritySuite1And2Properties extends DlmsSecurityProperties {

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

    private Certificate serverSignatureCertificate;
    private Certificate serverAgreementCertificate;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.serverSignatureCertificate = (Certificate) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.SERVER_SIGNING_CERTIFICATE.javaName());
        this.serverAgreementCertificate = (Certificate) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (this.serverSignatureCertificate != null) {
            setTypedPropertyValueTo(propertySetValues, DeviceSecurityProperty.SERVER_SIGNING_CERTIFICATE.javaName(), this.serverSignatureCertificate);
        }
        if (this.serverAgreementCertificate != null) {
            setTypedPropertyValueTo(propertySetValues, DeviceSecurityProperty.SERVER_KEY_AGREEMENT_CERTIFICATE.javaName(), this.serverAgreementCertificate);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}