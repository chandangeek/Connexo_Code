package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.TranslationInstaller;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointContractualCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointContractualCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointContractualDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private final String ID = "c.e.j.m.cps.impl.mtr.UsagePointContractualCustomPropertySet";

    public UsagePointContractualCustomPropertySet() {

    }

    public UsagePointContractualCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        activate();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_CONTRACTUAL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointContractualDomainExtension> getPersistenceSupport() {
        return new UsagePointContractualPersistenceSupport(thesaurus);
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
        PropertySpec billingCycleSpec = propertySpecService
                .stringSpec()
                .named(UsagePointContractualDomainExtension.Fields.BILLING_CYCLE.javaName(), TranslationKeys.CPS_CONTRACTUAL_BILLING_CYCLE)
                .fromThesaurus(thesaurus)
                .addValues("Monthly", "Yearly", "Billing month")
                .markRequired()
                .finish();

        return Arrays.asList(billingCycleSpec);
    }

    private static class UsagePointContractualPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointContractualDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_CON";
        public static final String FK_CPS_DEVICE_CONTRACTUAL = "FK_CPS_MTR_USAGEPOINT_CON";
        public static final String COMPONENT_NAME = "CON";
        private Thesaurus thesaurus;

        public UsagePointContractualPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointContractualDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_CONTRACTUAL;
        }

        @Override
        public Class<UsagePointContractualDomainExtension> persistenceClass() {
            return UsagePointContractualDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(UsagePointContractualDomainExtension.Fields.BILLING_CYCLE.databaseName())
                    .varChar(255)
                    .map(UsagePointContractualDomainExtension.Fields.BILLING_CYCLE.javaName())
                    .notNull()
                    .add();
        }
    }
}
