package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
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
import com.elster.jupiter.metering.impl.DefaultTranslationKey;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link MetrologyConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:20)
 */
public class MetrologyConfigurationServiceImpl implements ServerMetrologyConfigurationService, PrivilegesProvider, TranslationKeyProvider {

    static final String METER_ROLE_KEY_PREFIX = "meter.role.";
    static final String METER_PURPOSE_KEY_PREFIX = "metrology.purpose.";

    private volatile ServerMeteringService meteringService;
    private volatile UserService userService;
    private volatile MeterActivationValidatorsWhiteboard activationValidatorsWhiteboard;

    @Inject
    public MetrologyConfigurationServiceImpl(ServerMeteringService meteringService, UserService userService, MeterActivationValidatorsWhiteboard activationValidatorsWhiteboard) {
        this.meteringService = meteringService;
        this.userService = userService;
        this.activationValidatorsWhiteboard = activationValidatorsWhiteboard;
    }

    public void install(Logger logger) {
        new Installer(this.meteringService, this).install(null, logger);
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return this.getComponentName();
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION.getKey(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION,
                                Privileges.Constants.VIEW_METROLOGY_CONFIGURATION)));
        return resources;
    }

    @Override
    public String getComponentName() {
        return MeteringService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        translationKeys.addAll(Arrays.asList(DefaultMetrologyPurpose.Translation.values()));
        translationKeys.addAll(Arrays.asList(DefaultReadingTypeTemplate.TemplateTranslation.values()));
        translationKeys.addAll(Arrays.asList(MetrologyConfigurationStatus.Translation.values()));
        return translationKeys;
    }

    @Override
    public DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    @Override
    public Thesaurus getThesaurus() {
        return this.meteringService.getThesaurus();
    }

    @Override
    public Clock getClock() {
        return this.meteringService.getClock();
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
        LinkableMetrologyConfigurationFinder finder = new LinkableMetrologyConfigurationFinder(this.meteringService);
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
        return this.getDataModel().mapper(MetrologyConfiguration.class).getUnique("id", id);
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
        return this.getDataModel().mapper(MetrologyConfiguration.class).getUnique("name", name);
    }

    @Override
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return DefaultFinder.of(MetrologyConfiguration.class, this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public boolean isInUse(MetrologyConfiguration metrologyConfiguration) {
        Condition condition = Where.where("metrologyConfiguration").isEqualTo(metrologyConfiguration);
        List<EffectiveMetrologyConfigurationOnUsagePoint> atLeastOneUsagePoint = this.getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class)
                .select(condition, new Order[0], false, new String[0], 1, 1);
        return !atLeastOneUsagePoint.isEmpty();
    }

    @Override
    public Optional<MetrologyContract> findMetrologyContract(long id) {
        return this.getDataModel().mapper(MetrologyContract.class).getOptional(id);
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
    public MeterRole newMeterRole(NlsKey name) {
        String localKey = METER_ROLE_KEY_PREFIX + name.getKey();
        this.meteringService.copyKeyIfMissing(name, localKey);
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
        this.meteringService.copyKeyIfMissing(name, nameKey);
        this.meteringService.copyKeyIfMissing(description, descriptionKey);
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
                    .asSubquery(mappingCondition, MetrologyContractReadingTypeDeliverableUsage.Fields.DELIVERABLE.fieldName());
            condition = condition.and(ListOperator.IN.contains(subquery, "id"));
        }
        if (!filter.getMetrologyConfigurations().isEmpty()) {
            condition = condition.and(where(ReadingTypeDeliverableImpl.Fields.METROLOGY_CONFIGURATION.fieldName()).in(filter.getMetrologyConfigurations()));
        }
        return getDataModel().query(ReadingTypeDeliverable.class, MetrologyConfiguration.class).select(condition);
    }

    @Override
    public void addCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.activationValidatorsWhiteboard.addCustomUsagePointMeterActivationValidator(customUsagePointMeterActivationValidator);
    }

    @Override
    public void removeCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.activationValidatorsWhiteboard.removeCustomUsagePointMeterActivationValidator(customUsagePointMeterActivationValidator);
    }

    @Override
    public void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws
            CustomUsagePointMeterActivationValidationException {
        this.activationValidatorsWhiteboard.validateUsagePointMeterActivation(meterRole, meter, usagePoint);
    }

}