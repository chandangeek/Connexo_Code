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

@Component(name = "c.e.j.m.cps.impl.metrology.UsagePointMeterTechInfGTWCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointMeterTechInfGTWCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointMeterTechInfGTWDomExt> {

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_T_IN";
    public static final String FK_CPS_DEVICE_METER_TECH_INFORM = "FK_CPS_MTR_USAGEPOINT_T_IN";
    public static final String COMPONENT_NAME = "TECH_INF";

    public UsagePointMeterTechInfGTWCustomPropertySet() {
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
    public UsagePointMeterTechInfGTWCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getThesaurus()
                .getFormat(TranslationKeys.CPS_METER_TECH_INFORMATION_GTW_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointMeterTechInfGTWDomExt> getPersistenceSupport() {
        return new UsagePointMeterTechInfGTWPersSupp(this.getThesaurus());
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
        PropertySpec recessedLengthSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.javaName(), TranslationKeys.CPS_METER_TECH_RECESSED_LENGTH)
                .describedAs(TranslationKeys.CPS_METER_TECH_RECESSED_LENGTH_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec connectionTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.javaName(), TranslationKeys.CPS_METER_TECH_CONNECTION_TYPE)
                .describedAs(TranslationKeys.CPS_METER_TECH_CONNECTION_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec conversionMetrologySpec = propertySpecService
                .stringSpec()
                .named(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.javaName(), TranslationKeys.CPS_METER_TECH_CONVERSION_METROLOGY)
                .describedAs(TranslationKeys.CPS_METER_TECH_CONVERSION_METROLOGY_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();

        //unused until QuantityFactory is created
//        PropertySpec capacityMinimalSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MINIMAL.javaName(), TranslationKeys.CPS_METER_TECH_CAPACITY_MINIMAL)
//                .describedAs(TranslationKeys.CPS_METER_TECH_CAPACITY_MINIMAL_DESCRIPTION)
//                .fromThesaurus(this.getThesaurus())
//                .finish();
//        PropertySpec capacityNominalSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_NOMINAL.javaName(), TranslationKeys.CPS_METER_TECH_CAPACITY_NOMINAL)
//                .describedAs(TranslationKeys.CPS_METER_TECH_CAPACITY_NOMINAL_DESCRIPTION)
//                .fromThesaurus(this.getThesaurus())
//                .finish();
//        PropertySpec capacityMaximalSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MAXIMAL.javaName(), TranslationKeys.CPS_METER_TECH_CAPACITY_MAXIMAL)
//                .describedAs(TranslationKeys.CPS_METER_TECH_CAPACITY_MAXIMAL_DESCRIPTION)
//                .fromThesaurus(this.getThesaurus())
//                .finish();
//        PropertySpec pressureMaximalSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAXIMAL.javaName(), TranslationKeys.CPS_METER_TECH_PRESSURE_MAXIMAL)
//                .describedAs(TranslationKeys.CPS_METER_TECH_PRESSURE_MAXIMAL_DESCRIPTION)
//                .fromThesaurus(this.getThesaurus())
//                .finish();
        return Arrays.asList(recessedLengthSpec,
                connectionTypeSpec,
                conversionMetrologySpec);
        // capacityMinimalSpec,
        // capacityNominalSpec,
        // capacityMaximalSpec,
        //pressureMaximalSpec);
    }

    private Thesaurus getThesaurus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private static class UsagePointMeterTechInfGTWPersSupp implements PersistenceSupport<UsagePoint, UsagePointMeterTechInfGTWDomExt> {

        private Thesaurus thesaurus;

        public UsagePointMeterTechInfGTWPersSupp(Thesaurus thesaurus) {
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
            return UsagePointMeterTechInfGTWDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_METER_TECH_INFORM;
        }

        @Override
        public Class<UsagePointMeterTechInfGTWDomExt> persistenceClass() {
            return UsagePointMeterTechInfGTWDomExt.class;
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
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.RECESSED_LENGTH.javaName())
                    .add();
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CONNECTION_TYPE.javaName())
                    .add();
            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.databaseName())
                    .varChar(255)
                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CONVERSION_METROLOGY.javaName())
                    .add();

            //unused until QuantityFactory is created
//            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MINIMAL.databaseName())
//                    .varChar(255)
//                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MINIMAL.javaName())
//                    .add();
//            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_NOMINAL.databaseName())
//                    .varChar(255)
//                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_NOMINAL.javaName())
//                    .add();
//            table.column(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MAXIMAL.databaseName())
//                    .varChar(255)
//                    .map(UsagePointMeterTechInfGTWDomExt.Fields.CAPACITY_MAXIMAL.javaName())
//                    .add();
//            table.column(UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAXIMAL.databaseName())
//                    .varChar(255)
//                    .map(UsagePointMeterTechInfGTWDomExt.Fields.PRESSURE_MAXIMAL.javaName())
//                    .add();
        }
    }
}
