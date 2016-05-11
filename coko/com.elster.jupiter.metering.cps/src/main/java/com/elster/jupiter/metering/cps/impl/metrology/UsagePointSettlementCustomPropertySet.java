package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointSettlementCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointSettlementDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_SETTL";
    public static final String FK_CPS_DEVICE_SETTLEMENT = "RVK_CPS_MTR_USAGEPOINT_SETTL";
    public static final String COMPONENT_NAME = "STL";

    public UsagePointSettlementCustomPropertySet() {
        super();
    }

    public UsagePointSettlementCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_SETTLEMENT_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointSettlementDomExt> getPersistenceSupport() {
        return new UsagePointSettlPersistSupp(this.getThesaurus());
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
                .named(UsagePointSettlementDomExt.Fields.SETTLEMENT_AREA.javaName(), TranslationKeys.CPS_SETTLEMENT_AREA)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();
        PropertySpec settlementMethodSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomExt.Fields.SETTLEMENT_METHOD.javaName(), TranslationKeys.CPS_SETTLEMENT_METHOD)
                .describedAs(TranslationKeys.CPS_SETTLEMENT_METHOD_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("NL", "BE", "UK", "NA")
                .markRequired()
                .finish();
        PropertySpec gridfeeTimeframeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomExt.Fields.GRIDFEE_TIMEFRAME.javaName(), TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TIMEFRAME)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();
        PropertySpec gridfeeTariffcodeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointSettlementDomExt.Fields.GRIDFEE_TARIFFCODE.javaName(), TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TARIFFCODE)
                .describedAs(TranslationKeys.CPS_SETTLEMENT_GRIDFEE_TARIFFCODE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();

        return Arrays.asList(settlementAreaSpec,
                settlementMethodSpec,
                gridfeeTimeframeSpec,
                gridfeeTariffcodeSpec);
    }

    private static class UsagePointSettlPersistSupp implements PersistenceSupport<UsagePoint, UsagePointSettlementDomExt> {
        private Thesaurus thesaurus;

        public UsagePointSettlPersistSupp(Thesaurus thesaurus) {
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
            return UsagePointSettlementDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_SETTLEMENT;
        }

        @Override
        public Class<UsagePointSettlementDomExt> persistenceClass() {
            return UsagePointSettlementDomExt.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(UsagePointSettlementDomExt.Fields.SETTLEMENT_AREA.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomExt.Fields.SETTLEMENT_AREA.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomExt.Fields.SETTLEMENT_METHOD.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomExt.Fields.SETTLEMENT_METHOD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomExt.Fields.GRIDFEE_TIMEFRAME.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomExt.Fields.GRIDFEE_TIMEFRAME.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointSettlementDomExt.Fields.GRIDFEE_TARIFFCODE.databaseName())
                    .varChar(255)
                    .map(UsagePointSettlementDomExt.Fields.GRIDFEE_TARIFFCODE.javaName())
                    .notNull()
                    .add();
        }
    }

}
