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
import com.elster.jupiter.properties.InstantFactory;
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

@Component(name = "c.e.j.m.cps.impl.metrology.UsagePointDecentralizedProductionCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointDecentralizedProductionCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointDecentralizedProductionDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private final String ID = "c.e.j.m.cps.impl.mtr.UsagePointDecentralizedProductionCustomPropertySet";

    public UsagePointDecentralizedProductionCustomPropertySet() {
    }

    public UsagePointDecentralizedProductionCustomPropertySet(PropertySpecService propertySpecService) {
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
        return thesaurus.getFormat(TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointDecentralizedProductionDomainExtension> getPersistenceSupport() {
        return new UsagePointDecentralizedProductionPersistenceSupport(thesaurus);
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
        //unused until QuantityFactory is created
//        PropertySpec installedPowerSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointDecentralizedProductionDomainExtension.Fields.INSTALLED_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_INSTALLED_POWER)
//                .fromThesaurus(thesaurus)
//                .markEditable()
//                .markRequired()
//                .finish();
//        PropertySpec convertorPowerSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointDecentralizedProductionDomainExtension.Fields.CONVERTOR_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_CONVERTER_POWER)
//                .fromThesaurus(thesaurus)
//                .markEditable()
//                .markRequired()
//                .finish();
        PropertySpec typeOfDecentralizedProductionSpec = propertySpecService
                .stringSpec()
                .named(UsagePointDecentralizedProductionDomainExtension.Fields.TYPE_OF_DECENTRALIZED_PROD.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_TYPE_OF_DECENTRALIZED_PRODUCTION)
                .fromThesaurus(thesaurus)
                .addValues("solar", "wind", "other")
                .markRequired()
                .finish();

        PropertySpec commissioningDateSpec = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(UsagePointDecentralizedProductionDomainExtension.Fields.COMMISSIONING_DATE.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_COMMISSIONING_DATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();

        return Arrays.asList(
                //    installedPowerSpec,
                //    convertorPowerSpec,
                typeOfDecentralizedProductionSpec,
                commissioningDateSpec);
    }

    private static class UsagePointDecentralizedProductionPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointDecentralizedProductionDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_DEC";
        public static final String FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION = "FK_CPS_MTR_USAGEPOINT_DEC";
        public static final String COMPONENT_NAME = "DEC_PROD";
        private Thesaurus thesaurus;

        public UsagePointDecentralizedProductionPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointDecentralizedProductionDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION;
        }

        @Override
        public Class<UsagePointDecentralizedProductionDomainExtension> persistenceClass() {
            return UsagePointDecentralizedProductionDomainExtension.class;
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
            //unused until QuantityFactory is created
//            table.column(UsagePointDecentralizedProductionDomainExtension.Fields.INSTALLED_POWER.databaseName())
//                    .varChar(255)
//                    .map(UsagePointDecentralizedProductionDomainExtension.Fields.INSTALLED_POWER.javaName())
//                    .notNull()
//                    .add();
//            table.column(UsagePointDecentralizedProductionDomainExtension.Fields.CONVERTOR_POWER.databaseName())
//                    .varChar(255)
//                    .map(UsagePointDecentralizedProductionDomainExtension.Fields.CONVERTOR_POWER.javaName())
//                    .notNull()
//                    .add();
            table.column(UsagePointDecentralizedProductionDomainExtension.Fields.TYPE_OF_DECENTRALIZED_PROD.databaseName())
                    .varChar(255)
                    .map(UsagePointDecentralizedProductionDomainExtension.Fields.TYPE_OF_DECENTRALIZED_PROD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointDecentralizedProductionDomainExtension.Fields.COMMISSIONING_DATE.databaseName())
                    .varChar(255)
                    .map(UsagePointDecentralizedProductionDomainExtension.Fields.COMMISSIONING_DATE.javaName())
                    .notNull()
                    .add();
        }
    }
}
