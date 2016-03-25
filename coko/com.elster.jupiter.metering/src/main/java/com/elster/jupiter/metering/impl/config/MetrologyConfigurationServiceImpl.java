package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.metering.impl.DefaultTranslationKey;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link MetrologyConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:20)
 */
public class MetrologyConfigurationServiceImpl implements ServerMetrologyConfigurationService, InstallService, PrivilegesProvider, TranslationKeyProvider {

    private volatile ServerMeteringService meteringService;
    private volatile EventService eventService;
    private volatile UserService userService;

    @Inject
    public MetrologyConfigurationServiceImpl(ServerMeteringService meteringService, EventService eventService, UserService userService) {
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install() {
        new Installer(this.meteringService, this).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "MTR", "VAL", NlsService.COMPONENTNAME, CustomPropertySetService.COMPONENT_NAME);
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
        List<UsagePointMetrologyConfiguration> atLeastOneUsagePoint = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !atLeastOneUsagePoint.isEmpty();
    }


    @Override
    public FormulaBuilder newFormulaBuilder(Formula.Mode mode) {
        return new FormulaBuilderImpl(mode, getDataModel(), getThesaurus());
    }

    public ReadingTypeDeliverableBuilder newReadingTypeDeliverableBuilder(String name, MetrologyConfiguration metrologyConfiguration, ReadingType readingType, Formula.Mode mode) {
        return new ReadingTypeDeliverableBuilder(metrologyConfiguration, name, readingType, mode, getDataModel(), this.getThesaurus());
    }


    public Optional<Formula> findFormula(long id) {
        return getDataModel().mapper(Formula.class).getOptional(id);
    }

    @Override
    public List<Formula> findFormulas() {
        return getDataModel().mapper(Formula.class).find();
    }

    @Override
    public ReadingTypeTemplate createReadingTypeTemplate(String name) {
        ReadingTypeTemplateImpl template = getDataModel().getInstance(ReadingTypeTemplateImpl.class)
                .init(name);
        template.save();
        return template;
    }

    @Override
    public ReadingTypeTemplate createReadingTypeTemplate(DefaultReadingTypeTemplate defaultTemplate) {
        ReadingTypeTemplateImpl template = getDataModel().getInstance(ReadingTypeTemplateImpl.class)
                .init(defaultTemplate);
        template.save();
        return template;
    }

    @Override
    public Optional<ReadingTypeTemplate> findReadingTypeTemplate(long id) {
        return getDataModel().mapper(ReadingTypeTemplate.class).getOptional(id);
    }

    @Override
    public Optional<ReadingTypeTemplate> findAndLockReadingTypeTemplateByIdAndVersion(long id, long version) {
        return getDataModel().mapper(ReadingTypeTemplate.class).lockObjectIfVersion(version, id);
    }

    @Override
    public MeterRole newMeterRole(TranslationKey key) {
        MeterRoleImpl meterRole = getDataModel().getInstance(MeterRoleImpl.class).init(key.getKey());
        Save.CREATE.save(getDataModel(), meterRole);
        return meterRole;
    }

    @Override
    public Optional<MeterRole> findMeterRole(String key) {
        return getDataModel().mapper(MeterRole.class).getUnique(MeterRoleImpl.Fields.KEY.fieldName(), key);
    }

    @Override
    public MetrologyPurpose.MetrologyPurposeBuilder createMetrologyPurpose() {
        return new MetrologyPurposeBuilderImpl(getDataModel());
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

    //@Override
    public ReadingTypeDeliverable createReadingTypeDeliverable(MetrologyConfiguration metrologyConfiguration, String name, ReadingType readingType, Formula formula) {
        return metrologyConfiguration.addReadingTypeDeliverable(name, readingType, formula);
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
            Condition mappingCondition = where(MetrologyContractReadingTypeDeliverableMapping.Fields.METROLOGY_CONTRACT.fieldName())
                    .in(filter.getMetrologyContracts());
            Subquery subquery = getDataModel().query(MetrologyContractReadingTypeDeliverableMapping.class)
                    .asSubquery(mappingCondition, MetrologyContractReadingTypeDeliverableMapping.Fields.DELIVERABLE.fieldName());
            condition = condition.and(ListOperator.IN.contains(subquery, "id"));
        }
        if (!filter.getMetrologyConfigurations().isEmpty()) {
            condition = condition.and(where(ReadingTypeDeliverableImpl.Fields.METROLOGY_CONFIGURATION.fieldName()).in(filter.getMetrologyConfigurations()));
        }
        return getDataModel().query(ReadingTypeDeliverable.class, MetrologyConfiguration.class).select(condition);
    }
}