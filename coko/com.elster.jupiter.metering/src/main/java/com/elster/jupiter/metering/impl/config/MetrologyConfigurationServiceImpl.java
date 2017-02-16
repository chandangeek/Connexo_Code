/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link MetrologyConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:20)
 */
public class MetrologyConfigurationServiceImpl implements ServerMetrologyConfigurationService {

    static final String METER_ROLE_KEY_PREFIX = "meter.role.";
    static final String METER_PURPOSE_KEY_PREFIX = "metrology.purpose.";

    private MeteringDataModelService meteringDataModelService;
    private DataModel dataModel;
    private Thesaurus thesaurus;

    public MetrologyConfigurationServiceImpl(MeteringDataModelService meteringDataModelService, DataModel dataModel, Thesaurus thesaurus) {
        this.meteringDataModelService = meteringDataModelService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public MetrologyConfigurationBuilder newMetrologyConfiguration(String name, ServiceCategory serviceCategory) {
        MetrologyConfigurationBuilderImpl builder = new MetrologyConfigurationBuilderImpl(getDataModel());
        builder.init(name, serviceCategory);
        return builder;
    }

    @Override
    public UsagePointMetrologyConfigurationBuilder newUsagePointMetrologyConfiguration(String name, ServiceCategory serviceCategory) {
        UsagePointMetrologyConfigurationBuilderImpl builder = new UsagePointMetrologyConfigurationBuilderImpl(getDataModel());
        builder.init(name, serviceCategory);
        return builder;
    }

    @Override
    public List<UsagePointMetrologyConfiguration> findLinkableMetrologyConfigurations(UsagePoint usagePoint) {
        LinkableMetrologyConfigurationFinder finder = new LinkableMetrologyConfigurationFinder(this.dataModel);
        List<UsagePointMetrologyConfigurationImpl> activeConfigs = getDataModel().query(UsagePointMetrologyConfigurationImpl.class)
                .select(where(MetrologyConfigurationImpl.Fields.STATUS.fieldName()).isEqualTo(MetrologyConfigurationStatus.ACTIVE));
        if (!activeConfigs.isEmpty()) {
            activeConfigs.stream()
                    .map(config -> getDataModel().getInstance(UsagePointRequirementSqlBuilder.class).init(usagePoint, config))
                    .forEach(finder::addBuilder);
            return finder.find();
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(long id) {
        return this.getDataModel().mapper(MetrologyConfiguration.class).getOptional(id);
    }

    @Override
    public Optional<ReadingTypeRequirement> findReadingTypeRequirement(long id) {
        return getDataModel().mapper(ReadingTypeRequirement.class).getOptional(id);
    }


    @Override
    public Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version) {
        return this.getDataModel().mapper(MetrologyConfiguration.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(String name) {
        return this.getDataModel().mapper(MetrologyConfiguration.class)
                .getUnique(MetrologyConfigurationImpl.Fields.NAME.fieldName(), name, MetrologyConfigurationImpl.Fields.OBSOLETETIME.fieldName(), null);
    }

    @Override
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return DefaultFinder.of(MetrologyConfiguration.class, where(MetrologyConfigurationImpl.Fields.OBSOLETETIME.fieldName()).isNull(),
                this.getDataModel(), MetrologyContract.class, ReadingTypeDeliverable.class, Formula.class, ReadingTypeRequirement.class)
                .defaultSortColumn("lower(mc.name)")
                .find();
    }

    @Override
    public boolean isInUse(MetrologyConfiguration metrologyConfiguration) {
        Condition condition = where("metrologyConfiguration").isEqualTo(metrologyConfiguration).and(where("interval").isEffective());
        List<EffectiveMetrologyConfigurationOnUsagePoint> atLeastOneUsagePoint = this.getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(condition, new Order[0], false, new String[0], 1, 1);
        return !atLeastOneUsagePoint.isEmpty();
    }

    @Override
    public ServerFormulaBuilder newFormulaBuilder(Formula.Mode mode) {
        return new FormulaBuilderImpl(mode, getDataModel(), getThesaurus());
    }

    public Optional<Formula> findFormula(long id) {
        return getDataModel().mapper(Formula.class).getOptional(id);
    }

    @Override
    public List<Formula> findFormulas() {
        return getDataModel().mapper(Formula.class).find();
    }

    @Override
    public ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter createReadingTypeTemplate(String name) {
        ReadingTypeTemplateImpl template = getDataModel().getInstance(ReadingTypeTemplateImpl.class)
                .init(name);
        return template.startUpdate();
    }

    @Override
    public ReadingTypeTemplate.ReadingTypeTemplateAttributeSetter createReadingTypeTemplate(DefaultReadingTypeTemplate defaultTemplate) {
        ReadingTypeTemplateImpl template = getDataModel().query(ReadingTypeTemplateImpl.class)
                .select(where(ReadingTypeTemplateImpl.Fields.DEFAULT_TEMPLATE.fieldName()).isEqualTo(defaultTemplate))
                .stream()
                .findFirst()
                .orElseGet(() -> getDataModel().getInstance(ReadingTypeTemplateImpl.class).init(defaultTemplate));
        return template.startUpdate();
    }

    @Override
    public Optional<? extends ReadingTypeTemplate> findReadingTypeTemplate(DefaultReadingTypeTemplate defaultTemplate) {
        return getDataModel().query(ReadingTypeTemplateImpl.class)
                .select(where(ReadingTypeTemplateImpl.Fields.DEFAULT_TEMPLATE.fieldName()).isEqualTo(defaultTemplate))
                .stream()
                .findFirst();
    }

    @Override
    public List<ReadingTypeTemplate> getReadingTypeTemplates() {
        return getDataModel().mapper(ReadingTypeTemplate.class).find();
    }

    @Override
    public Optional<ReadingTypeTemplate> findReadingTypeTemplate(String name) {
        return getDataModel().mapper(ReadingTypeTemplate.class).getUnique(ReadingTypeTemplateImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public Optional<ReadingTypeTemplate> findReadingTypeTemplate(long id) {
        return getDataModel().mapper(ReadingTypeTemplate.class).getOptional(id);
    }

    @Override
    public MeterRole newMeterRole(NlsKey name) {
        String localKey = METER_ROLE_KEY_PREFIX + name.getKey();
        this.meteringDataModelService.copyKeyIfMissing(name, localKey);
        MeterRoleImpl meterRole = getDataModel().getInstance(MeterRoleImpl.class).init(localKey);
        Save.CREATE.save(getDataModel(), meterRole);
        return meterRole;
    }

    @Override
    public Optional<MeterRole> findMeterRole(String key) {
        return getDataModel().mapper(MeterRole.class).getUnique(MeterRoleImpl.Fields.KEY.fieldName(), METER_ROLE_KEY_PREFIX + key);
    }

    @Override
    public MeterRole findDefaultMeterRole(DefaultMeterRole defaultMeterRole) {
        return this.findMeterRole(defaultMeterRole.getKey()).get();
    }

    @Override
    public MetrologyPurpose createMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        return this.getDataModel().query(MetrologyPurpose.class)
                .select(where(MetrologyPurposeImpl.Fields.DEFAULT_PURPOSE.fieldName()).isNotNull())
                .stream()
                .map(MetrologyPurposeImpl.class::cast)
                .filter(candidate -> candidate.getDefaultMetrologyPurpose().get() == defaultMetrologyPurpose)
                .findAny()
                .orElseGet(() -> {
                            MetrologyPurposeImpl purpose = this.getDataModel().getInstance(MetrologyPurposeImpl.class).init(defaultMetrologyPurpose);
                            Save.CREATE.save(this.getDataModel(), purpose);
                            return purpose;
                        }
                );
    }

    @Override
    public MetrologyPurpose createMetrologyPurpose(NlsKey name, NlsKey description) {
        String nameKey = Checks.is(name.getKey()).emptyOrOnlyWhiteSpace() ? name.getKey() : METER_PURPOSE_KEY_PREFIX + name.getKey();
        String descriptionKey = Checks.is(name.getKey()).emptyOrOnlyWhiteSpace() ? description.getKey() : METER_PURPOSE_KEY_PREFIX + description.getKey();
        MetrologyPurposeImpl metrologyPurpose = getDataModel().getInstance(MetrologyPurposeImpl.class)
                .init(nameKey, descriptionKey, true);
        metrologyPurpose.save();
        this.meteringDataModelService.copyKeyIfMissing(name, nameKey);
        this.meteringDataModelService.copyKeyIfMissing(description, descriptionKey);
        return metrologyPurpose;
    }

    @Override
    public Optional<MetrologyPurpose> findMetrologyPurpose(long id) {
        return getDataModel().mapper(MetrologyPurpose.class).getOptional(id);
    }

    @Override
    public Optional<MetrologyPurpose> findMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        return getDataModel().query(MetrologyPurpose.class)
                .select(where(MetrologyPurposeImpl.Fields.DEFAULT_PURPOSE.fieldName()).isEqualTo(defaultMetrologyPurpose))
                .stream()
                .findFirst();
    }

    @Override
    public List<MetrologyPurpose> getMetrologyPurposes() {
        return getDataModel().mapper(MetrologyPurpose.class).find();
    }

    @Override
    public Optional<ReadingTypeDeliverable> findReadingTypeDeliverable(long id) {
        return getDataModel().mapper(ReadingTypeDeliverable.class).getOptional(id);
    }

    @Override
    public List<ReadingTypeDeliverable> findReadingTypeDeliverable(ReadingTypeDeliverableFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter can not be null.");
        }
        Condition condition = Condition.TRUE;
        if (!filter.getReadingTypes().isEmpty()) {
            condition = condition.and(where(ReadingTypeDeliverableImpl.Fields.READING_TYPE.fieldName()).in(filter.getReadingTypes()));
        }
        if (!filter.getMetrologyContracts().isEmpty()) {
            Condition mappingCondition = where(MetrologyContractReadingTypeDeliverableUsage.Fields.METROLOGY_CONTRACT.fieldName())
                    .in(filter.getMetrologyContracts());
            Subquery subquery = getDataModel().query(MetrologyContractReadingTypeDeliverableUsage.class)
                    .asSubquery(mappingCondition,
                            MetrologyContractReadingTypeDeliverableUsage.Fields.DELIVERABLE.fieldName());
            condition = condition.and(ListOperator.IN.contains(subquery, "id"));
        }
        if (!filter.getMetrologyConfigurations().isEmpty()) {
            condition = condition.and(where(ReadingTypeDeliverableImpl.Fields.METROLOGY_CONFIGURATION.fieldName()).in(filter.getMetrologyConfigurations()));
        }
        return getDataModel().query(ReadingTypeDeliverable.class, MetrologyConfiguration.class).select(condition);
    }

    @Override
    public void addCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.meteringDataModelService.addCustomUsagePointMeterActivationValidator(customUsagePointMeterActivationValidator);
    }

    @Override
    public void removeCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.meteringDataModelService.removeCustomUsagePointMeterActivationValidator(customUsagePointMeterActivationValidator);
    }

    @Override
    public void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws CustomUsagePointMeterActivationValidationException {
        this.meteringDataModelService.validateUsagePointMeterActivation(meterRole, meter, usagePoint);
    }

    @Override
    public Optional<MetrologyContract> findMetrologyContract(long id) {
        return this.getDataModel().mapper(MetrologyContract.class).getUnique("id", id);
    }

    @Override
    public Optional<MetrologyContract> findAndLockMetrologyContract(long id, long version) {
        return this.getDataModel().mapper(MetrologyContract.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Finder<EffectiveMetrologyConfigurationOnUsagePoint> getEffectiveMetrologyConfigurationFinderFor(MetrologyContract contract) {
        return DefaultFinder.of(EffectiveMetrologyConfigurationOnUsagePoint.class,
                where("metrologyConfiguration").isEqualTo(contract.getMetrologyConfiguration()),
                dataModel, UsagePoint.class, MetrologyConfiguration.class);
    }
}
