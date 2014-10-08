package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.time", service = {TimeService.class, InstallService.class}, property = "name=" + TimeService.COMPONENT_NAME)
public class TimeServiceImpl implements TimeService, InstallService {
    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile QueryService queryService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;

    public TimeServiceImpl() {}

    @Inject
    public TimeServiceImpl(DataModel dataModel, Clock clock, QueryService queryService, OrmService ormService, NlsService nlsService, UserService userService) {
        this.setClock(clock);
        this.setQueryService(queryService);
        this.setOrmService(ormService);
        this.setThesaurus(nlsService);
        this.setUserService(userService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public RelativePeriod findRelativePeriod(long relativePeriodId) {
        return this.getDataModel().mapper((RelativePeriod.class)).getUnique("id", relativePeriodId).orNull();
    }

    @Override
    public RelativePeriod findRelativePeriodByName(String name) {
        return this.getDataModel().mapper((RelativePeriod.class)).getUnique("name", name).orNull();
    }

    @Override
    public List<RelativePeriod> getRelativePeriods() {
        return dataModel.mapper(RelativePeriod.class).find();
    }

    @Override
    public RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to) {
        RelativePeriodImpl relativePeriod = dataModel.getInstance(RelativePeriodImpl.class);
        relativePeriod.setName(name);
        relativePeriod.setFrom(from);
        relativePeriod.setTo(to);
        relativePeriod.save();
        return findRelativePeriodByName(relativePeriod.getName());
    }

    @Override
    public RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to) {
        RelativePeriodImpl relativePeriod = RelativePeriodImpl.class.cast(findRelativePeriod(id));
        relativePeriod.setName(name);
        relativePeriod.setFrom(from);
        relativePeriod.setTo(to);
        relativePeriod.update();
        return findRelativePeriod(relativePeriod.getId());
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(QueryService.class).toInstance(queryService);
                bind(OrmService.class).toInstance(ormService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS", "USR");
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    @Override
    public void install() {
        new Installer(dataModel, thesaurus, userService).install(true);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Time Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }
}
