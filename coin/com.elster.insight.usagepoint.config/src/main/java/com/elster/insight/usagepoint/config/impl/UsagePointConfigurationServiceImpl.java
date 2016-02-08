package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.insight.usagepoint.config.impl.errors.MessageSeeds;
import com.elster.insight.usagepoint.config.security.Privileges;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.insight.usagepoint.config.impl.UsagePointConfigurationServiceImpl",
        service = {UsagePointConfigurationService.class, InstallService.class, PrivilegesProvider.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + UsagePointConfigurationService.COMPONENTNAME},
        immediate = true)
public class UsagePointConfigurationServiceImpl implements UsagePointConfigurationService, InstallService, PrivilegesProvider, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile EventService eventService;
    private volatile ValidationService validationService;
    private volatile UserService userService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;

    @SuppressWarnings("unused")
    public UsagePointConfigurationServiceImpl() {
    }

    @Inject
    public UsagePointConfigurationServiceImpl(Clock clock, OrmService ormService, EventService eventService, UserService userService,
                                              ValidationService validationService, NlsService nlsService,
                                              CustomPropertySetService customPropertySetService) {
        setClock(clock);
        setOrmService(ormService);
        setEventService(eventService);
        setUserService(userService);
        setValidationService(validationService);
        setCustomPropertySetService(customPropertySetService);
        setNlsService(nlsService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Clock.class).toInstance(clock);
                bind(ValidationService.class).toInstance(validationService);
                bind(UserService.class).toInstance(userService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UsagePointConfigurationService.class).toInstance(UsagePointConfigurationServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    @Override
    public void install() {
        new Installer(dataModel, eventService).install(true, true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "MTR", "VAL", NlsService.COMPONENTNAME, CustomPropertySetService.COMPONENT_NAME);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Usage Point Configuration");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public MetrologyConfiguration newMetrologyConfiguration(String name) {
        MetrologyConfigurationImpl metrologyConfiguration = dataModel.getInstance(MetrologyConfigurationImpl.class).init(name);
        metrologyConfiguration.update();
        return metrologyConfiguration;
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(long id) {
        return dataModel.mapper(MetrologyConfiguration.class).getUnique("id", id);
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(String name) {
        return dataModel.mapper(MetrologyConfiguration.class).getUnique("name", name);
    }

    @Override
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return DefaultFinder.of(MetrologyConfiguration.class, this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public UsagePointMetrologyConfiguration link(UsagePoint up, MetrologyConfiguration mc) {
        Optional<UsagePointMetrologyConfiguration> link = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(where("usagePoint").isEqualTo(up)).stream().findFirst();
        if (link.isPresent()) {
            link.get().updateMetrologyConfiguration(mc);
            return link.get();
        }
        UsagePointMetrologyConfigurationImpl candidate = dataModel.getInstance(UsagePointMetrologyConfigurationImpl.class);
        candidate.init(up, mc);
        candidate.update();
        return candidate;
    }

    @Override
    public Boolean unlink(UsagePoint up, MetrologyConfiguration mc) {
        Boolean result = false;
        Optional<UsagePointMetrologyConfiguration> link = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(where("usagePoint").isEqualTo(up)).stream().findFirst();
        if (link.isPresent()) {
            link.get().delete();
            result = true;
        }
        return result;
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up) {
        Optional<UsagePointMetrologyConfiguration> obj = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(where("usagePoint").isEqualTo(up)).stream().findFirst();
        if (!obj.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(obj.get().getMetrologyConfiguration());
    }

    @Override
    public List<UsagePoint> findUsagePointsForMetrologyConfiguration(MetrologyConfiguration mc) {
        return this.getDataModel()
                .query(UsagePointMetrologyConfiguration.class)
                .select(where("metrologyConfiguration").isEqualTo(mc))
                .stream()
                .map(UsagePointMetrologyConfiguration::getUsagePoint)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetrologyConfiguration> findMetrologyConfigurationsForValidationRuleSet(ValidationRuleSet rs) {
        return this.getDataModel()
                .query(MetrologyConfigurationValidationRuleSetUsage.class)
                .select(where("validationRuleSet").isEqualTo(rs))
                .stream()
                .map(MetrologyConfigurationValidationRuleSetUsage::getMetrologyConfiguration)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version) {
        return dataModel.mapper(MetrologyConfiguration.class).lockObjectIfVersion(version, id);
    }

    @Override
    public String getModuleName() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_METROLOGY_CONFIG.getKey(), Privileges.RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION, Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION)));
        return resources;
    }

    @Override
    public String getComponentName() {
        return getModuleName();
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Privileges.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
