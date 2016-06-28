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

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//@Component(name = "c.e.j.mtr.cps.impl.mtr.UsagePointTechInstAllCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechInstAllCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechInstAllDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_TECH";
    public static final String FK_CPS_DEVICE_TECHNICAL_INSTALLATION = "FK_CPS_MTR_USAGEPOINT_TECH";
    public static final String COMPONENT_NAME = "IST";

    public UsagePointTechInstAllCustomPropertySet() {
        super();
    }

    public UsagePointTechInstAllCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
        return this.getThesaurus().getFormat(TranslationKeys.CPS_TECHNICAL_INSTALLATION_ALL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechInstAllDomExt> getPersistenceSupport() {
        return new UsagePointTechInstAllPersSupp(this.getThesaurus());
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
        PropertySpec substationSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechInstAllDomExt.Fields.SUBSTATION.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_SUBSTATION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        return Arrays.asList(substationSpec);
    }

    private static class UsagePointTechInstAllPersSupp implements PersistenceSupport<UsagePoint, UsagePointTechInstAllDomExt> {

        private Thesaurus thesaurus;

        @Override
        public String application() {
            return "Example";
        }

        public UsagePointTechInstAllPersSupp(Thesaurus thesaurus) {
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
            return UsagePointTechInstAllDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_TECHNICAL_INSTALLATION;
        }

        @Override
        public Class<UsagePointTechInstAllDomExt> persistenceClass() {
            return UsagePointTechInstAllDomExt.class;
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
            table.column(UsagePointTechInstAllDomExt.Fields.SUBSTATION.databaseName())
                    .varChar(255)
                    .map(UsagePointTechInstAllDomExt.Fields.SUBSTATION.javaName())
                    .add();
        }
    }

}
