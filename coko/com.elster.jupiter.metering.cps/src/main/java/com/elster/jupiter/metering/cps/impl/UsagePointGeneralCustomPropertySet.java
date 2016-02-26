package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
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

@Component(name = "c.e.j.mtr.cps.impl.UsagePointGeneralCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointGeneralCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointGeneralDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    public UsagePointGeneralCustomPropertySet() {
    }

    public UsagePointGeneralCustomPropertySet(PropertySpecService propertySpecService) {
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
        return UsagePointGeneralCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_GENERAL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> getPersistenceSupport() {
        return new UsagePointGeneralPersistenceSupport(thesaurus);
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
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
        PropertySpec prepaySpec = propertySpecService
                .booleanSpec()
                .named(UsagePointGeneralDomainExtension.Fields.PREPAY.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_PREPAY)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_PREPAY_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
        PropertySpec marketCodeSectorSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_MARKET_CODE_SECTOR)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_MARKED_CODE_SECTOR_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .addValues("BE", "NL", "UK")
                .markRequired()
                .finish();
        PropertySpec meteringPointTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.javaName(), TranslationKeys.CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE)
                .describedAs(TranslationKeys.CPS_GENERAL_PROPERTIES_METERING_POINT_TYPE_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .addValues("BE", "NL", "UK")
                .markRequired()
                .finish();

        return Arrays.asList(prepaySpec, marketCodeSectorSpec, meteringPointTypeSpec);
    }

    private class UsagePointGeneralPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointGeneralDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_USAGEPOINT_GENERAL";
        public static final String FK_CPS_DEVICE_GENERAL = "FK_CPS_USAGEPOINT_GENERAL";
        public static final String COMPONENT_NAME = "GNR";
        private Thesaurus thesaurus;

        public UsagePointGeneralPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointGeneralDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_GENERAL;
        }

        @Override
        public Class<UsagePointGeneralDomainExtension> persistenceClass() {
            return UsagePointGeneralDomainExtension.class;
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
            table.column(UsagePointGeneralDomainExtension.Fields.PREPAY.databaseName())
                    .bool()
                    .map(UsagePointGeneralDomainExtension.Fields.PREPAY.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.databaseName())
                    .varChar(255)
                    .map(UsagePointGeneralDomainExtension.Fields.MARKET_CODE_SECTOR.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.databaseName())
                    .varChar(255)
                    .map(UsagePointGeneralDomainExtension.Fields.METERING_POINT_TYPE.javaName())
                    .notNull()
                    .add();
        }
    }

}
