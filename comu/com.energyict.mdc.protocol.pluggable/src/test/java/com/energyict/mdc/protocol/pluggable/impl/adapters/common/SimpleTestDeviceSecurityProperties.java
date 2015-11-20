package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SimpleTestDeviceSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:54)
 */
public class SimpleTestDeviceSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        FIRST {
            @Override
            public String javaName() {
                return "first";
            }

            @Override
            public String databaseName() {
                return "FIRST";
            }
        },
        SECOND {
            @Override
            public String javaName() {
                return "second";
            }

            @Override
            public String databaseName() {
                return "SECOND";
            }
        },
        THIRD {
            @Override
            public String javaName() {
                return "third";
            }

            @Override
            public String databaseName() {
                return "THIRD";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.basicPropertySpec(this.javaName(), false, StringFactory.class);
        }
    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String first;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String second;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String third;
    @Size(max = Table.MAX_STRING_LENGTH)

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.first = (String) propertyValues.getProperty(ActualFields.FIRST.javaName());
        this.second = (String) propertyValues.getProperty(ActualFields.SECOND.javaName());
        this.third = (String) propertyValues.getProperty(ActualFields.THIRD.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.FIRST.javaName(), this.first);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.SECOND.javaName(), this.second);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.THIRD.javaName(), this.third);
    }

    private void setPropertyIfNotNull(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}