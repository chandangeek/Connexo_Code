/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.InsightServiceCategoryCustomPropertySetsCheckList;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UsagePointCorrectionFactorsCPS implements CustomPropertySet<UsagePoint, UsagePointCorrectionFactorsDomExt> {
    public static final String TABLE_NAME = "SLP_CPS_CORRECTIONFACTOR";
    public static final String DOMAIN_FK_NAME = "PK_SLP_CPS_CORRFACTOR";
    public static final String COMPONENT_NAME = "CF1";
    public PropertySpecService propertySpecService;
    public SyntheticLoadProfileService syntheticLoadProfileService;
    public Thesaurus thesaurus;

    public UsagePointCorrectionFactorsCPS() {
        super();
    }

    public UsagePointCorrectionFactorsCPS(PropertySpecService propertySpecService, SyntheticLoadProfileService syntheticLoadProfileService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.syntheticLoadProfileService = syntheticLoadProfileService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public String getName() {
        return this.getThesaurus()
                .getFormat(TranslationKeys.CPS_CORRECTIONFACTORS_NAME)
                .format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CPS_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointCorrectionFactorsDomExt> getPersistenceSupport() {
        return new UsagePointCorrectionFactorsPerSupp(this.getThesaurus());
    }

    @Override
    public boolean isRequired() {
        return true;
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
        List<SyntheticLoadProfile> correctionFactors = syntheticLoadProfileService.findSyntheticLoadProfiles();
        PropertySpecBuilder<SyntheticLoadProfile> lossFactorSpec = propertySpecService
                .referenceSpec(SyntheticLoadProfile.class)
                .named(UsagePointCorrectionFactorsDomExt.Fields.LOSS_FACTOR.javaName(), TranslationKeys.CPS_CORRECTIONFACTOR_LOSS_FACTOR)
                .describedAs(TranslationKeys.CPS_CORRECTIONFACTOR_LOSS_FACTOR)
                .fromThesaurus(this.getThesaurus())
                .markRequired();
        if (correctionFactors.size() == 1) {
            lossFactorSpec.setDefaultValue(correctionFactors.get(0));
        } else {
            lossFactorSpec.addValues(correctionFactors.toArray(new SyntheticLoadProfile[correctionFactors.size()])).markExhaustive(PropertySelectionMode.COMBOBOX);
        }

        return Collections.singletonList(lossFactorSpec.finish());
    }

    private static class UsagePointCorrectionFactorsPerSupp implements PersistenceSupport<UsagePoint, UsagePointCorrectionFactorsDomExt> {

        private Thesaurus thesaurus;

        private UsagePointCorrectionFactorsPerSupp(Thesaurus thesaurus) {
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
            return UsagePointCorrectionFactorsDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return DOMAIN_FK_NAME;
        }

        @Override
        public Class<UsagePointCorrectionFactorsDomExt> persistenceClass() {
            return UsagePointCorrectionFactorsDomExt.class;
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
            Column correctionFactorColumn = table.column(UsagePointCorrectionFactorsDomExt.Fields.LOSS_FACTOR.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.foreignKey("FK_SLP_CPS_CORRFACTOR")
                    .references(SyntheticLoadProfile.class)
                    .on(correctionFactorColumn)
                    .map(UsagePointCorrectionFactorsDomExt.Fields.LOSS_FACTOR.javaName())
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(UsagePointCorrectionFactorsDomExt.Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(UsagePointCorrectionFactorsDomExt.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

        @Override
        public String application() {
            return InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME;
        }
    }
}
