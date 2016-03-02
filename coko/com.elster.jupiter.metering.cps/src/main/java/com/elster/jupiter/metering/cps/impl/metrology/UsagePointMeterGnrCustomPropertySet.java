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
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointMeterGnrCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointMeterGnrCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointMeterGnrDomainExtension> {

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_MET_GEN";
    public static final String FK_CPS_DEVICE_MTR_GEN = "FK_CPS_MTR_USAGEPOINT_MET_GEN";
    public static final String COMPONENT_NAME = "MET_GEN";

    public UsagePointMeterGnrCustomPropertySet() {
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
    public UsagePointMeterGnrCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
    }

    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_METER_GENERAL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointMeterGnrDomainExtension> getPersistenceSupport() {
        return new UsagePointMtrGeneralPersistSupp(this.getThesaurus());
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
        PropertySpec manufacturerSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.javaName(), TranslationKeys.CPS_METER_GENERAL_MANUFACTURER)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MANUFACTURER_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec modelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.MODEL.javaName(), TranslationKeys.CPS_METER_GENERAL_MODEL)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MODEL_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec classSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGnrDomainExtension.Fields.CLASS.javaName(), TranslationKeys.CPS_METER_GENERAL_CLASS)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_CLASS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Class 0.5", "Class 1", "Class 2", "Class 3")
                .finish();
        return Arrays.asList(manufacturerSpec,
                modelSpec,
                classSpec);
    }

    private Thesaurus getThesaurus() {
        return this.nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private static class UsagePointMtrGeneralPersistSupp implements PersistenceSupport<UsagePoint, UsagePointMeterGnrDomainExtension> {
        private Thesaurus thesaurus;


        public UsagePointMtrGeneralPersistSupp(Thesaurus thesaurus) {
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
            return UsagePointMeterGnrDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_MTR_GEN;
        }

        public Class<UsagePointMeterGnrDomainExtension> persistenceClass() {
            return UsagePointMeterGnrDomainExtension.class;
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
            table.column(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGnrDomainExtension.Fields.MANUFACTURER.javaName())
                    .add();
            table.column(UsagePointMeterGnrDomainExtension.Fields.MODEL.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGnrDomainExtension.Fields.MODEL.javaName())
                    .add();
            table.column(UsagePointMeterGnrDomainExtension.Fields.CLASS.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGnrDomainExtension.Fields.CLASS.javaName())
                    .add();
        }
    }
}
