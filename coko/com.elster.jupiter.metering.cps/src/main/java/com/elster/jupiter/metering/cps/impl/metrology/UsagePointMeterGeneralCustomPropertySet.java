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

@Component(name = "c.e.j.m.cps.impl.metrology.UsagePointMeterGeneralCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointMeterGeneralCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointMeterGeneralDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private final String ID = "c.e.j.m.cps.impl.mtr.UsagePointMeterGeneralCustomPropertySet";

    public UsagePointMeterGeneralCustomPropertySet() {

    }

    public UsagePointMeterGeneralCustomPropertySet(PropertySpecService propertySpecService) {
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
        return thesaurus.getFormat(TranslationKeys.CPS_METER_GENERAL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointMeterGeneralDomainExtension> getPersistenceSupport() {
        return new UsagePointMeterGeneralPersistenceSupport(thesaurus);
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
                .named(UsagePointMeterGeneralDomainExtension.Fields.MANUFACTURER.javaName(), TranslationKeys.CPS_METER_GENERAL_MANUFACTURER)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MANUFACTURER_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .finish();
        PropertySpec modelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGeneralDomainExtension.Fields.MODEL.javaName(), TranslationKeys.CPS_METER_GENERAL_MODEL)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_MODEL_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .finish();
        PropertySpec classSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterGeneralDomainExtension.Fields.CLASS.javaName(), TranslationKeys.CPS_METER_GENERAL_CLASS)
                .describedAs(TranslationKeys.CPS_METER_GENERAL_CLASS_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .addValues("Class 0.5", "Class 1", "Class 2", "Class 3")
                .finish();
        return Arrays.asList(manufacturerSpec,
                modelSpec,
                classSpec);
    }

    private static class UsagePointMeterGeneralPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointMeterGeneralDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_MET_GEN";
        public static final String FK_CPS_DEVICE_MTR_GEN = "FK_CPS_MTR_USAGEPOINT_MET_GEN";
        public static final String COMPONENT_NAME = "MET_GEN";
        private Thesaurus thesaurus;


        public UsagePointMeterGeneralPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointMeterGeneralDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_MTR_GEN;
        }

        public Class<UsagePointMeterGeneralDomainExtension> persistenceClass() {
            return UsagePointMeterGeneralDomainExtension.class;
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
            table.column(UsagePointMeterGeneralDomainExtension.Fields.MANUFACTURER.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGeneralDomainExtension.Fields.MANUFACTURER.javaName())
                    .add();
            table.column(UsagePointMeterGeneralDomainExtension.Fields.MODEL.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGeneralDomainExtension.Fields.MODEL.javaName())
                    .add();
            table.column(UsagePointMeterGeneralDomainExtension.Fields.CLASS.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterGeneralDomainExtension.Fields.CLASS.javaName())
                    .add();
        }
    }
}
