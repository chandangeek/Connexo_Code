package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointTechInstEGCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechInstEGDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_INST_EG";
    public static final String FK_CPS_DEVICE_TECHNICAL_INSTALLATION = "FK_CPS_MTR_USAGEPOINT_INST_EG";
    public static final String COMPONENT_NAME = "INST_EG";

    public UsagePointTechInstEGCustomPropertySet() {
        super();
    }

    public UsagePointTechInstEGCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getThesaurus()
                .getFormat(TranslationKeys.CPS_TECHNICAL_INSTALLATION_ELECTRICITY_GAS_SIMPLE_NAME)
                .format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechInstEGDomExt> getPersistenceSupport() {
        return new UsagePointTechInstEGPerSupp(this.getThesaurus());
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec lossFactorSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointTechInstEGDomExt.Fields.LOSS_FACTOR.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_LOSS_FACTOR)
                .describedAs(TranslationKeys.CPS_TECHNICAL_INSTALLATION_LOSS_FACTOR_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues(Quantity.create(new BigDecimal(0), 0, "%"))
                .finish();

        return Arrays.asList(lossFactorSpec);
    }

    private static class UsagePointTechInstEGPerSupp implements PersistenceSupport<UsagePoint, UsagePointTechInstEGDomExt> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return "Example";
        }

        public UsagePointTechInstEGPerSupp(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return COMPONENT_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UsagePointTechInstEGDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_TECHNICAL_INSTALLATION;
        }

        @Override
        public Class<UsagePointTechInstEGDomExt> persistenceClass() {
            return UsagePointTechInstEGDomExt.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.addQuantityColumns(UsagePointTechInstEGDomExt.Fields.LOSS_FACTOR.databaseName(), false, UsagePointTechInstEGDomExt.Fields.LOSS_FACTOR
                    .javaName());
        }
    }
}
