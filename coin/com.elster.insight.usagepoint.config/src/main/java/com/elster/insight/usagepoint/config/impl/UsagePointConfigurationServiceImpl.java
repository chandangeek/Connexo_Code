package com.elster.insight.usagepoint.config.impl;

import java.security.Principal;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

@Component(name = "com.elster.jupiter.parties", service = {UsagePointConfigurationService.class, InstallService.class}, property = "name=" + UsagePointConfigurationService.COMPONENTNAME)
public class UsagePointConfigurationServiceImpl implements UsagePointConfigurationService, InstallService {

    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile EventService eventService;
    private volatile OrmService ormService;
    private volatile MeteringService meteringService;

    public UsagePointConfigurationServiceImpl() {
    }

    @Inject
    public UsagePointConfigurationServiceImpl(Clock clock, OrmService ormService, QueryService queryService, UserService userService, EventService eventService, ThreadPrincipalService threadPrincipalService,
            MeteringService meteringService) {
        setClock(clock);
        setOrmService(ormService);
        setQueryService(queryService);
        setUserService(userService);
        setEventService(eventService);
        setThreadPrincipalService(threadPrincipalService);
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
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    private Principal getPrincipal() {
        return threadPrincipalService.getPrincipal();
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
        return Arrays.asList("ORM", "EVT");
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
        this.ormService = ormService;
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
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return DefaultFinder.of(MetrologyConfiguration.class, this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public UsagePointMetrologyConfiguration link(UsagePoint up, MetrologyConfiguration mc, Interval interval) {
        UsagePointMetrologyConfigurationImpl candidate = new UsagePointMetrologyConfigurationImpl(clock, dataModel, eventService);
        candidate.init(up, mc, interval);
        validateAddingLink(candidate);
        candidate.update();
        return candidate;        
    }
    
    private void validateAddingLink(UsagePointMetrologyConfigurationImpl candidate) {
        List<UsagePointMetrologyConfiguration> existing = dataModel.mapper(UsagePointMetrologyConfiguration.class).find();
        for (UsagePointMetrologyConfiguration other : existing) {
            if (candidate.conflictsWith(other)) {
                throw new IllegalArgumentException("Conflicts with existing association : " + other);
            }
        }
    }
}
