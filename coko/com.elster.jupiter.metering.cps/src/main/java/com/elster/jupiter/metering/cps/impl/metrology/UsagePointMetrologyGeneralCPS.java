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

@Component(name = "com.e.j.m.cps.impl.metrology.UsagePointMetrologyGeneralCPS", service = CustomPropertySet.class, immediate = true)
public class UsagePointMetrologyGeneralCPS implements CustomPropertySet<UsagePoint, UsagePointMetrologyGeneralDomExt> {

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_GENERAL";
    public static final String FK_CPS_DEVICE_GENERAL = "FK_CPS_MTR_USAGEPOINT_GENERAL";
    public static final String COMPONENT_NAME = "MTR_GNR";

    public UsagePointMetrologyGeneralCPS() {
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
    public UsagePointMetrologyGeneralCPS(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_METROLOGY_GENERAL_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointMetrologyGeneralDomExt> getPersistenceSupport() {
        return new UsagePointMetrologyGeneralPersSupp(this.getThesaurus());
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
        PropertySpec readCycleSpec = propertySpecService
                .stringSpec()
                .named(UsagePointMetrologyGeneralDomExt.Fields.READ_CYCLE.javaName(), TranslationKeys.CPS_METROLOGY_GENERAL_PROPERTIES_READ_CYCLE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();
        PropertySpec informationFrequencySpec = propertySpecService
                .stringSpec()
                .named(UsagePointMetrologyGeneralDomExt.Fields.INFORMATION_FREQUENCY.javaName(), TranslationKeys.CPS_METROLOGY_GENERAL_PROPERTIES_INFORMATION_FREQUENCY)
                .describedAs(TranslationKeys.CPS_METROLOGY_GENERAL_PROPERTIES_INFORMATION_FREQUENCY_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markEditable()
                .finish();
        return Arrays.asList(readCycleSpec, informationFrequencySpec);
    }

    private Thesaurus getThesaurus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private static class UsagePointMetrologyGeneralPersSupp implements PersistenceSupport<UsagePoint, UsagePointMetrologyGeneralDomExt> {

        private Thesaurus thesaurus;

        public UsagePointMetrologyGeneralPersSupp(Thesaurus thesaurus) {
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
            return UsagePointMetrologyGeneralDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_GENERAL;
        }

        @Override
        public Class<UsagePointMetrologyGeneralDomExt> persistenceClass() {
            return UsagePointMetrologyGeneralDomExt.class;
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
            table.column(UsagePointMetrologyGeneralDomExt.Fields.READ_CYCLE.databaseName())
                    .varChar(255)
                    .map(UsagePointMetrologyGeneralDomExt.Fields.READ_CYCLE.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointMetrologyGeneralDomExt.Fields.INFORMATION_FREQUENCY.databaseName())
                    .varChar(255)
                    .map(UsagePointMetrologyGeneralDomExt.Fields.INFORMATION_FREQUENCY.javaName())
                    .notNull()
                    .add();
        }
    }
}
