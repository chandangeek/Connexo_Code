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

@Component(name = "c.e.j.m.cps.impl.metrology.UsagePointConvertorCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointConvertorCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointConvertorDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private final String ID = "c.e.j.m.cps.impl.mtr.UsagePointConvertorCustomPropertySet";

    public UsagePointConvertorCustomPropertySet() {

    }

    public UsagePointConvertorCustomPropertySet(PropertySpecService propertySpecService) {
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
        return thesaurus.getFormat(TranslationKeys.CPS_CONVERTOR_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointConvertorDomainExtension> getPersistenceSupport() {
        return new UsagePointConvertorPersistenceSupport(thesaurus);
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
        PropertySpec serialNumberSpec = propertySpecService
                .stringSpec()
                .named(UsagePointConvertorDomainExtension.Fields.SERIAL_NUMBER.javaName(), TranslationKeys.CPS_CONVERTOR_SERIAL_NUMBER)
                .describedAs(TranslationKeys.CPS_CONVERTOR_SERIAL_NUMBER_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .finish();
        //unused until QuantityFactory is created
//        PropertySpec noOfCorrectedDialsSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointConvertorDomainExtension.Fields.NO_OF_CORRECTED_DIALS.javaName(), TranslationKeys.CPS_CONVERTOR_NO_OF_CORRECTED_DIALS)
//                .describedAs(TranslationKeys.CPS_CONVERTOR_NO_OF_CORRECTED_DIALS_DESCRIPTION)
//                .fromThesaurus(thesaurus)
//                .finish();
//        PropertySpec noOfUncorrectedDialsSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointConvertorDomainExtension.Fields.NO_OF_UNCORRECTED_DIALS.javaName(), TranslationKeys.CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS)
//                .describedAs(TranslationKeys.CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS_DESCRIPTION)
//                .fromThesaurus(thesaurus)
//                .finish();

        return Arrays.asList(serialNumberSpec);
        // noOfCorrectedDialsSpec,
        // noOfUncorrectedDialsSpec);
    }

    private static class UsagePointConvertorPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointConvertorDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_CONV";
        public static final String FK_CPS_DEVICE_CONVERTOR = "FK_CPS_MTR_USAGEPOINT_CONV";
        public static final String COMPONENT_NAME = "CONV";
        private Thesaurus thesaurus;


        public UsagePointConvertorPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointConvertorDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_CONVERTOR;
        }

        @Override
        public Class<UsagePointConvertorDomainExtension> persistenceClass() {
            return UsagePointConvertorDomainExtension.class;
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
            table.column(UsagePointConvertorDomainExtension.Fields.SERIAL_NUMBER.databaseName())
                    .varChar(255)
                    .map(UsagePointConvertorDomainExtension.Fields.SERIAL_NUMBER.javaName())
                    .add();
            //unused until QuantityFactory is created
//            table.column(UsagePointConvertorDomainExtension.Fields.NO_OF_CORRECTED_DIALS.databaseName())
//                    .varChar(255)
//                    .map(UsagePointConvertorDomainExtension.Fields.NO_OF_CORRECTED_DIALS.javaName())
//                    .add();
//            table.column(UsagePointConvertorDomainExtension.Fields.NO_OF_UNCORRECTED_DIALS.databaseName())
//                    .varChar(255)
//                    .map(UsagePointConvertorDomainExtension.Fields.NO_OF_UNCORRECTED_DIALS.javaName())
//                    .add();
        }
    }
}
