package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;

import javax.validation.constraints.Size;
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

    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.empty();

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String number;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private Instant expirationDate;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String certificationDoc;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{FieldTooLong}")
    private String meteringScheme;

    private Interval interval;

    public UsagePointLicenseDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCertificationDoc() {
        return certificationDoc;
    }

    public void setCertificationDoc(String certificationDoc) {
        this.certificationDoc = certificationDoc;
    }

    public String getMeteringScheme() {
        return meteringScheme;
    }

    public void setMeteringScheme(String meteringScheme) {
        this.meteringScheme = meteringScheme;
    }

    @Override
    public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.usagePoint.set(domainInstance);
        this.setNumber((String) propertyValues.getProperty(Fields.NUMBER.javaName()));
        this.setExpirationDate((Instant) propertyValues.getProperty(Fields.EXPIRATION_DATE.javaName()));
        this.setCertificationDoc((String) propertyValues.getProperty(Fields.CERTIFICATION_DOC.javaName()));
        this.setMeteringScheme((String) propertyValues.getProperty(Fields.METERING_SCHEME.javaName()));
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
