package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.domain.util.DefaultFinder;
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

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by igh on 18/04/2016.
 */


@Component(name = "com.elster.jupiter.calendar",
        service = {CalendarService.class, InstallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + CalendarService.COMPONENTNAME,
        immediate = true)
public class CalendarServiceImpl implements ServerCalendarService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider, InstallService {

    static final String TIME_OF_USE_CATEGORY_NAME = "Time of use";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;

    public CalendarServiceImpl() {
    }

    @Inject
    public CalendarServiceImpl(OrmService ormService, NlsService nlsService, UserService userService) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(CalendarService.COMPONENTNAME, "Calendars");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        InstallerImpl installer = new InstallerImpl(this, dataModel);
        installer.install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, UserService.COMPONENTNAME);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(CalendarService.class).toInstance(CalendarServiceImpl.this);
                bind(ServerCalendarService.class).toInstance(CalendarServiceImpl.this);
            }
        };
    }

    @Override
    public String getComponentName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getModuleName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_TOU_CALENDARS.getKey(), Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.MANAGE_TOU_CALENDARS)));
        return resources;
    }

    @Override
    public CalendarBuilder newCalendar(String name, TimeZone timeZone, Year start) {
        CalendarBuilderImpl builder = new CalendarBuilderImpl(getDataModel());
        builder.init(name, timeZone, start);
        return builder;
    }

    public List<Calendar> findAllCalendars() {
        return DefaultFinder.of(Calendar.class, this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public Optional<Calendar> findCalendar(long id) {
        return this.getDataModel().mapper(Calendar.class).getUnique("id", id);
    }

    @Override
    public Optional<Calendar> findCalendarByName(String name) {
        return this.getDataModel().mapper(Calendar.class).getUnique("name", name);
    }

    @Override
    public Optional<Calendar> findCalendarByMRID(String mRID) {
        return this.getDataModel().mapper(Calendar.class).getUnique("mRID", mRID);
    }

    @Override
    public Optional<Category> findTimeOfUseCategory() {
        return this.getDataModel().mapper(Category.class).getUnique("name", TIME_OF_USE_CATEGORY_NAME);
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}
