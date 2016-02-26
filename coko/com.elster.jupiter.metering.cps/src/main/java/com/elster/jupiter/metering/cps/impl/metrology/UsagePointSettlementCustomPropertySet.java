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

@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointSettlementCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointSettlementCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointSettlementDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private final String ID = "com.e.j.m.cps.impl.mtr.UsagePointSettlementCustomPropertySet";

    public UsagePointSettlementCustomPropertySet() {
    }

    public UsagePointSettlementCustomPropertySet(PropertySpecService propertySpecService) {
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
        return thesaurus.getFormat(TranslationKeys.CPS_SETTLEMENT_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointSettlementDomainExtension> getPersistenceSupport() {
        return new UsagePointSettlementPersistenceSupport(thesaurus);
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
        PropertySpec settlementAreaSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_AREA.javaName(), TranslationKeys.CPS_SETTLEMENT_AREA)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec settlementMethodSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_METHOD.javaName(), TranslationKeys.CPS_SETTLEMENT_METHOD)
                .describedAs(TranslationKeys.CPS_SETTLEMENT_METHOD_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .addValues("NL", "BE", "UK", "NA")
                .markRequired()
                .finish();
        PropertySpec gridfeeTimeframeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TIMEFRAME.javaName(), TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TIMEFRAME)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        PropertySpec gridfeeTariffcodeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TARIFFCODE.javaName(), TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TARIFFCODE)
                .describedAs(TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TARIFFCODE_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();

        return Arrays.asList(settlementAreaSpec,
                settlementMethodSpec,
                gridfeeTimeframeSpec,
                gridfeeTariffcodeSpec);
    }

    private static class UsagePointSettlementPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointSettlementDomainExtension> {
        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_SETTL";
        public static final String FK_CPS_DEVICE_SETTLEMENT = "RVK_CPS_MTR_USAGEPOINT_SETTL";
        public static final String COMPONENT_NAME = "STL";
        private Thesaurus thesaurus;

        public UsagePointSettlementPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointSettlementDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_SETTLEMENT;
        }

        @Override
        public Class<UsagePointSettlementDomainExtension> persistenceClass() {
            return UsagePointSettlementDomainExtension.class;
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
            table.column(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_AREA.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_AREA.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_METHOD.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomainExtension.Fields.SETTLEMENT_METHOD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TIMEFRAME.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TIMEFRAME.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TARIFFCODE.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomainExtension.Fields.GRIDFEE_TARIFFCODE.javaName())
                    .notNull()
                    .add();
        }
    }

}
