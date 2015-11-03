package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

@Component(name = "com.elster.jupiter.parties", service = {UsagePointConfigurationService.class, InstallService.class}, property = "name=" + UsagePointConfigurationService.COMPONENTNAME)
public class UsagePointConfigurationServiceImpl implements UsagePointConfigurationService, InstallService {

    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile EventService eventService;
    private volatile ValidationService validationService;

    public UsagePointConfigurationServiceImpl() {
    }

    @Inject
    public UsagePointConfigurationServiceImpl(Clock clock, OrmService ormService, QueryService queryService, UserService userService, EventService eventService,
            MeteringService meteringService, ValidationService validationService) {
        setClock(clock);
        setOrmService(ormService);
        setQueryService(queryService);
        setUserService(userService);
        setEventService(eventService);
        setValidationService(validationService);
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
                bind(UserService.class).toInstance(userService);
                bind(ValidationService.class).toInstance(validationService);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public void install() {
        new Installer(dataModel, eventService).install(true, true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "MTR", "VAL");
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
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Usage Point Configuration");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
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
            link.get().setMetrologyConfiguration(mc);
            link.get().update();           
            return link.get();
        }
        UsagePointMetrologyConfigurationImpl candidate = new UsagePointMetrologyConfigurationImpl(dataModel, eventService);
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
        List<UsagePointMetrologyConfiguration> list = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(where("metrologyConfiguration").isEqualTo(mc));
        return list.stream().map(each -> each.getUsagePoint()).collect(Collectors.toList());
    }

    @Override
    public List<MetrologyConfiguration> findMetrologyConfigurationsForValidationRuleSet(long id) {
        return this.getDataModel().
                query(MetrologyConfiguration.class, MetrologyConfigurationValidationRuleSetUsage.class).
                select(where("metrologyConfigurationValidationRuleSetUsage.validationRuleSetId").isEqualTo(id), Order.ascending("name"));

    }
}
