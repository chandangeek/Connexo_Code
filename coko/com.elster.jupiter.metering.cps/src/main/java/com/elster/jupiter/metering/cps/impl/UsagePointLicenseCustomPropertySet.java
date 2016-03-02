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
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//@Component(name = "c.e.j.mtr.cps.impl.UsagePointLicenceCPS", service = CustomPropertySet.class, immediate = true)
public class UsagePointLicenseCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointLicenseDomainExtension> {

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    public static final String TABLE_NAME = "RVK_CPS_USAGEPOINT_LIC";
    public static final String FK_CPS_DEVICE_LICENSE = "FK_CPS_USAGEPOINT_LIC";
    public static final String COMPONENT_NAME = "LICENSE";


    public UsagePointLicenseCustomPropertySet() {
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
    public UsagePointLicenseCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
        System.out.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return getThesaurus().getFormat(TranslationKeys.CPS_LICENSE_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointLicenseDomainExtension> getPersistenceSupport() {
        return new UsagePointLicensePersistenceSupport(getThesaurus());
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
        PropertySpec numberSpec = propertySpecService
                .stringSpec()
                .named(UsagePointLicenseDomainExtension.Fields.NUMBER.javaName(), TranslationKeys.CPS_LICENSE_PROPERTIES_NUMBER)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec expirationDateSpec = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(UsagePointLicenseDomainExtension.Fields.EXPIRATION_DATE.javaName(), TranslationKeys.CPS_LICENSE_PROPERTIES_EXPIRATION_DATE)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec certificationDocSpec = propertySpecService
                .stringSpec()
                .named(UsagePointLicenseDomainExtension.Fields.CERTIFICATION_DOC.javaName(), TranslationKeys.CPS_LICENSE_PROPERTIES_CERTIFICATION_DOC)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec meteringSchemeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointLicenseDomainExtension.Fields.METERING_SCHEME.javaName(), TranslationKeys.CPS_LICENSE_PROPERTIES_METERING_SCHEME)
                .fromThesaurus(this.getThesaurus())
                .finish();

        return Arrays.asList(numberSpec,
                expirationDateSpec,
                certificationDocSpec,
                meteringSchemeSpec);
    }

    private Thesaurus getThesaurus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private class UsagePointLicensePersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointLicenseDomainExtension> {

        private Thesaurus thesaurus;

        public UsagePointLicensePersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointLicenseDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_LICENSE;
        }

        @Override
        public Class<UsagePointLicenseDomainExtension> persistenceClass() {
            return UsagePointLicenseDomainExtension.class;
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
            table.column(UsagePointLicenseDomainExtension.Fields.NUMBER.databaseName())
                    .varChar(255)
                    .map(UsagePointLicenseDomainExtension.Fields.NUMBER.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointLicenseDomainExtension.Fields.EXPIRATION_DATE.databaseName())
                    .varChar(255)
                    .map(UsagePointLicenseDomainExtension.Fields.EXPIRATION_DATE.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointLicenseDomainExtension.Fields.CERTIFICATION_DOC.databaseName())
                    .varChar(255)
                    .map(UsagePointLicenseDomainExtension.Fields.CERTIFICATION_DOC.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointLicenseDomainExtension.Fields.METERING_SCHEME.databaseName())
                    .varChar(255)
                    .map(UsagePointLicenseDomainExtension.Fields.METERING_SCHEME.javaName())
                    .notNull()
                    .add();
        }
    }
}

