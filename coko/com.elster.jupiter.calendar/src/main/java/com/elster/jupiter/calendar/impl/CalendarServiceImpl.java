package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarResolver;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;

/**
 * Created by igh on 18/04/2016.
 */


@Component(name = "com.elster.jupiter.calendar",
        service = {CalendarService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + CalendarService.COMPONENTNAME,
        immediate = true)
public class CalendarServiceImpl implements ServerCalendarService, MessageSeedProvider, TranslationKeyProvider {

    static final String TIME_OF_USE_CATEGORY_NAME = "Time of use";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;

    private final List<CalendarResolver> calendarResolvers = new CopyOnWriteArrayList<>();

    public CalendarServiceImpl() {
    }

    @Inject
    public CalendarServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, EventService eventService, UpgradeService upgradeService, MessageService messageService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        activate();
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

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", CalendarService.COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(CalendarService.class).toInstance(CalendarServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(ServerCalendarService.class).toInstance(CalendarServiceImpl.this);
                bind(UserService.class).toInstance(userService);
                bind(MessageService.class).toInstance(messageService);
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
    public Optional<Category> findCategoryByName(String name) {
        return this.getDataModel().mapper(Category.class).getUnique("name", name);
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
    public boolean isCalendarInUse(Calendar calendar) {
        for (CalendarResolver resolver : calendarResolvers) {
            if (resolver.isCalendarInUse(calendar)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCalendarResolver(CalendarResolver resolver) {
        calendarResolvers.add(resolver);
    }

    public void removeCalendarResolver(CalendarResolver resolver) {
        calendarResolvers.remove(resolver);
    }
}
