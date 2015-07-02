package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.security.Privileges;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.time", service = {TimeService.class, InstallService.class, PrivilegesProvider.class}, property = "name=" + TimeService.COMPONENT_NAME, immediate = true)
public class TimeServiceImpl implements TimeService, InstallService, PrivilegesProvider {
    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile EventService eventService;

    public TimeServiceImpl() {
    }

    @Inject
    public TimeServiceImpl(QueryService queryService, OrmService ormService, NlsService nlsService, UserService userService, EventService eventService) {
        this.setQueryService(queryService);
        this.setOrmService(ormService);
        this.setThesaurus(nlsService);
        this.setUserService(userService);
        this.setEventService(eventService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public Optional<RelativePeriod> findRelativePeriod(long relativePeriodId) {
        return this.getDataModel().mapper((RelativePeriod.class)).getUnique("id", relativePeriodId);
    }

    @Override
    public Optional<RelativePeriod> findRelativePeriodByName(String name) {
        return this.getDataModel().mapper((RelativePeriod.class)).getUnique("name", name);
    }

    @Override
    public List<RelativePeriod> getRelativePeriods() {
        return dataModel.mapper(RelativePeriod.class).find();
    }

    @Override
    public RelativePeriod createRelativePeriod(String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories) {
        RelativePeriodImpl relativePeriod = dataModel.getInstance(RelativePeriodImpl.class);
        relativePeriod.setName(name);
        relativePeriod.setRelativeDateFrom(from);
        relativePeriod.setRelativeDateTo(to);
        categories.stream().forEach(relativePeriod::addRelativePeriodCategory);
        relativePeriod.save();
        return relativePeriod;
    }

    @Override
    public RelativePeriod updateRelativePeriod(Long id, String name, RelativeDate from, RelativeDate to, List<RelativePeriodCategory> categories) {
        RelativePeriodImpl relativePeriod = RelativePeriodImpl.class.cast(findRelativePeriod(id));
        relativePeriod.setName(name);
        relativePeriod.setRelativeDateFrom(from);
        relativePeriod.setRelativeDateTo(to);
        relativePeriod.setRelativePeriodCategoryUsages(categories);
        relativePeriod.save();
        return relativePeriod;
    }

    @Override
    public RelativePeriodCategory createRelativePeriodCategory(String name) {
        RelativePeriodCategoryImpl category = RelativePeriodCategoryImpl.from(getDataModel(), name);
        category.save();
        return category;
    }

    @Override
    public Optional<RelativePeriodCategory> findRelativePeriodCategory(long relativePeriodCategoryId) {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).getUnique("id", relativePeriodCategoryId);
    }

    @Override
    public Optional<RelativePeriodCategory> findRelativePeriodCategoryByName(String name) {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).getUnique("name", name);
    }

    @Override
    public List<RelativePeriodCategory> getRelativePeriodCategories() {
        return this.getDataModel().mapper((RelativePeriodCategory.class)).find();
    }

    @Override
    public Query<? extends RelativePeriod> getRelativePeriodQuery() {
        return queryService.wrap(dataModel.query(RelativePeriod.class, RelativePeriodCategoryUsage.class, RelativePeriodCategory.class));
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(QueryService.class).toInstance(queryService);
                bind(OrmService.class).toInstance(ormService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(EventService.class).toInstance(eventService);
            }
        };
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS", "USR", "EVT");
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    @Override
    public void install() {
        new Installer(dataModel, this, thesaurus, userService, eventService).install(true);
    }

    @Override
    public String toLocalizedString(PeriodicalScheduleExpression expression) {
        return expression.toString(thesaurus);
    }

    @Override
    public RelativePeriod getAllRelativePeriod() {
        return AllRelativePeriod.INSTANCE;
    }

    @Override
    public String getModuleName() {
        return TimeService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(TimeService.COMPONENT_NAME, "period.periods", "period.periods.description",
                Arrays.asList(Privileges.VIEW_RELATIVE_PERIOD, Privileges.ADMINISTRATE_RELATIVE_PERIOD)));
        return resources;
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

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }
}
