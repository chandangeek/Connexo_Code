package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.insight.usagepoint.config.impl.UsagePointConfigurationServiceImpl",
        service = {UsagePointConfigurationService.class, InstallService.class},
        property = {"name=" + UsagePointConfigurationService.COMPONENTNAME},
        immediate = true)
public class UsagePointConfigurationServiceImpl implements UsagePointConfigurationService, InstallService {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile EventService eventService;
    private volatile ValidationService validationService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;

    // For OSGi purpose
    public UsagePointConfigurationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public UsagePointConfigurationServiceImpl(Clock clock, OrmService ormService, EventService eventService, UserService userService,
                                              ValidationService validationService, NlsService nlsService, MetrologyConfigurationService metrologyConfigurationService) {
        this();
        setClock(clock);
        setOrmService(ormService);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setEventService(eventService);
        setUserService(userService);
        setValidationService(validationService);
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
        new Installer(dataModel, eventService).install(true, true);
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
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public MetrologyConfiguration newMetrologyConfiguration(String name) {
        return this.metrologyConfigurationService.newMetrologyConfiguration(name);
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(long id) {
        return this.metrologyConfigurationService.findMetrologyConfiguration(id);
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(String name) {
        return this.metrologyConfigurationService.findMetrologyConfiguration(name);
    }

    @Override
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return this.metrologyConfigurationService.findAllMetrologyConfigurations();
    }

    @Override
    public void link(UsagePoint usagePoint, MetrologyConfiguration metrologyConfiguration) {
        usagePoint.apply(metrologyConfiguration, this.clock.instant());
    }

    @Override
    public Boolean unlink(UsagePoint usagePoint, MetrologyConfiguration mc) {
        usagePoint.removeMetrologyConfiguration(this.clock.instant());
        return Boolean.TRUE;
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint usagePoint) {
        return usagePoint.getMetrologyConfiguration();
    }

    @Override
    public List<UsagePoint> findUsagePointsForMetrologyConfiguration(MetrologyConfiguration metrologyConfiguration) {
        // This is
        return Collections.emptyList();
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
    public void addValidationRuleSet(MetrologyConfiguration metrologyConfiguration, ValidationRuleSet validationRuleSet) {
        MetrologyConfigurationValidationRuleSetUsageImpl newUsage = this.dataModel.getInstance(MetrologyConfigurationValidationRuleSetUsageImpl.class).init(metrologyConfiguration, validationRuleSet);
        getDataModel().persist(newUsage);
    }

    @Override
    public void removeValidationRuleSet(MetrologyConfiguration metrologyConfiguration, ValidationRuleSet validationRuleSet) {
        List<MetrologyConfigurationValidationRuleSetUsage> atMostOneUsage = this.dataModel
                .mapper(MetrologyConfigurationValidationRuleSetUsage.class)
                .find(
                    MetrologyConfigurationValidationRuleSetUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName(), metrologyConfiguration,
                    MetrologyConfigurationValidationRuleSetUsageImpl.Fields.VALIDATION_RULE_SET.fieldName(), validationRuleSet);
        if (atMostOneUsage.isEmpty()) {
            return; // ValidationRuletSet was not added to MetrologyConfiguration before
        } else {
            // There can be only 1 because of the primary key
            atMostOneUsage.get(0).close(this.clock.instant());
        }
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets(MetrologyConfiguration metrologyConfiguration) {
        return this.dataModel
                .mapper(MetrologyConfigurationValidationRuleSetUsage.class)
                .find(MetrologyConfigurationValidationRuleSetUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName(), metrologyConfiguration)
                .stream()
                .map(MetrologyConfigurationValidationRuleSetUsage::getValidationRuleSet)
                .collect(Collectors.toList());
    }

}