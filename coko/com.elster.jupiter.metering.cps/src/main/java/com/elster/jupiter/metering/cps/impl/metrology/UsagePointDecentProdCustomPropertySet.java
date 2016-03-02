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
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointDecentProdCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointDecentProdCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointDecentProdDomExt> {

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_DEC";
    public static final String FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION = "FK_CPS_MTR_USAGEPOINT_DEC";
    public static final String COMPONENT_NAME = "DEC_PROD";

    public UsagePointDecentProdCustomPropertySet() {
        super();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Inject
    public UsagePointDecentProdCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }
    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getTheasarus().getFormat(TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> getPersistenceSupport() {
        return new UsagePointDecentrProdPS(this.getTheasarus());
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
//                .named(UsagePointDecentProdDomExt.Fields.INSTALLED_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_INSTALLED_POWER)
//                .fromThesaurus(this.getTheasarus())
//                .markEditable()
//                .markRequired()
//                .finish();
//        PropertySpec convertorPowerSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_CONVERTER_POWER)
//                .fromThesaurus(this.getTheasarus())
//                .markEditable()
//                .markRequired()
//                .finish();
        PropertySpec typeOfDecentralizedProductionSpec = propertySpecService
                .stringSpec()
                .named(UsagePointDecentProdDomExt.Fields.TYPE_OF_DECENTRALIZED_PROD.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_TYPE_OF_DECENTRALIZED_PRODUCTION)
                .fromThesaurus(this.getTheasarus())
                .addValues("solar", "wind", "other")
                .markRequired()
                .finish();

        PropertySpec commissioningDateSpec = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_COMMISSIONING_DATE)
                .fromThesaurus(this.getTheasarus())
                .markRequired()
                .finish();

        return Arrays.asList(
                //    installedPowerSpec,
                //    convertorPowerSpec,
                typeOfDecentralizedProductionSpec,
                commissioningDateSpec);
    }

    private Thesaurus getTheasarus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private static class UsagePointDecentrProdPS implements PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> {
        private Thesaurus thesaurus;

        public UsagePointDecentrProdPS(Thesaurus thesaurus) {
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
            return UsagePointDecentProdDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION;
        }

        @Override
        public Class<UsagePointDecentProdDomExt> persistenceClass() {
            return UsagePointDecentProdDomExt.class;
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
//            table.column(UsagePointDecentProdDomExt.Fields.INSTALLED_POWER.databaseName())
//                    .varChar(255)
//                    .map(UsagePointDecentProdDomExt.Fields.INSTALLED_POWER.javaName())
//                    .notNull()
//                    .add();
//            table.column(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.databaseName())
//                    .varChar(255)
//                    .map(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName())
//                    .notNull()
//                    .add();
            table.column(UsagePointDecentProdDomExt.Fields.TYPE_OF_DECENTRALIZED_PROD.databaseName())
                    .varChar(255)
                    .map(UsagePointDecentProdDomExt.Fields.TYPE_OF_DECENTRALIZED_PROD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.databaseName())
                    .varChar(255)
                    .map(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName())
                    .notNull()
                    .add();
        }
    }
}
