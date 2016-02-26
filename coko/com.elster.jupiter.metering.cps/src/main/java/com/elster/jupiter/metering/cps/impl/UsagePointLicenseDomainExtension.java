package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.time.Instant;

public class UsagePointLicenseDomainExtension implements PersistentDomainExtension<UsagePoint> {

    public enum Fields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        NUMBER {
            @Override
            public String javaName() {
                return "number";
            }
        },
        EXPIRATION_DATE {
            @Override
            public String javaName() {
                return "expirationDate";
            }
        },
        CERTIFICATION_DOC {
            @Override
            public String javaName() {
                return "certificationDoc";
            }
        },
        METERING_SCHEME {
            @Override
            public String javaName() {
                return "meteringScheme";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    @IsPresent
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    private String number;
    private Instant expirationDate;
    private String certificationDoc;
    private String meteringScheme;

    public UsagePointLicenseDomainExtension() {
        super();
    }

    public String getNumber() {
        return number;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public String getCertificationDoc() {
        return certificationDoc;
    }

    public String getMeteringScheme() {
        return meteringScheme;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.number = (String) propertyValues.getProperty(Fields.NUMBER.javaName());
        this.expirationDate = (Instant) propertyValues.getProperty(Fields.EXPIRATION_DATE.javaName());
        this.certificationDoc = (String) propertyValues.getProperty(Fields.CERTIFICATION_DOC.javaName());
        this.meteringScheme = (String) propertyValues.getProperty(Fields.METERING_SCHEME.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(Fields.NUMBER.javaName(), this.getNumber());
        propertySetValues.setProperty(Fields.EXPIRATION_DATE.javaName(), this.getExpirationDate());
        propertySetValues.setProperty(Fields.CERTIFICATION_DOC.javaName(), this.getCertificationDoc());
        propertySetValues.setProperty(Fields.METERING_SCHEME.javaName(), this.getMeteringScheme());
    }

    @Override
    public void validateDelete() {

    }


}
